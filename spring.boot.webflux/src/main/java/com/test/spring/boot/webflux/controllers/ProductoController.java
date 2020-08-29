package com.test.spring.boot.webflux.controllers;

import com.test.spring.boot.webflux.models.documents.Categoria;
import com.test.spring.boot.webflux.models.documents.Producto;
import com.test.spring.boot.webflux.models.services.IProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private IProductoService service;

    @Value("C:\\Users\\mauri\\OneDrive\\Pictures\\Uploads//")
    private String path;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categorias")
    public Flux<Categoria>categoriaFlux(){
        return service.findAllCategoria();
    }

    @GetMapping("/ver/{id}")
    public Mono<String>ver(Model model,@PathVariable String id){
        return service.findById(id)
                .doOnNext(p->{
                    model.addAttribute("producto",p);
                    model.addAttribute("titulo","Detalle Producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(p -> {
                    if(p.getId()==null){
                        return Mono.error(new InterruptedException(("No existe el Producto")));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }
    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>>verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
        Path ruta= Paths.get(path).resolve(nombreFoto).toAbsolutePath();
        Resource imagen=new UrlResource(ruta.toUri());
        return Mono.just(
                ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+imagen.getFilename()+"\"")
                .body(imagen)
        );
    }

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {

        Flux<Producto> productos = service.findAllConNombreUpperCase();
        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {

        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de Producto");
        model.addAttribute("boton", "crear");
        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable String id, Model model) {

        return service.findById(id).doOnNext(producto -> {
            log.info("Producto " + producto.getNombre());

            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("producto", producto);
            model.addAttribute("boton", "crear");
        }).defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(producto);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = service.findById(id).doOnNext(producto -> {
            log.info("Producto " + producto.getNombre());
        }).defaultIfEmpty(new Producto());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", productoMono);
        model.addAttribute("boton", "editar");

        return Mono.just("form");

    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid @ModelAttribute("producto") Producto producto,
                                BindingResult result, Model model,@RequestPart(name = "file") FilePart archivo, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Errores en formulario de Producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        } else {
            status.setComplete();
            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }

            Mono<Categoria>categoriaMono=service.findCategoriaById(producto.getCategoria().getId());

            return  categoriaMono.flatMap(c->{
                if (producto.getCreateAt() == null) {
                    producto.setCreateAt(new Date());
                }

                if(!archivo.filename().isEmpty()){
                    producto.setFoto(UUID.randomUUID().toString()+"-"+archivo.filename()
                            .replace("","")
                            .replace(":","")
                            .replace("\\","")
                    );
                }

                producto.setCategoria(c);
               return service.save(producto);
            }).doOnNext(producto1 -> {
                        log.info("Producto Almacenado: "
                                + producto1.getNombre() + " id " + producto1.getId());
                        log.info("Categoría Almacenada: "
                                + producto1.getCategoria().getNombre() + " id " + producto1.getCategoria().getId());
                    })
                    .flatMap(producto1 -> {
                        if(!archivo.filename().isEmpty()){
                           return archivo.transferTo(new File(path+producto1.getFoto()));

                        }
                        return Mono.empty();
                    })
                    .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return service.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(producto);
                })
                .flatMap(service::delete)
                .then(Mono.just("redirect:/listar?success=producto+eliminado+con+éxito"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {

        Flux<Producto> productos = service.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));
        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {

        Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar-chunked";
    }
}
