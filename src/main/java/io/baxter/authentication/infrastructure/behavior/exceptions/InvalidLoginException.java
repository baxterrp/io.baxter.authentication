package io.baxter.authentication.infrastructure.behavior.exceptions;

public class InvalidLoginException extends RuntimeException{
    public InvalidLoginException(){
        super("Unauthorized");
    }
}
