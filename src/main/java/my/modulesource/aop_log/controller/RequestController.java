package my.modulesource.aop_log.controller;


import my.modulesource.aop_log.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class RequestController {


    @PostMapping("/get")
    public ResponseEntity<String> getMethod(@Valid @RequestBody UserDto user){
        return ResponseEntity.status(200).body("get");
    }



}
