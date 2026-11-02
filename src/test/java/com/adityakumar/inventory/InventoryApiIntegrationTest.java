package com.adityakumar.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static String uid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String adminToken() throws Exception {
        return login("admin", "admin12345");
    }

    private String userToken() throws Exception {
        String name = "user" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult r = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + name + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String user, String pass) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private static String productJson(String sku, int qty) {
        return "{\"sku\":\"" + sku + "\",\"name\":\"Mouse\",\"price\":19.99,\"quantity\":" + qty + ",\"reorderLevel\":5}";
    }

    private long createProduct(String token, String sku, int qty) throws Exception {
        MvcResult r = mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(productJson(sku, qty)))
                .andExpect(status().isCreated()).andReturn();
        JsonNode node = json.readTree(r.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void normalUserCannotCreateProducts() throws Exception {
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content(productJson("SKU-U-" + uid(), 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAndReadProduct() throws Exception {
        String token = adminToken();
        long id = createProduct(token, "SKU-A-" + uid(), 20);
        mvc.perform(get("/api/products/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void duplicateSkuReturnsConflict() throws Exception {
        String token = adminToken();
        String sku = "SKU-D-" + uid();
        createProduct(token, sku, 1);
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(productJson(sku, 1)))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidProductReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"name\":\"x\",\"price\":-1,\"quantity\":1,\"reorderLevel\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void userCanAdjustStockAndOverdrawIsRejected() throws Exception {
        long id = createProduct(adminToken(), "SKU-S-" + uid(), 10);
        String user = userToken();

        mvc.perform(post("/api/products/" + id + "/stock").header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OUT\",\"quantity\":8,\"note\":\"sale\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.lowStock").value(true));

        mvc.perform(post("/api/products/" + id + "/stock").header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OUT\",\"quantity\":3}"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/products/" + id + "/movements").header("Authorization", "Bearer " + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listIsPaginated() throws Exception {
        String token = adminToken();
        for (int i = 0; i < 3; i++) {
            createProduct(token, "SKU-P-" + uid(), 10);
        }
        mvc.perform(get("/api/products?size=2&page=0").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2));
    }
}
