package spe.projectportfolio.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ReactController {
    @GetMapping(value = {"/", "/register", "/home", "/project", "/editor", "/project-editor", "/admin"})
    public String getHome() {
        return "index.html";
    }
}
