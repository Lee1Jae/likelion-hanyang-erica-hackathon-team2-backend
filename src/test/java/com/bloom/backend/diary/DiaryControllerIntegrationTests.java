package com.bloom.backend.diary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class DiaryControllerIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void savesFrontendFieldsAndCalculatesFlatDailyResponse() throws Exception {
        String accessToken = signupAndLogin();

        mockMvc.perform(put("/api/v1/diaries/2026-08-14")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo":"좋음","condition":4.5,"weight":61.8,"waterIntake":1500,
                                 "skinCondition":"DRY","menstrualStatus":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value(4.5))
                .andExpect(jsonPath("$.waterIntake").value(1500));

        mockMvc.perform(post("/api/v1/diaries/2026-08-14/meals")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mealType":"LUNCH","foodName":"현미밥","calories":320,
                                 "carbs":60,"protein":8,"fat":3}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/diaries/2026-08-14/activities")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"activityAmount":8200,"memo":"산책"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/diaries/2026-08-14")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(320))
                .andExpect(jsonPath("$.remainingCalories").value(1680))
                .andExpect(jsonPath("$.totalActivity").value(8200))
                .andExpect(jsonPath("$.carbs").value(60))
                .andExpect(jsonPath("$.protein").value(8))
                .andExpect(jsonPath("$.fat").value(3))
                .andExpect(jsonPath("$.meals[0].foodName").value("현미밥"))
                .andExpect(jsonPath("$.activities[0].activityAmount").value(8200));
    }

    private String signupAndLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"diary@example.com","password":"Password1!","nickname":"테스터"}
                                """))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"diary@example.com","password":"Password1!"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
