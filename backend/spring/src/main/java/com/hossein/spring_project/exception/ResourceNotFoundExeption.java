package com.hossein.spring_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ResourceNotFoundExeption extends RuntimeException{
    public ResourceNotFoundExeption(String message){
        super(message);
    }
}
