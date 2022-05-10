package com.shaizambrovski.cognitohelper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.shaizambrovski.cognitohelper.model.CognitoAppClientDTO;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.List;

@Service
public class AWSCognitoService implements InitializingBean, DisposableBean {

    private CognitoIdentityProviderClient cognitoClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.region:us-east-1}")
    private String AWS_REGION;

    @Override
    public void afterPropertiesSet() {
        cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(AWS_REGION))
                .build();
    }

    @Override
    public void destroy() {
        cognitoClient.close();
    }

    public String createUserPools(String userPoolName) {
        CreateUserPoolResponse createUserPoolResponse = cognitoClient.createUserPool(CreateUserPoolRequest.builder()
                .poolName(userPoolName)
                .build());

        return createUserPoolResponse.userPool().id();
    }

    public void createResourceServer(String resourceIdentifier, String resourceName, String scopeName, String scopeDescription, String userPoolId) {

        cognitoClient.createResourceServer(CreateResourceServerRequest.builder()
                .userPoolId(userPoolId)
                .identifier(resourceIdentifier)
                .name(resourceName)
                .scopes(ResourceServerScopeType
                        .builder()
                        .scopeName(scopeName)
                        .scopeDescription(scopeDescription)
                        .build())
                .build());
    }

    public CognitoAppClientDTO createAppClient(String appClientName, String resourceIdentifier, String scopeName, String userPoolId) {

        CreateUserPoolClientResponse response = cognitoClient.createUserPoolClient(
                CreateUserPoolClientRequest.builder()
                        .clientName(appClientName)
                        .userPoolId(userPoolId)
                        .generateSecret(true)
                        .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
                        .allowedOAuthFlowsUserPoolClient(true)
                        .allowedOAuthScopes(resourceIdentifier + '/' + scopeName)
                        .build());

        return new CognitoAppClientDTO(response.userPoolClient().clientId(), response.userPoolClient().clientSecret()); // need to encrypt
    }

    public void createDomain(String domainName, String userPoolId) {

        cognitoClient.createUserPoolDomain(CreateUserPoolDomainRequest.builder()
                .userPoolId(userPoolId)
                .domain(domainName)
                .build());
    }


    public TokenResponse generateAccessToken(String url, String scope, CognitoAppClientDTO cognitoAppClientDTO) throws Exception {
        return new ClientCredentialsTokenRequest(new NetHttpTransport(), new GsonFactory(),
                new GenericUrl(url))
                .setScopes(List.of(scope))
                .setClientAuthentication(new BasicAuthentication(cognitoAppClientDTO.getClientId(), cognitoAppClientDTO.getClientSecret()))
                .execute();
    }
}
