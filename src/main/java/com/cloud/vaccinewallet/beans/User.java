package com.cloud.vaccinewallet.beans;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String username;
    @NonNull
    private String usermail;
    @NonNull
    private String encryptedPassword;
    @NonNull
    private Boolean enabled;
    @Lob
    private Blob qr;

    public User(@NonNull String username,@NonNull String encryptedPassword,@NonNull String usermail, Boolean enabled) {
        this.username = username;
        this.usermail = usermail;
        this.encryptedPassword=encryptedPassword;
        this.enabled=enabled;
    }

    //User Information
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;

    private String middleName;
    @NonNull
    private Long phoneNumber;
    @NonNull
    private Integer age;
    @NonNull
    private String email;

   



    @ManyToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<Role>();


}
