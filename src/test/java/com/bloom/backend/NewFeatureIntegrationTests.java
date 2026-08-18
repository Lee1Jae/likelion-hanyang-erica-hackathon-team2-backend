package com.bloom.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NewFeatureIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void uploadsPrivateImageAndReturnsAuthenticatedUrl() throws Exception {
        String token = signupAndLogin("upload@example.com");
        MockMultipartFile image = new MockMultipartFile("file", "body.jpg", "image/jpeg",
                new byte[]{(byte) 0xff, (byte) 0xd8, 1, 2, 3});
        String response = mockMvc.perform(multipart("/api/v1/uploads/images")
                        .file(image).param("purpose", "BODY_CHECK")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.size").value(5))
                .andReturn().getResponse().getContentAsString();
        String imageUrl = objectMapper.readTree(response).get("imageUrl").asText();
        String path = imageUrl.substring(imageUrl.indexOf("/api/v1"));
        mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().bytes(image.getBytes()));
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void attendanceIsRewardedOnceAndHistoryIsServerOwned() throws Exception {
        String token = signupAndLogin("mileage@example.com");
        mockMvc.perform(post("/api/v1/mileage/attendance").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rewarded").value(true))
                .andExpect(jsonPath("$.amount").value(100));
        mockMvc.perform(post("/api/v1/mileage/attendance").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rewarded").value(false))
                .andExpect(jsonPath("$.reason").value("ALREADY_REWARDED"));
        mockMvc.perform(get("/api/v1/mileage").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(100));
        mockMvc.perform(get("/api/v1/mileage/history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].reason").value("ATTENDANCE"));
    }

    @Test
    void aiPublicApisDoNotReturnFakeResults() throws Exception {
        String token = signupAndLogin("ai-care@example.com");
        String bodyCheck = mockMvc.perform(post("/api/v1/care/body-checks")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordedDate\":\"2026-08-17\",\"originalImageUrl\":\"https://example.com/body.jpg\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long bodyCheckId = objectMapper.readTree(bodyCheck).get("bodyCheckId").asLong();
        mockMvc.perform(post("/api/v1/ai/procedures/recommendations")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyCheckId\":" + bodyCheckId + "}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_SERVICE_UNAVAILABLE"));
        mockMvc.perform(post("/api/v1/ai/reports")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"2026-08-10\",\"to\":\"2026-08-16\"}"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/api/v1/ai/reports/latest").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void periodRecordsSupportOwnedCrud() throws Exception {
        String token = signupAndLogin("period@example.com");
        String created = mockMvc.perform(post("/api/v1/periods")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-08-18\",\"endDate\":\"2026-08-22\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate").value("2026-08-18"))
                .andReturn().getResponse().getContentAsString();
        long periodId = objectMapper.readTree(created).get("periodId").asLong();

        mockMvc.perform(get("/api/v1/periods").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].periodId").value(periodId));
        mockMvc.perform(patch("/api/v1/periods/{periodId}", periodId)
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endDate\":\"2026-08-23\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.endDate").value("2026-08-23"));
        mockMvc.perform(delete("/api/v1/periods/{periodId}", periodId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/periods").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    private String signupAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1!\",\"nickname\":\"테스트\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(login).get("accessToken").asText();
    }
}
