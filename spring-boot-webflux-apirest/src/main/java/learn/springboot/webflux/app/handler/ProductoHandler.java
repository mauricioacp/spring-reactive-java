package learn.springboot.webflux.app.handler;

import learn.springboot.webflux.app.models.documents.Categoria;
import learn.springboot.webflux.app.models.documents.Producto;
import learn.springboot.webflux.app.models.service.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;

import static org.springframework.web.reactive.function.BodyInserters.*;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class ProductoHandler {
	@Autowired
	private IProductoService service;
	@Value("C:\\Java-Projects\\Intellij-Projects\\Spring-boot-WebFlux\\image//")
	private String path;

	@Autowired
	private Validator validator;

	public Mono<ServerResponse> upload(ServerRequest request) {

		String id = request.pathVariable("id");
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(filePart -> service.findById(id).flatMap(p -> {
					p.setFoto(UUID.randomUUID().toString()+"-"+filePart.filename()
							.replace("","")
							.replace(":","")
							.replace("\\",""));
					return filePart.transferTo(new File(path + p.getFoto())).then(service.save(p));
				})).flatMap(p->ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	public Mono<ServerResponse> crearConFoto(ServerRequest request) {

		Mono<Producto>producto=request.multipartData().map(multipart->{
			FormFieldPart nombre= (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio= (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoriaId= (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaName= (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

			Categoria categoria=new Categoria(categoriaName.value());
			categoria.setId(categoriaId.value());
			return new Producto(nombre.value(),Double.parseDouble(precio.value()),categoria);

		});

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(filePart -> producto.flatMap(p -> {

					p.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
							.replace("","")
							.replace(":","")
							.replace("\\",""));

					p.setCreateAt(new Date());

					return filePart.transferTo(new File(path + p.getFoto())).then(service.save(p));
				})).flatMap(p->ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromObject(p)));

	}

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {

		String id = request.pathVariable("id");
		return service.findById(id).flatMap(p -> ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> {
			Object target;
			Errors errors=new BeanPropertyBindingResult(p, Producto.class.getName());
			validator.validate(p,errors);

			if (errors.hasErrors()){
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo "+fieldError.getField()+" "+fieldError.getDefaultMessage())
						.collectList()
						.flatMap(listaErrores->ServerResponse.badRequest().body(BodyInserters.fromValue(listaErrores)));
			}else {
				if (p.getCreateAt() == null) {
					p.setCreateAt(new Date());
				}
				return service.save(p).flatMap(pdb -> ServerResponse
						.created(URI.create("/api/v2/productos/".concat(pdb.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(pdb)));

			}

		});
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");

		Mono<Producto> productoDb = service.findById(id);

		return productoDb.zipWith(producto, (db, req) -> {
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());
			return db;
		}).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.save(p), Producto.class))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");

		Mono<Producto> productoDb = service.findById(id);

		return productoDb.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

}
