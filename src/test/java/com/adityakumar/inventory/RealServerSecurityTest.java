package com.adityakumar.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs against a real embedded server. MockMvc skips the servlet error dispatch,
 * which is where a missing "/error" rule turns a 403 into a 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RealServerSecurityTest {

    @Autowired TestRestTemplate rest;

    private ResponseEntity<JsonNode> send(HttpMethod method, String path, String body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }

    @Test
    void normalUserGets403OnAdminEndpoint() {
        String name = "user" + UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<JsonNode> reg = send(HttpMethod.POST, "/api/auth/register",
                "{\"username\":\"" + name + "\",\"password\":\"password123\"}", null);
        assertEquals(201, reg.getStatusCode().value());
        String token = reg.getBody().get("token").asText();

        ResponseEntity<JsonNode> res = send(HttpMethod.POST, "/api/products",
                "{\"sku\":\"S-" + name + "\",\"name\":\"x\",\"price\":1,\"quantity\":1,\"reorderLevel\":1}", token);
        assertEquals(403, res.getStatusCode().value());
    }

    @Test
    void missingTokenIs401() {
        assertEquals(401, send(HttpMethod.GET, "/api/products", null, null).getStatusCode().value());
    }
}
