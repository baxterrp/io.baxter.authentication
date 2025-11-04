package io.baxter.authentication.data.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
public class UserDataModel {
    @Id
    Integer id;

    @Column("user_id")
    UUID userId;

    @Column("username")
    String username;

    @Column("password")
    String password;

    public UserDataModel(String username, String password){
        this.username = username;
        this.password = password;
    }
}
