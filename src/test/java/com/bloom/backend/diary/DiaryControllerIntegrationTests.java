package com.bloom.backend.diary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import java.util.UUID;

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
                                {"mealType":"LUNCH","foodName":"현미밥","kcal":320,
                                 "carbs":60,"protein":8,"fat":3}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/diaries/2026-08-14/activities")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"steps":8200,"exerciseMinutes":30,"burnedKcal":180,"memo":"산책"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/diaries/2026-08-14")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories").value(320))
                .andExpect(jsonPath("$.remainingCalories").value(1680))
                .andExpect(jsonPath("$.totalActivity").value(180))
                .andExpect(jsonPath("$.carbs").value(60))
                .andExpect(jsonPath("$.protein").value(8))
                .andExpect(jsonPath("$.fat").value(3))
                .andExpect(jsonPath("$.meals[0].foodName").value("현미밥"))
                .andExpect(jsonPath("$.activities[0].steps").value(8200))
                .andExpect(jsonPath("$.activities[0].exerciseMinutes").value(30))
                .andExpect(jsonPath("$.activities[0].burnedKcal").value(180));
    }

    @Test
    void agreedDailyContractSupportsPatchGetAndHistory() throws Exception {
        String accessToken = signupAndLogin();

        mockMvc.perform(patch("/api/v1/diary/daily")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-08-15","weightKg":61.2,"emotionScore":5,"bodyScore":3,
                                 "emotionTags":["HAPPY","STRESS"],"bodyTags":["LOWER_BACK_PAIN"],
                                 "waterMl":1800,"skin":["DRY","SENSITIVE"],
                                 "periodStart":"2026-08-14","periodEnd":"2026-08-18","memo":"회복 중"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(61.2))
                .andExpect(jsonPath("$.waterMl").value(1800))
                .andExpect(jsonPath("$.skin[1]").value("SENSITIVE"));

        mockMvc.perform(post("/api/v1/diaries/2026-08-15/activities")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"steps":6500,"exerciseMinutes":25,"burnedKcal":160,"memo":"걷기"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/diary/daily").param("date", "2026-08-15")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emotionScore").value(5))
                .andExpect(jsonPath("$.bodyScore").value(3))
                .andExpect(jsonPath("$.emotionTags[1]").value("STRESS"))
                .andExpect(jsonPath("$.memo").value("회복 중"))
                .andExpect(jsonPath("$.totalSteps").value(6500))
                .andExpect(jsonPath("$.totalExerciseMinutes").value(25))
                .andExpect(jsonPath("$.totalBurnedKcal").value(160));

        mockMvc.perform(get("/api/v1/diary/history")
                        .param("from", "2026-08-01").param("to", "2026-08-31")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-08-15"))
                .andExpect(jsonPath("$[0].weightKg").value(61.2));
    }

    @Test
    void rejectsUnknownConditionTag() throws Exception {
        String accessToken = signupAndLogin();

        mockMvc.perform(patch("/api/v1/diary/daily")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-08-16","emotionTags":["UNKNOWN_EMOTION"]}
                                """))
                .andExpect(status().isBadRequest());
    }

    private String signupAndLogin() throws Exception {
        String email = "diary-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password1!","nickname":"테스터"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password1!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
