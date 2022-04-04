package com.shaizambrovski.cognitohelper.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaizambrovski.cognitohelper.model.CognitoAppClientDTO;
import com.shaizambrovski.cognitohelper.service.IAWSCognitoService;
import com.shaizambrovski.cognitohelper.utils.HttpsClientClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AWSCognitoService implements IAWSCognitoService, InitializingBean, DisposableBean {

    private static final String TOKEN_ENDPOINT = "https://%s.auth.us-east-1.amazoncognito.com/oauth2/token?grant_type=client_credentials&scope=%s";

    private CognitoIdentityProviderClient cognitoClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${pool.Name:tenant_v3}")
    private String cognitoPoolName;
    @Value("${cognito.resourceName:resource_name}")
    private String cognitoResourceName;
    @Value("${cognito.resourceIdentifier:scope_identifier}")
    private String cognitoResourceIdentifier;
    @Value("${cognito.scopeName:scope_name}")
    private String cognitoScopeName;
    @Value("${cognito.scopeDescription:scope_description}")
    private String cognitoScopeDescription;
    @Value("${cognito.appClientName:client_name}")
    private String cognitoClientName;
    @Value("${cognito.domainName:mgmt-to-illusive-saas}")
    private String cognitoDomainName;

    @Autowired
    private HttpsClientClient httpsClientClient;

    @Override
    public void afterPropertiesSet() {

        cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Override
    public void destroy() {
        cognitoClient.close();
    }

    @Override
    public void createUserPools() {
        cognitoClient.createUserPool(CreateUserPoolRequest.builder()
                .poolName(cognitoPoolName)
                .build());
    }

    @Override
    public void createResourceServer() {
        String userPoolId = getUserPoolId();

        cognitoClient.createResourceServer(CreateResourceServerRequest.builder()
                .userPoolId(userPoolId)
                .identifier(cognitoResourceIdentifier)
                .name(cognitoResourceName)
                .scopes(ResourceServerScopeType
                        .builder()
                        .scopeName(cognitoScopeName)
                        .scopeDescription(cognitoScopeDescription)
                        .build())
                .build());
    }

    @Override
    public CognitoAppClientDTO createAppClient() {
        String userPoolId = getUserPoolId();

        CreateUserPoolClientResponse response = cognitoClient.createUserPoolClient(
                CreateUserPoolClientRequest.builder()
                        .clientName(cognitoClientName)
                        .userPoolId(userPoolId)
                        .generateSecret(true)
                        .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
                        .allowedOAuthFlowsUserPoolClient(true)
                        .allowedOAuthScopes(cognitoResourceIdentifier + '/' + cognitoScopeName)
                        .build());

        return new CognitoAppClientDTO(response.userPoolClient().clientId(), response.userPoolClient().clientSecret()); // need to encrypt
    }

    @Override
    public void createDomain() {
        String userPoolId = getUserPoolId();

        cognitoClient.createUserPoolDomain(CreateUserPoolDomainRequest.builder()
                .userPoolId(userPoolId)
                .domain(cognitoDomainName)
                .build());
    }

    @Override
    public List<String> getUserPools() {
        return getUserPoolDescriptionType()
                .stream()
                .map(userPool -> userPool.name())
                .collect(Collectors.toList());
    }

    @Override
    public String generateAccessToken(String clientId) throws Exception {

        String userPoolId = getUserPoolId();

        DescribeUserPoolClientRequest describeUserPoolClientRequest = DescribeUserPoolClientRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .build();

        DescribeUserPoolClientResponse describeUserPoolClientResponse = cognitoClient.describeUserPoolClient(describeUserPoolClientRequest);

        String auth = describeUserPoolClientResponse.userPoolClient().clientId() + ":" + describeUserPoolClientResponse.userPoolClient().clientSecret();
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);

        String url = String.format(TOKEN_ENDPOINT, cognitoDomainName, (cognitoResourceIdentifier + '/' + cognitoScopeName));

        String token = httpsClientClient.executePostHttpsRequestWithoutBody(
                url,
                new Header[]{
                        new BasicHeader("authorization", authHeader),
                        new BasicHeader("content-type", "application/x-www-form-urlencoded")
                }
        );

        return token;
    }

    private List<UserPoolDescriptionType> getUserPoolDescriptionType() {
        ListUserPoolsRequest request = ListUserPoolsRequest.builder()
                .maxResults(10)
                .build();
        ListUserPoolsResponse response = cognitoClient.listUserPools(request);

        return response
                .userPools();
    }

    private String getUserPoolId() {
        return getUserPoolDescriptionType()
                .stream()
                .filter(userPoolName -> userPoolName.name().equalsIgnoreCase(cognitoPoolName))
                .map(userPoolDescriptionType -> userPoolDescriptionType.id())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("A user pool with the given %s isn't found", cognitoPoolName)));
    }

}
