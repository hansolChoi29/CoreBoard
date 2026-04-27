package com.example.coreboard.domain.users.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    protected Users() {
    }

    public Users(
            String username,
            String encodePassword,
            String email,
            String phoneNumber,
            UserRole role
    ) {
        this.username = username;
        this.password = encodePassword;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
    }

    public static Users createUsers(
            String username,
            String encodedPassword,
            String email,
            String phoneNumber
    ) {
        return new Users(
                username,
                encodedPassword,
                email,
                phoneNumber,
                UserRole.USER
        );
    }

    public UserRole getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
