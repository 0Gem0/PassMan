package com.Passman.Manager.Landing.Controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainPageController {


    @GetMapping()
    private String getPage(){
        return "Main.html";
    }



}
