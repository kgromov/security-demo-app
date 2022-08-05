package com.demo.app.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Data
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @Setter
    private User user;
}
