package com.example.coreboard.domain.auth.entity;

import com.example.coreboard.domain.users.entity.Users;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

public class RefreshToken {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column
    private String token;

    @ManyToOne
    @JoinColumn(name="user_id")
    private Users user;

    public RefreshToken(String token, Users user){
        this.token=token;
        this.user=user;
    }

    public String getToken(){
        return token;
    }

    public void updateToken(String token){
        this.token=token;
    }
}
