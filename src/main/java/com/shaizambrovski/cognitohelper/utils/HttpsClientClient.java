package com.shaizambrovski.cognitohelper.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class HttpsClientClient implements DisposableBean {

    private static HttpClient httpsClient;

    @Override
    public void destroy() throws Exception {
        ((CloseableHttpClient) httpsClient).close();
    }

    private static HttpClient getHttpsClientInstance() throws Exception {
        if (httpsClient == null) {
            synchronized (HttpsClientClient.class) {
                if (httpsClient == null) {
                    httpsClient = createHttpsClient();
                }
            }
        }
        return httpsClient;
    }

    public String executePostHttpsRequestWithBody(String url, String jsonData, Header[] headers) throws Exception {
        StringEntity requestEntity = new StringEntity(
                jsonData,
                ContentType.APPLICATION_JSON);
        HttpPost request = new HttpPost(url);
        request.setEntity(requestEntity);
        request.setHeaders(headers);

        return executeHttpsRequest(request);
    }

    public String executePostHttpsRequestWithoutBody(String url, Header[] headers) throws Exception {
        HttpPost request = new HttpPost(url);
        request.setHeaders(headers);

        return executeHttpsRequest(request);
    }

    public String executeGetHttpsRequest(String url) throws Exception {
        HttpUriRequest request = new HttpGet(url);

        return executeHttpsRequest(request);
    }

    private String executeHttpsRequest(HttpUriRequest httpUriRequest) throws Exception {
        HttpResponse response = getHttpsClientInstance().execute(httpUriRequest);

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    private static HttpClient createHttpsClient() throws Exception {
        return HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }


}
