package io.baxter.authentication.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
public class UserDataModel {
    @Id
    Integer id;

    @Column("username")
    String username;

    @Column("password")
    String password;

    public UserDataModel(){}

    public UserDataModel(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){ return this.username; }
    public String getPassword(){ return this.password; }
    public Integer getId(){ return this.id; }
}
