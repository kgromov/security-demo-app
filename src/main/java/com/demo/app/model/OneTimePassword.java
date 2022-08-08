package com.demo.app.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
@Data
public class OneTimePassword {
    @Id
    @GeneratedValue
    private Long id;
    @Setter
    private String username;
    @Setter
    private String code;
}
