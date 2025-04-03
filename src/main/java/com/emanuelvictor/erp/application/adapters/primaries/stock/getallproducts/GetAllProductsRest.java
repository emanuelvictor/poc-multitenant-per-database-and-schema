package com.emanuelvictor.erp.application.adapters.primaries.stock.getallproducts;

import com.emanuelvictor.erp.domain.stock.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllProductsRest {

    private final ProductRepository productRepository;

    @Transactional
    @GetMapping("products")
    public List<ProductDTO> getAllProducts() {
        return productRepository.getAllProducts().stream()
                .map(product -> new ProductDTO(product.getName())).toList();
    }
}
