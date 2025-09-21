package com.example.coreboard.domain.auth.Entity;

import jakarta.persistence.*;



@Entity
@Table(name="sign_in")
public class SignIn {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="username")
    private String username;

    @Column(name="password")
    private String password;
}
