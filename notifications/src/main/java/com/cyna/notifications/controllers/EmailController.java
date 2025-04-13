package com.cyna.notifications.controllers;

import com.cyna.notifications.services.JavaMailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/notification")
public class EmailController {

    @Autowired
    private JavaMailerService mailerService;

    @GetMapping("/email")
    public String email() {
         mailerService.sendEmail("kankeulandry22@gmail.com", "Test : Send simple message", " Tema, it's work!");
         return "OK";
    }


}
