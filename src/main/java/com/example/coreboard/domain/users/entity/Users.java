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

    @Column(nullable = false)
    private String salt; // Base64 문자열

    protected Users() {
    }

    public Users(String username, String encodePassword, String email, String phoneNumber, String base64Salt) {
        this.username = username;
        this.password = encodePassword;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.salt = base64Salt;
    }

    public static Users createUsers(
            String username,
            String encodedPassword,
            String email,
            String phoneNumber,
            String base64Salt
    ) {
        return new Users(username, encodedPassword, email, phoneNumber, base64Salt);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String  getSalt() {
        return salt;
    }
}
