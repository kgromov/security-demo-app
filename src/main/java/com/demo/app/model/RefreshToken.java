package com.demo.app.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Data
public class RefreshToken {
    @Id
    private String id;
    @Setter
    private String token;
    @Setter
    private Instant  createdAt;

}
