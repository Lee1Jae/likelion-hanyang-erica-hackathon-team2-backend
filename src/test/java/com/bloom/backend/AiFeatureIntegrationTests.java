package com.bloom.backend;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bloom.backend.ai.client.OpenAiResponsesClient;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AiFeatureIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OpenAiResponsesClient aiClient;

    @BeforeEach
    void configureAiResponses() throws Exception {
        when(aiClient.model()).thenReturn("test-model");
        when(aiClient.text(anyString(), any())).thenReturn("오늘은 가벼운 걷기와 스트레칭을 추천해요.");
        when(aiClient.structured(anyString(), any(), eq("nutrition_analysis"), any(JsonNode.class)))
                .thenReturn(objectMapper.readTree("""
                        {"foods":[{"foodName":"현미밥","amount":150,"amountUnit":"g","kcal":310,
                        "carbs":66,"protein":6,"fat":2,"confidence":0.91}]}
                        """));
        when(aiClient.structured(anyString(), any(), eq("health_report"), any(JsonNode.class)))
                .thenReturn(objectMapper.readTree("""
                        {"summary":"활동량이 늘었습니다.",
                        "priorities":[{"title":"수분","description":"물을 나누어 마셔보세요."}],
                        "methods":[{"title":"걷기","description":"가볍게 걸어보세요."}]}
                        """));
        when(aiClient.structured(anyString(), any(), eq("procedure_recommendations"), any(JsonNode.class)))
                .thenReturn(objectMapper.readTree("""
                        {"recommendations":[{"procedureId":"elasticity-care","name":"탄력 관리",
                        "description":"피부 탄력 관리를 돕습니다.","reason":"입력한 목표를 기준으로 추천했습니다.",
                        "estimatedSessions":null,"interval":null,"estimatedPrice":null}]}
                        """));
        when(aiClient.structured(anyString(), any(), eq("meal_recommendations"), any(JsonNode.class)))
                .thenReturn(objectMapper.readTree("""
                        {"title":"든든한 저녁","description":"최근 기록을 고려한 균형 식사입니다.",
                        "foods":[
                          {"foodName":"현미밥","amount":150,"amountUnit":"g","kcal":230,"carbs":48,"protein":5,"fat":2},
                          {"foodName":"닭가슴살 구이","amount":100,"amountUnit":"g","kcal":165,"carbs":0,"protein":31,"fat":4}
                        ],"reason":"최근 활동량과 식단 기록을 바탕으로 구성했습니다."}
                        """));
    }

    @Test
    void chatStoresConversationAndCompletedMessages() throws Exception {
        String token = signupAndLogin("ai-chat@example.com");
        String response = mockMvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"오늘 운동 뭐 하면 좋을까?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("오늘은 가벼운 걷기와 스트레칭을 추천해요."))
                .andReturn().getResponse().getContentAsString();
        long conversationId = objectMapper.readTree(response).get("conversationId").asLong();
        mockMvc.perform(get("/api/v1/ai/conversations/{conversationId}", conversationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"));
    }

    @Test
    void nutritionDraftCanBeRecordedAsEditableMeal() throws Exception {
        String token = signupAndLogin("ai-food@example.com");
        String response = mockMvc.perform(multipart("/api/v1/ai/nutrition/analyses")
                        .param("date", "2026-08-18").param("mealType", "LUNCH")
                        .param("inputType", "TEXT").param("text", "현미밥 한 공기")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.foods[0].source").value("AI_ESTIMATE"))
                .andReturn().getResponse().getContentAsString();
        long analysisId = objectMapper.readTree(response).get("analysisId").asLong();
        long foodId = objectMapper.readTree(response).path("foods").get(0).get("draftFoodId").asLong();
        mockMvc.perform(patch("/api/v1/ai/nutrition/analyses/{analysisId}/foods/{foodId}", analysisId, foodId)
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fat\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fat").doesNotExist());
        mockMvc.perform(post("/api/v1/ai/nutrition/analyses/{analysisId}/record", analysisId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RECORDED"))
                .andExpect(jsonPath("$.meals[0].nutritionAnalysisId").value(analysisId));
        mockMvc.perform(get("/api/v1/diary/daily").param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalCalories").value(310));
    }

    @Test
    void reportAndProcedureRecommendationReturnStructuredAiResults() throws Exception {
        String token = signupAndLogin("ai-care-success@example.com");
        String bodyCheck = mockMvc.perform(post("/api/v1/care/body-checks")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordedDate\":\"2026-08-17\",\"originalImageUrl\":\"https://example.com/body.jpg\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long bodyCheckId = objectMapper.readTree(bodyCheck).get("bodyCheckId").asLong();
        mockMvc.perform(post("/api/v1/ai/procedures/recommendations")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyCheckId\":" + bodyCheckId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recommendations[0].name").value("탄력 관리"));
        mockMvc.perform(post("/api/v1/ai/reports")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"2026-08-10\",\"to\":\"2026-08-16\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.priorities[0].title").value("수분"));
    }

    @Test
    void personalizedMealRecommendationReturnsFoodsAndServerCalculatedTotals() throws Exception {
        String token = signupAndLogin("ai-meal-recommendation@example.com");
        mockMvc.perform(post("/api/v1/ai/meals/recommendations")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-08-19\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("든든한 저녁"))
                .andExpect(jsonPath("$.foods.length()").value(2))
                .andExpect(jsonPath("$.totalKcal").value(395))
                .andExpect(jsonPath("$.totalCarbs").value(48))
                .andExpect(jsonPath("$.totalProtein").value(36))
                .andExpect(jsonPath("$.totalFat").value(6));
    }

    @Test
    void unknownMealNutrientKeepsTheCorrespondingTotalUnknown() throws Exception {
        when(aiClient.structured(anyString(), any(), eq("meal_recommendations"), any(JsonNode.class)))
                .thenReturn(objectMapper.readTree("""
                        {"title":"간단한 저녁","description":"기록을 고려한 식사입니다.",
                        "foods":[{"foodName":"채소 비빔밥","amount":null,"amountUnit":null,
                        "kcal":420,"carbs":70,"protein":12,"fat":null}],
                        "reason":"최근 식단 기록을 바탕으로 구성했습니다."}
                        """));
        String token = signupAndLogin("ai-meal-null@example.com");
        mockMvc.perform(post("/api/v1/ai/meals/recommendations")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-08-19\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalKcal").value(420))
                .andExpect(jsonPath("$.totalFat").doesNotExist());
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
