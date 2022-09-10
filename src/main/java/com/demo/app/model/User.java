package com.demo.app.model;

import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
@Builder
@Table(name = "Users", uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private boolean enabled;
    private boolean locked;
    private Instant createdAt;

    // This allows to remove the item from SQL (outside of the entityManager)
    // Alternatively FK definition can be provided explicitly in @CollectionTable:
    /*foreignKey = @ForeignKey(
            name = "fk_Authority_user",
            foreignKeyDefinition = "foreign key (authority_id) references Users (user_id) on delete cascade")*/
    // All listed relevant for tables either generated automatically or when cascade is not specified on migration level
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @CollectionTable(name = "user_authority", joinColumns = @JoinColumn(name = "user_id"))
    private Set<String> authorities = new HashSet<>();

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
