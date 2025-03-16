package com.emanuelvictor.erp.application.services.stock.insertnewproduct;

import com.emanuelvictor.erp.application.adapters.stock.TProduct;
import com.emanuelvictor.erp.application.adapters.stock.TProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InsertNewProduct {
    private final TProductRepository tProductRepository;

    @Transactional
    @PostMapping("products")
    public ProductDTO insertNewProduct(@RequestBody ProductDTO productDTO) {
        final var productTable = new TProduct(productDTO.name());
        tProductRepository.save(productTable);
        return productDTO;
    }
}
