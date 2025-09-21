package com.example.coreboard.domain.auth.entity;

import jakarta.persistence.*;


@Entity
@Table(name="sign_in")
public class SignIn {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", nullable=false, unique=true)
    private String username;

    @Column(name="password", nullable=false)
    private String password;

    protected SignIn(){}

    public SignIn(String username, String password){
        this.username=username;
        this.password=password;
    }
    public String getUsername(){return username;}
    public String getPassword(){return password;}
}
