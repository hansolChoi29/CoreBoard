package com.example.coreboard.domain.users.entity;

import jakarta.persistence.*;


@Entity
@Table(name="sign_in")
public class Users {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", nullable=false, unique=true)
    private String username;

    @Column(name="password", nullable=false)
    private String password;

    @Column(name="email")
    private String email;

    @Column(name="phone_number")
    private String phoneNumber;


    protected Users(){}

    public Users(String username, String encodePassword, String email, String phoneNumber){
        this.username=username;
        this.password=encodePassword;
        this.phoneNumber=phoneNumber;
        this.email=email;
    }

    public Users(String username, String encodePassowrd) {
        this.username=username;
        this.password=encodePassowrd;

    }

    public void  setEmail(String email){
        this.email=email;
    }
    public void  setPhoneNumber(String phoneNumber){
        this.phoneNumber=phoneNumber;
    }

    public String getUsername(){return username;}
    public String getPassword(){return password;}

    public Long getId() {
        return id;
    }
}
