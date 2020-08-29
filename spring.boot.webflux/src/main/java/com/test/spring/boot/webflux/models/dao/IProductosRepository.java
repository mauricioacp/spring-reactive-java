package com.test.spring.boot.webflux.models.dao;


import com.test.spring.boot.webflux.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IProductosRepository extends ReactiveMongoRepository<Producto,String> {


}
