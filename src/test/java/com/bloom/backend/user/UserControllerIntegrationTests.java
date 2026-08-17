package com.bloom.backend.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class UserControllerIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void onboardingAndProfileFollowAgreedContract() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"profile@example.com","password":"Password1!","nickname":"프로필"}
                                """))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"profile@example.com","password":"Password1!"}
                                """))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();

        mockMvc.perform(post("/api/v1/onboarding").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"birthDate":"1995-01-01","deliveryDate":"2026-06-01","heightCm":165.0,
                                 "weightKg":61.0,"beautyGoals":["DIET"],"healthIssues":["BACK_PAIN"],
                                 "focusAreas":["ABDOMEN","THIGH"],"recoveryAreas":["CORE","PELVIS"],
                                 "skinConcerns":["STRETCH_MARKS","LOSS_OF_ELASTICITY"],
                                 "lastPeriodDate":"2026-07-20","cycleLength":28}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.focusAreas[0]").value("ABDOMEN"))
                .andExpect(jsonPath("$.recoveryAreas[1]").value("PELVIS"));

        mockMvc.perform(patch("/api/v1/users/me/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weightKg\":60.5,\"focusAreas\":[],\"skinConcerns\":[\"DRYNESS\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(60.5))
                .andExpect(jsonPath("$.focusAreas").isEmpty())
                .andExpect(jsonPath("$.skinConcerns[0]").value("DRYNESS"));

        mockMvc.perform(get("/api/v1/users/me/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beautyGoals[0]").value("DIET"));

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"테스트\"}"))
                .andExpect(status().isNoContent());
    }
}
