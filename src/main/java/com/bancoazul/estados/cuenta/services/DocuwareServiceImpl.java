package com.bancoazul.estados.cuenta.services;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.unbescape.json.JsonEscape;

import com.bancoazul.estados.cuenta.pojos.Documento;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class {@link DocuwareService} implementation
 *
 * @author Melvin Reyes
 */
@Service
@Slf4j
public class DocuwareServiceImpl implements DocuwareService {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

    @Value("${banco.azul.estados.cuenta.docuware.url}")
    private String baseUrl;
    @Value("${banco.azul.estados.cuenta.docuware.user}")
    private String user;
    @Value("${banco.azul.estados.cuenta.docuware.password}")
    private String password;

    @Override
    public boolean documentExist(Documento documento) {
        boolean response = false;
        try {
            response = checkExistSendPostRequest(documento);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String indexDocument(Documento documento) {
        String response = null;
        try {
            response = indexDocSendPostRequest(documento);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String downloadDocument(Documento documento) {
        String response = null;
        try {
            response = donwloadSendPostRequest(documento);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String indexDocSendPostRequest(Documento documento) throws IOException {
        HttpPost httpPost = composeHttPostRequest(documento, "IndexarDocumento");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            // Get json response from http
            JsonObject res = composeHttPostResponse(response);
            String codigo = res.get("codigo").getAsString();
            String mensaje = res.get("mensaje").getAsString();
            if (statusCode == 200 && "000".equals(codigo)) {
                log.info("Status code: " + statusCode);
            }
            return mensaje;
        }
    }

    private String donwloadSendPostRequest(Documento documento) throws IOException {
        HttpPost httpPost = composeHttPostRequest(documento, "DescargarDocumentos");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            // Get json response from http
            JsonObject res = composeHttPostResponse(response);
            String codigo = res.get("codigo").getAsString();
            String mensaje = res.get("mensaje").getAsString();
            // jsonObject.getAsJsonArray("documentos").get(0).getAsJsonObject().get("ContentType")
            if (statusCode == 200 && "000".equals(codigo)) {
                log.info("Status code: " + statusCode);
            } else {
                log.info("Status code: " + statusCode);
            }
            return mensaje;
        }
    }

    private boolean checkExistSendPostRequest(Documento documento) throws IOException {
        HttpPost httpPost = composeHttPostRequest(documento, "DescargarDocumentos");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            // Get json response from http
            JsonObject res = composeHttPostResponse(response);
            String codigo = res.get("codigo").getAsString();
            if (statusCode == 200 && "000".equals(codigo)) {
                return true;
            } else {
                log.info("Status code: " + statusCode);
                return false;
            }
        }
    }

    private HttpPost composeHttPostRequest(Documento documento, String endpoint) throws IOException {
        // Create payload from object class
        String jsonPayload = gson.toJson(documento);
        HttpPost httpPost = new HttpPost(baseUrl + endpoint);
        httpPost.setHeader("Content-Type", "application/json");
        // Set Basic Authentication credentials
        String credentials = user + ":" + password;
        String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        httpPost.setHeader("Authorization", "Basic " + base64Credentials);
        httpPost.setEntity(new StringEntity(jsonPayload));
        return httpPost;
    }

    private JsonObject composeHttPostResponse(CloseableHttpResponse response) throws IOException {
        // Get response body
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity, "UTF-8");
        // Create string builder, then delete first and last string characters
        // Must be a valid JSON string
        StringBuilder processResponseBody = new StringBuilder(responseBody);
        processResponseBody.deleteCharAt(0);
        processResponseBody.deleteCharAt(processResponseBody.length() - 1);
        // Remove escaping characters
        String cleanResponseBody = JsonEscape.unescapeJson(processResponseBody.toString());
        // Convert to JSON object
        return JsonParser.parseString(cleanResponseBody).getAsJsonObject();
    }
}
