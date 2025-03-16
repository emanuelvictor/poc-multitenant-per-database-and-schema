package com.emanuelvictor.erp.domain.stock.domain.gateways;

import com.emanuelvictor.erp.domain.stock.domain.model.Product;

import java.util.List;

public interface ProductRepository {

    void insertNewProduct(Product product);

    List<Product> getAllProducts();
}
