package com.example.websocektpratice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloWorldPageController {

    @GetMapping("/hello")
        public String hello(Model model) {
        model.addAttribute("message", "Hello, World!");
        return "hello";
    }
    @GetMapping("/chat")
    public String chat(Model model) {
        model.addAttribute("message", "Hello, World!");
        return "chat";
    }
}
