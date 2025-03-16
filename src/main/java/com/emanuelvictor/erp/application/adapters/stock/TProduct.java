package com.emanuelvictor.erp.application.adapters.stock;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "product")
public class TProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public TProduct(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public TProduct(String name) {
        this.name = name;
    }

}
