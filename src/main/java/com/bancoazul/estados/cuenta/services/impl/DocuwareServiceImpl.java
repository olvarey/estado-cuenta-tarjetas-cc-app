package com.bancoazul.estados.cuenta.services.impl;

import com.bancoazul.estados.cuenta.pojos.Documento;
import com.bancoazul.estados.cuenta.services.DocuwareService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.unbescape.json.JsonEscape;

import java.io.IOException;

/**
 * Service class {@link DocuwareService} implementation
 *
 * @author Melvin Reyes
 */
@Service
public class DocuwareServiceImpl implements DocuwareService {

    private static final Logger LOGGER = LogManager.getLogger(DocuwareServiceImpl.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();
    private static final String CODE = "codigo";
    private static final String MESSAGE = "mensaje";

    @Value("${banco.azul.estados.cuenta.docuware.url}")
    private String baseUrl;
    @Value("${banco.azul.estados.cuenta.docuware.user}")
    private String user;
    @Value("${banco.azul.estados.cuenta.docuware.password}")
    private String password;

    /**
     * Checks if a document exists.
     *
     * @param documento The document to check for existence
     * @return true if the document exists, false otherwise
     */
    @Override
    public boolean documentExist(Documento documento) {
        boolean response = false;
        try {
            // Check if the document exists by sending a POST request
            response = checkExistSendPostRequest(documento);
        } catch (IOException e) {
            // Print the stack trace if an IOException occurs
            LOGGER.debug("Error in documentExist method: ", e);
        }
        return response;
    }

    /**
     * Indexes a document and returns the response.
     *
     * @param documento The document to be indexed
     * @return The response from indexing the document
     */
    @Override
    public String indexDocument(Documento documento) {
        String response = null;
        try {
            response = indexDocSendPostRequest(documento);
        } catch (IOException e) {
            LOGGER.debug("Error in indexDocument method: ", e);
        }
        return response;
    }

    /**
     * Downloads a document by sending a POST request.
     *
     * @param documento The document to be downloaded
     * @return The response from the download request
     */
    @Override
    public String downloadDocument(Documento documento) {
        String response = null;
        try {
            // Send a POST request to download the document
            response = downloadSendPostRequest(documento);
        } catch (IOException e) {
            // Print the stack trace in case of an IOException
            LOGGER.debug("Error in downloadDocument method: ", e);
        }
        return response;
    }

    /**
     * Sends a POST request to index a document and returns a message.
     *
     * @param documento The document to be indexed
     * @return A message indicating the result of the indexing process
     * @throws IOException If an I/O error occurs
     */
    private String indexDocSendPostRequest(Documento documento) throws IOException {
        // Compose HTTP POST request
        HttpPost httpPost = composeHttpPostRequest(documento, "IndexarDocumento");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();

            // Get JSON response from HTTP
            JsonObject res = composeHttpPostResponse(response);
            String codigo = res.get(CODE).getAsString();
            String mensaje = res.get(MESSAGE).getAsString();

            // Log success if status code is 200 and code is "000"
            if (statusCode == 200 && "000".equals(codigo)) {
                LOGGER.debug("Document indexed successfully. Status code: {}", statusCode);
            }

            return mensaje;
        }
    }

    /**
     * Downloads the document by sending a POST request.
     *
     * @param documento The document to be downloaded
     * @return The message received from the server
     * @throws IOException If an I/O error occurs
     */
    private String downloadSendPostRequest(Documento documento) throws IOException {
        // Compose the HTTP POST request
        HttpPost httpPost = composeHttpPostRequest(documento, "DescargarDocumentos");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            // Get json response from http
            JsonObject res = composeHttpPostResponse(response);
            String codigo = res.get(CODE).getAsString();
            String mensaje = res.get(MESSAGE).getAsString();
            // jsonObject.getAsJsonArray("documentos").get(0).getAsJsonObject().get("ContentType")
            if (statusCode == 200 && "000".equals(codigo)) {
                LOGGER.debug("Document downloaded successfully. Status code: {}", statusCode);
            } else {
                LOGGER.debug("Something went wrong. Status code: {}", statusCode);
            }
            return mensaje;
        }
    }

    /**
     * Checks the existence of a document by sending a POST request.
     *
     * @param documento The document to check
     * @return true if the document exists, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean checkExistSendPostRequest(Documento documento) throws IOException {
        // Compose HTTP POST request
        HttpPost httpPost = composeHttpPostRequest(documento, "DescargarDocumentos");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();

            // Get JSON response from HTTP
            JsonObject res = composeHttpPostResponse(response);
            String codigo = res.get(CODE).getAsString();

            // Check if the response indicates success
            if (statusCode == 200 && "000".equals(codigo)) {
                return true;
            } else {
                LOGGER.debug("Document does not exist. Status code: {}", statusCode);
                return false;
            }
        }
    }

    /**
     * Composes an HTTP POST request with the given document and endpoint.
     *
     * @param documento The document object to be sent in the request body.
     * @param endpoint  The endpoint to which the request will be sent.
     * @return The HttpPost request object ready to be executed.
     * @throws IOException If an I/O error occurs while creating the request.
     */
    private HttpPost composeHttpPostRequest(Documento documento, String endpoint) throws IOException {
        // Create payload from object class
        String jsonPayload = gson.toJson(documento);

        // Create the HttpPost request
        HttpPost httpPost = new HttpPost(baseUrl + endpoint);

        // Set request headers
        httpPost.setHeader("Content-Type", "application/json");

        // Set Basic Authentication credentials
        String credentials = user + ":" + password;
        String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        httpPost.setHeader("Authorization", "Basic " + base64Credentials);

        // Set request entity with the JSON payload
        httpPost.setEntity(new StringEntity(jsonPayload));

        return httpPost;
    }

    /**
     * Composes an HTTP POST response from the CloseableHttpResponse object
     *
     * @param response the CloseableHttpResponse object
     * @return the JSON object representing the HTTP POST response
     */
    private JsonObject composeHttpPostResponse(CloseableHttpResponse response) throws IOException {
        // Get response body
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity, "UTF-8");
        // Remove the first and last characters to ensure valid JSON string
        StringBuilder processResponseBody = new StringBuilder(responseBody);
        processResponseBody.deleteCharAt(0);
        processResponseBody.deleteCharAt(processResponseBody.length() - 1);
        // Remove escaping characters
        String cleanResponseBody = JsonEscape.unescapeJson(processResponseBody.toString());
        // Convert to JSON object
        return JsonParser.parseString(cleanResponseBody).getAsJsonObject();
    }
}
