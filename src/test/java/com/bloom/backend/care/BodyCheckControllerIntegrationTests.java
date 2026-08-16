package com.bloom.backend.care;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BodyCheckControllerIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createsListsUpdatesAndDeletesBodyCheck() throws Exception {
        String token = signupAndLogin();
        String created = mockMvc.perform(post("/api/v1/care/body-checks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recordedDate":"2026-08-16","originalImageUrl":"https://cdn.example.com/body/original.jpg"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analysisStatus").value("NOT_REQUESTED"))
                .andExpect(jsonPath("$.expectedImageUrl").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("bodyCheckId").asLong();

        mockMvc.perform(get("/api/v1/care/body-checks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bodyCheckId").value(id));

        mockMvc.perform(patch("/api/v1/care/body-checks/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordedDate\":\"2026-08-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordedDate").value("2026-08-15"));

        mockMvc.perform(delete("/api/v1/care/body-checks/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private String signupAndLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"body@example.com","password":"Password1!","nickname":"눈바디"}
                                """))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"body@example.com","password":"Password1!"}
                                """))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
