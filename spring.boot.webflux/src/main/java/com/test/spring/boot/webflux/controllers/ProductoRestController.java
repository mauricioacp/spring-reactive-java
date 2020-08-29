package com.test.spring.boot.webflux.controllers;


import com.test.spring.boot.webflux.models.dao.IProductosRepository;
import com.test.spring.boot.webflux.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private IProductosRepository dao;

    private static final Logger log= LoggerFactory.getLogger(ProductoRestController.class);

    @GetMapping()
    public Flux<Producto>index(){

        Flux<Producto>productos=dao.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        }).doOnNext(product -> log.info(product.getNombre()));

        return productos;
    }

    @GetMapping("/{id}")
    public Mono<Producto>show(@PathVariable String id){
        Flux<Producto>productos=dao.findAll();
        Mono<Producto>producto=productos.filter(producto1 -> producto1.getId().equals(id))
                .next().doOnNext(product -> log.info(product.getNombre()));

        return dao.findById(id);
    }

}
