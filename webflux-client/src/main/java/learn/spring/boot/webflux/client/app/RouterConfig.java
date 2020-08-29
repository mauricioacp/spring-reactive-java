package learn.spring.boot.webflux.client.app;

import learn.spring.boot.webflux.client.app.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

	@Bean
	public RouterFunction<ServerResponse>rutas(ProductoHandler handler){
		return route(GET("/api/client"),handler::listar)
				.andRoute(GET("/api/client/{id}"),handler::detalle)
				.andRoute(POST("/api/client"),handler::crear)
				.andRoute(PUT("/api/client/{id}"),handler::editar)
				.andRoute(DELETE("/api/client/{id}"),handler::eliminar)
				.andRoute(POST("/api/client/upload/{id}"),handler::upload);
	}
}
