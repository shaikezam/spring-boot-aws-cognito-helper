package com.shaizambrovski.cognitohelper.controller;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.shaizambrovski.cognitohelper.model.CognitoAppClientDTO;
import com.shaizambrovski.cognitohelper.service.AWSCognitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/web/api")
public class CognitoHelperController {

    @Autowired
    private AWSCognitoService awsCognitoService;

    @PostMapping(value = "/user_pools")
    @ResponseBody()
    public String createUserPools(@RequestParam String userPoolName) {
        return awsCognitoService.createUserPools(userPoolName);
    }

    @PostMapping(value = "/resource_server")
    @ResponseBody
    public void createResourceServer(@RequestParam String resourceIdentifier,
                                     @RequestParam String resourceName,
                                     @RequestParam String scopeName,
                                     @RequestParam String scopeDescription,
                                     @RequestParam String userPoolId) {
        awsCognitoService.createResourceServer(resourceIdentifier, resourceName, scopeName, scopeDescription, userPoolId);
    }

    @PostMapping(value = "/app_client")
    @ResponseBody
    public CognitoAppClientDTO createAppClient(@RequestParam String appClientName,
                                               @RequestParam String resourceIdentifier,
                                               @RequestParam String scopeName,
                                               @RequestParam String userPoolId) {
        return awsCognitoService.createAppClient(appClientName, resourceIdentifier, scopeName, userPoolId);
    }

    @PostMapping(value = "/domain")
    @ResponseBody
    public void createDomain(@RequestParam String domainName,
                             @RequestParam String userPoolId) {
        awsCognitoService.createDomain(domainName, userPoolId);
    }

    @PostMapping(value = "/access_token")
    @ResponseBody
    public TokenResponse generateAppClientAccessToken(@RequestParam String url,
                                                      @RequestParam String scope,
                                                      @RequestBody CognitoAppClientDTO cognitoAppClientDTO) throws Exception {
        return awsCognitoService.generateAccessToken(url, scope, cognitoAppClientDTO);
    }
}
