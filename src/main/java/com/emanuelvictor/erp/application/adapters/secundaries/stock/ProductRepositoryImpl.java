package com.emanuelvictor.erp.application.adapters.secundaries.stock;

import com.emanuelvictor.erp.domain.stock.Product;
import com.emanuelvictor.erp.domain.stock.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final TProductRepository tProductRepository;

    @Override
    public void addProduct(Product product) {
        tProductRepository.save(new TProduct(product.getName()));
    }

    @Override
    public List<Product> getAllProducts() {
        return tProductRepository.findAll().stream().map(tProduct -> new Product(tProduct.getName())).toList();
    }
}
