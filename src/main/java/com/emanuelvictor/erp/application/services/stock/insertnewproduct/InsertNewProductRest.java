package com.emanuelvictor.erp.application.services.stock.insertnewproduct;

import com.emanuelvictor.erp.application.adapters.stock.TProduct;
import com.emanuelvictor.erp.application.adapters.stock.TProductRepository;
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

    private final TProductRepository productRepository;

    @Transactional
    @PostMapping("products")
    public ResponseEntity<ProductDTO> insertNewProduct(@RequestBody ProductDTO productDTO) {
        final var productTable = new TProduct(productDTO.name());
        productRepository.save(productTable);
        return new ResponseEntity<>(productDTO, CREATED);
    }
}
