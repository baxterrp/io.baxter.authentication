package io.baxter.authentication.data.models;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_roles")
public class UserRoleDataModel {
    @Column("user_id")
    private final Integer userId;

    @Column("role_id")
    private final Integer roleId;

    public UserRoleDataModel(Integer userId, Integer roleId){
        this.userId = userId;
        this.roleId = roleId;
    }

    public Integer getUserId() { return this.userId; }
    public Integer getRoleId() { return this.roleId; }
}
