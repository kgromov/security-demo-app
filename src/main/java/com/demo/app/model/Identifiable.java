package com.demo.app.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public abstract class Identifiable {
    @Id
    private String id;

    public Identifiable(String id) {
        this.id = UUID.randomUUID().toString();
    }
}
