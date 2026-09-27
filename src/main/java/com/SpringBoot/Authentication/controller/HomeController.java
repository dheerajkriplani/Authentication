package com.SpringBoot.Authentication.controller;

import com.SpringBoot.Authentication.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "Welcome to the Index Page!";
    }
    @GetMapping("/public/")
    public String home() {
        return "Welcome to the Home Page!";
    }
    @GetMapping("/public/data")
    public String data() {
        return "Data Page!";
    }
    @GetMapping("/admin/one")
    public String admin1() {
        return " Admin Page_1 !";
    }
    @GetMapping("/admin/two")
    public String admin2() {
//        User user= SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return " Admin Page_2 !";
    }
}
