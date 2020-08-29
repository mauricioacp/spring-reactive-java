package learn.springboot.webflux.app;

import learn.springboot.webflux.app.models.documents.Categoria;
import learn.springboot.webflux.app.models.documents.Producto;
import learn.springboot.webflux.app.models.service.IProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment =SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private IProductoService service;

	@Autowired
	private WebTestClient client;

	@Test
	void listarTest() {
		client.get()
				.uri("/api/v2/productos").accept(MediaType.APPLICATION_JSON)
				.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith(response->{
			List<Producto>productos=response.getResponseBody();
			productos.forEach(p->{
				System.out.println(p.getNombre());
			});
			Assertions.assertEquals(8, productos.size());
		});

		//.hasSize(7);
	}
	@Test
	void verTest() {
		Producto producto=service.findByNombre("TV Panasonic Pantalla LCD").block();

		client.get()
				.uri("/api/v2/productos/{id}", Collections.singletonMap("id",producto.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");

	}

	@Test
	public void crearTest(){

		Categoria categoria=service.findCategoriaByNombre("Muebles").block();
		Producto producto=new Producto("Mesa Comedor",100.00,categoria);
		client.post().uri("/api/v2/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto),Producto.class)
		.exchange().expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Mesa Comedor")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}
	@Test
	public void editarTest(){
		Producto producto=service.findByNombre("Apple Ipod").block();
		Categoria categoria=service.findCategoriaByNombre("Electrónico").block();
		Producto productoEditado=new Producto("Asus Notebook",700.00,categoria);

		client.put().uri("/api/productos/{id}",Collections.singletonMap("id",producto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado),Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Asus Notebook")
				.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}

	@Test
	public void eliminarTest(){
		Producto producto=service.findByNombre("Bicicleta").block();
		client.delete().uri("/api/productos/{id}",Collections.singletonMap("id",producto.getId()))
		.exchange()
		.expectStatus()
				.isNoContent()
		.expectBody()
				.isEmpty();

		client.get().uri("/api/productos/{id}",Collections.singletonMap("id",producto.getId()))
				.exchange()
				.expectStatus()
				.isNotFound()
				.expectBody()
				.isEmpty();
	}

}
