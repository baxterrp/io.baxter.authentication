package io.baxter.authentication.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("roles")
public class RoleDataModel {
    @Id
    private final Integer id;

    @Column("name")
    private final String name;

    public RoleDataModel(Integer id, String name){
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return this.id; }
    public String getName() { return this.name; }
}
