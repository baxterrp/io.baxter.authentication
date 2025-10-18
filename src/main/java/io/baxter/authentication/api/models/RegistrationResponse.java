package io.baxter.authentication.api.models;

public class RegistrationResponse {
    String name;
    Integer id;

    public RegistrationResponse(String name, Integer id){
        this.name = name;
        this.id = id;
    }

    public String getName(){ return this.name; }
    public Integer getId(){ return this.id; }
}
