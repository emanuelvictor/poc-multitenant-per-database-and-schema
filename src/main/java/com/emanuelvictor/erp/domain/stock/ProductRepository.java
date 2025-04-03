package com.emanuelvictor.erp.domain.stock;

import java.util.List;

public interface ProductRepository {

    void addProduct(Product product);

    List<Product> getAllProducts();
}
