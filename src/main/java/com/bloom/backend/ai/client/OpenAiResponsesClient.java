package com.bloom.backend.ai.client;

import com.bloom.backend.ai.config.OpenAiProperties;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class OpenAiResponsesClient {
    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiResponsesClient(RestClient openAiRestClient, OpenAiProperties properties,
                                 ObjectMapper objectMapper) {
        this.restClient = openAiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String text(String instructions, Object input) {
        return execute(instructions, input, null, null);
    }

    public JsonNode structured(String instructions, Object input, String schemaName, JsonNode schema) {
        String output = execute(instructions, input, schemaName, schema);
        try {
            return objectMapper.readTree(output);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    public String model() { return properties.getModel(); }

    private String execute(String instructions, Object input, String schemaName, JsonNode schema) {
        if (!properties.configured()) throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("instructions", instructions);
        body.put("input", input);
        body.put("store", false);
        body.put("reasoning", Map.of("effort", "low"));
        if (schema != null) {
            body.put("text", Map.of("format", Map.of(
                    "type", "json_schema", "name", schemaName, "strict", true, "schema", schema)));
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                JsonNode response = restClient.post().uri("/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + properties.getApiKey())
                        .body(body).retrieve().body(JsonNode.class);
                String output = extractOutputText(response);
                if (output != null && !output.isBlank()) return output;
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            } catch (HttpClientErrorException exception) {
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            } catch (RestClientException exception) {
                if (attempt == 1) throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
        }
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) return null;
        for (JsonNode item : response.path("output")) {
            if (!"message".equals(item.path("type").asText())) continue;
            for (JsonNode content : item.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    return content.path("text").asText(null);
                }
            }
        }
        return null;
    }
}
