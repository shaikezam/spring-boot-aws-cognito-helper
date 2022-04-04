package com.shaizambrovski.cognitohelper.controller;

import com.shaizambrovski.cognitohelper.model.CognitoAppClientDTO;
import com.shaizambrovski.cognitohelper.service.IAWSCognitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/web/api")
public class CognitoHelperController {

    @Autowired
    private IAWSCognitoService awsCognitoService;

    @PostMapping(value = "/user_pools")
    @ResponseBody
    public void createUserPools() {
        awsCognitoService.createUserPools();
    }

    @PostMapping(value = "/resource_server")
    @ResponseBody
    public void createResourceServer() {
        awsCognitoService.createResourceServer();
    }

    @GetMapping(value = "/user_pools")
    @ResponseBody
    public List<String> getUserPools() {
        return awsCognitoService.getUserPools();
    }

    @PostMapping(value = "/app_client")
    @ResponseBody
    public CognitoAppClientDTO createAppClient() {
        return awsCognitoService.createAppClient();
    }

    @PostMapping(value = "/domain")
    @ResponseBody
    public void createDomain() {
        awsCognitoService.createDomain();
    }

    @GetMapping(value = "/app_client_acess_token")
    @ResponseBody
    public String generateAppClientAccessToken(@RequestParam(value = "client_id") String clientId) throws Exception {
        return awsCognitoService.generateAccessToken(clientId);
    }
}
