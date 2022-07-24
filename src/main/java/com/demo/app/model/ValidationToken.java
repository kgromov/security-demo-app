package com.demo.app.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Getter
@NoArgsConstructor
@Data
public class ValidationToken {
    @Id
    private String id;
    @Setter
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @Setter
    private User user;
}
