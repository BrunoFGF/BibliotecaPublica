package com.backend.biblioteca.controller.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/web/libros";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "redirect:/web/libros";
    }
}