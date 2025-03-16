package com.emanuelvictor.erp.application.services.stock.getallproducts;

import com.emanuelvictor.erp.application.adapters.stock.TProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllProducts {
    private final TProductRepository tProductRepository;

    @GetMapping("products")
    public List<ProductDTO> getAllProducts() {
        return tProductRepository.findAll().stream().map(tProduct -> new ProductDTO(tProduct.getName())).toList();
    }
}
