package com.fitworkup.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Imports necessários para a segurança do Spring
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // CORREÇÃO AQUI: Assinando o contrato de segurança

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;
    
    @Column(nullable = false)
    private Integer xp = 0;

    @Column(nullable = false)
    private int level = 1;

    @Column(name = "fit_coins", nullable = false)
    private Integer fitcoins = 0;

    @Column(nullable = false)
    private Integer streak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(nullable = false, length = 50, name = "avatar_border")
    private String avatarBorder = "DEFAULT";

    @Column(nullable = false, length = 50, name = "prestige_title")
    private String prestigeTitle = "NOVATO";

    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements = new ArrayList<>();

    // ==========================================
    // IMPLEMENTAÇÃO DOS MÉTODOS USERDETAILS
    // ==========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Define o nível de acesso do usuário dentro do ecossistema FitWorkUp
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // O Spring Security usa o e-mail como identificador único de login no nosso ecossistema
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override 
    public boolean isAccountNonExpired() { 
        return true; 
    }

    @Override 
    public boolean isAccountNonLocked() { 
        return true; 
    }

    @Override 
    public boolean isCredentialsNonExpired() { 
        return true; 
    }

    @Override 
    public boolean isEnabled() { 
        return true; 
    }
}