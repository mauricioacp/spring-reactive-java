package com.test.spring.boot.webflux;

import com.test.spring.boot.webflux.models.documents.Categoria;
import com.test.spring.boot.webflux.models.documents.Producto;
import com.test.spring.boot.webflux.models.services.IProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private IProductoService service;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electrónico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computación");
        Categoria muebles = new Categoria("Muebles");

        Flux.just(electronico, deporte, computacion, muebles)
                .flatMap(service::saveCategoria)
                .doOnNext(c -> {
                    log.info("Categoría creada " + c.getNombre() + " " + c.getId());
                }).thenMany(Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
                new Producto("Sony Camara HD Digital", 177.89, electronico),
                new Producto("Apple Ipod", 46.89, electronico),
                new Producto("Sony Notebook", 200.89, computacion),
                new Producto("Acer Curve Screen", 150.78, muebles),
                new Producto("Niko Pro Microphone", 1052.87, deporte),
                new Producto("Bicicleta", 2552.87, deporte))
                .flatMap(producto -> {
                    producto.setCreateAt(new Date());
                    return service.save(producto);
                }))
                .subscribe(productoMono -> log.info("Insert: " + productoMono.getId() + " " + productoMono.getNombre()));
    }
}
