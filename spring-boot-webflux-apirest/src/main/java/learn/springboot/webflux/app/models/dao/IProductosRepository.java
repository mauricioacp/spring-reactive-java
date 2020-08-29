package learn.springboot.webflux.app.models.dao;


import learn.springboot.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IProductosRepository extends ReactiveMongoRepository<Producto, String> {

	public Mono<Producto> findByNombre(String nombre);

	//Usando el lenguaje de Mongo
	@Query("{'nombre:'?0}")
	public Mono<Producto> obtenerPorNombre(String nombre);

}
