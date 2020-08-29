package com.test.spring.boot.webflux.models.dao;

import com.test.spring.boot.webflux.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ICatagoriaRepository  extends ReactiveMongoRepository<Categoria,String> {

}
