package io.baxter.authentication.infrastructure.behavior.exceptions;

public class ResourceExistsException extends RuntimeException {
    public ResourceExistsException(String resourceType, String resourceValue){
        super(String.format("%s already exists with value %s", resourceType, resourceValue));
    }
}
