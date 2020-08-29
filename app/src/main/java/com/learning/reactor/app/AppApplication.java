package com.learning.reactor.app;

import ch.qos.logback.core.CoreConstants;
import com.learning.reactor.app.models.Comentarios;
import com.learning.reactor.app.models.Usuario;
import com.learning.reactor.app.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.CompositeName;
import java.awt.color.CMMException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class AppApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			//ejemploToString();
			ejemploIntervaloDesdeCreate();
			//ejemploDelay();
			//ejemploInterval();
			//ejemploZipWithRangos();
			//ejemploUsuarioComentariosZIPforma2();
			//ejemploUsuarioComentariosZIP();
			//ejemploUsuarioComentariosFlatMap();
			//ejemploCollectList();
			//ejemploFlatmap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void ejemploIntervaloDesdeCreate(){
		Flux.create(emitter->{
			Timer timer=new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador=0;
				@Override
				public void run() {
					emitter.next(++contador);
					if(contador==10){
						timer.cancel();
						emitter.complete();
					}

					if(contador==5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el Flux en 5"));
					}
				}
			}, 1000, 1000);
		})
				.subscribe(next->log.info(next.toString()),
						error->log.error(error.getMessage()),
						()-> log.info("Hemos terminado"));
	}
	public void ejemploIntervaloinfinito() throws InterruptedException {

		CountDownLatch latch=new CountDownLatch(1);
		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i->{
					if(i>=5){
						return Flux.error(new InterruptedException(("Solo hasta 5")));
					}
					return Flux.just(i);
				})
				.map(i->"Hola "+i)
			 	.retry(2)
				.subscribe(log::info, e->log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploZipWithRangos(){
		Flux.just(1,2,3,4)
				.map(i->(i*2))
				.zipWith(Flux.range(0,4),
						(uno,dos)->String.format("Primer Flux: %d, Segundo Flux: %d",uno,dos))
				.subscribe(log::info);
	}
	public void ejemploDelay() throws InterruptedException {
		Flux<Integer>rango=Flux.range(1,12)
		.delayElements(Duration.ofSeconds((1))).doOnNext(i->log.info(i.toString()));
		rango.blockLast();

		Thread.sleep(1300);

	}
	public void ejemploInterval(){
		Flux<Integer>rango=Flux.range(1,12);
		Flux<Long>retraso=Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(retraso,(ra,re)->ra)
				.doOnNext(i->log.info(i.toString()))
				.blockLast();
	}

	public void ejemploUsuarioComentariosZIPforma2(){
		//Mono<Usuario>usuarioMono=Mono.fromCallable(()->crearUsuario());

		Mono<Usuario>usuarioMono=Mono.fromCallable(()-> new Usuario("John","Doe"));
		Mono<Comentarios>comentariosUsuarioMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola Pepe,qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy haciendo un curso de spring boot con web flux y reactive java");
			return comentarios;
		});
		Mono<UsuarioComentarios>usuarioConComentarios=
				usuarioMono.zipWith(comentariosUsuarioMono)
				.map(tuple ->{
					Usuario u=tuple.getT1();
					Comentarios c=tuple.getT2();
					return new UsuarioComentarios(u,c);
				});

		usuarioConComentarios.subscribe(uc->log.info(uc.toString()));
	}
	public void ejemploUsuarioComentariosZIP(){
		//Mono<Usuario>usuarioMono=Mono.fromCallable(()->crearUsuario());

		Mono<Usuario>usuarioMono=Mono.fromCallable(()-> new Usuario("John","Doe"));
		Mono<Comentarios>comentariosUsuarioMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola Pepe,qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy haciendo un curso de spring boot con web flux y reactive java");
			return comentarios;
		});
		Mono<UsuarioComentarios>usuarioConComentarios=usuarioMono
				.zipWith(comentariosUsuarioMono, UsuarioComentarios::new);
//		Mono<UsuarioComentarios>usuarioConComentarios=usuarioMono
//				.zipWith(comentariosUsuarioMono,(usuarios,comentariosUsuarios)
//						->new UsuarioComentarios(usuarios,comentariosUsuarios));
						usuarioConComentarios.subscribe(uc->log.info(uc.toString()));
	}
	public Usuario crearUsuario(){
		return new Usuario("John","Doe");
	}


	public void ejemploUsuarioComentariosFlatMap(){
		//Mono<Usuario>usuarioMono=Mono.fromCallable(()->crearUsuario());

		Mono<Usuario>usuarioMono=Mono.fromCallable(()-> new Usuario("John","Doe"));
		Mono<Comentarios>comentariosUsuarioMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola Pepe,qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy haciendo un curso de spring boot con web flux y reactive java");
			return comentarios;
		});
		usuarioMono.flatMap(u->comentariosUsuarioMono.map(c->new UsuarioComentarios(u,c)))
				.subscribe(uc->log.info(uc.toString()));
	}


	public void ejemploCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Juan", "Guzmán"));
		usuariosList.add(new Usuario("Manuel", "Fulano"));
		usuariosList.add(new Usuario("Estefanía", "Algo"));
		usuariosList.add(new Usuario("Josef", "Lang"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()//Convierte a un solo objeto un Mono que sería en este caso la lista de Usuarios
				.subscribe(lista -> {
					lista.forEach(item->
					log.info(item.toString()));
				});
	}

	public void ejemploToString() throws Exception {
		List<Usuario>usuariosList=new ArrayList<>();
		usuariosList.add(new Usuario("Juan", "Guzmán"));
		usuariosList.add(new Usuario("Manuel", "Fulano"));
		usuariosList.add(new Usuario("Estefanía", "Algo"));
		usuariosList.add(new Usuario("Josef", "Lang"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario->usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())){
						return Mono.just(nombre);
					}
					return Mono.empty();
				})
				.map(String::toLowerCase)
				.subscribe(u->log.info(u.toString()));

	}
	public void ejemploFlatmap() throws Exception {
		List<String>usuariosList=new ArrayList<>();
		usuariosList.add("Juan Guzmán");
		usuariosList.add("Manuel Fulano");
		usuariosList.add("Estefanía Andres");
		usuariosList.add("Josefa Algo");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");
		//.just("Juan Guzmán","Manuel Fulano","Estefanía Andres","Josefa Algo","Bruce Lee","Bruce Willis");
		Flux.fromIterable(usuariosList)
		.map(nombre->new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")){
						return Mono.just(usuario);
					}
					return Mono.empty();
				})
				.map(usuario->{
					String nombre=usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
			.subscribe(u->log.info(u.toString()));

	}


}
