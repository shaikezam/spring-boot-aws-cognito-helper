package com.shaizambrovski.cognitohelper.service;

import com.shaizambrovski.cognitohelper.model.CognitoAppClientDTO;

import java.util.List;

public interface IAWSCognitoService {
    CognitoAppClientDTO createAppClient();

    List<String> getUserPools();

    String generateAccessToken(String clientId) throws Exception;

    void createResourceServer();

    void createUserPools();

    void createDomain();
}
