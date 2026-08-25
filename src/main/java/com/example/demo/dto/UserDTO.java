package com.example.demo.dto;

import javax.validation.constraints.NotBlank;


public class UserDTO {
    private Long id;
    private String username;
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String password;
    private String role;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String firstName, String lastName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class UserDTO {
//     private Long id;

//     @NotBlank(message = "Username is required")
//     private String username;

//     @Email(message = "Email should be valid")
//     @NotBlank(message = "Email is required")
//     private String email;

//     @NotBlank(message = "Password is required")
//     private String password;

//     @NotBlank(message = "First name is required")
//     private String firstName;

//     @NotBlank(message = "Last name is required")
//     private String lastName;

//     private Role role;
//     private Boolean active;
//     private Date createdAt;
//     private Date updatedAt;
// }
