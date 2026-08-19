package com.bloom.backend.ai.client;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bloom.backend.ai.config.OpenAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiResponsesClientTests {

    @Test
    void requestsImageEditAndDecodesGeneratedPng() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://api.openai.test/v1");
        properties.setModel("gpt-5.6-terra");
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiResponsesClient client = new OpenAiResponsesClient(
                builder.build(), properties, new ObjectMapper());
        byte[] expected = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47};
        String response = """
                {"output":[{"type":"image_generation_call","result":"%s"}]}
                """.formatted(Base64.getEncoder().encodeToString(expected));

        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().string(containsString("\"type\":\"input_image\"")))
                .andExpect(content().string(containsString("\"type\":\"image_generation\"")))
                .andExpect(content().string(containsString("\"action\":\"edit\"")))
                .andRespond(withSuccess(response.getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON));

        byte[] actual = client.editImage("subtle edit", "data:image/jpeg;base64,/9g=");

        assertArrayEquals(expected, actual);
        server.verify();
    }
}
