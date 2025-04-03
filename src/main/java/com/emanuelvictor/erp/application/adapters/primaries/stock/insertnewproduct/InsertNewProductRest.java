package com.emanuelvictor.erp.application.adapters.primaries.stock.insertnewproduct;

import com.emanuelvictor.erp.domain.stock.Product;
import com.emanuelvictor.erp.domain.stock.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class InsertNewProductRest {

    private final ProductRepository productRepository;

    @Transactional
    @PostMapping("products")
    public ResponseEntity<ProductDTO> insertNewProduct(@RequestBody ProductDTO productDTO) {
        final var product = new Product(productDTO.name());
        productRepository.addProduct(product);
        return new ResponseEntity<>(productDTO, CREATED);
    }
}
