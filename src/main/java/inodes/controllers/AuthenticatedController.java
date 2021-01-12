package inodes.controllers;

import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

public class AuthenticatedController {

    @ModelAttribute("loggedinuser")
    public String getUser(HttpServletRequest request) {
        return (String)request.getAttribute("loggedinuser");
    }
}
