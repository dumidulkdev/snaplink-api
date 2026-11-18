package com.snaplink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "snaplink.rate-limit.create.capacity=5",
        "snaplink.rate-limit.create.refill-per-minute=5",
        "snaplink.rate-limit.redirect.capacity=10",
        "snaplink.rate-limit.redirect.refill-per-minute=10"
})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createEndpointReturns429WhenRateLimited() throws Exception {
        String body = """
                {"targetUrl":"https://example.com/rate"}
                """;
        int limit = 5;
        String clientIp = "198.51.100.77";
        for (int i = 0; i < limit; i++) {
            mockMvc.perform(post("/api/links")
                            .header("X-Forwarded-For", clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.replace("rate", "rate-" + i)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/links")
                        .header("X-Forwarded-For", clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUrl\":\"https://example.com/rate-overflow\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("rate_limited"));
    }

    @Test
    void redirectEndpointReturns429WhenRateLimited() throws Exception {
        String create = """
                {"targetUrl":"https://example.com/redirect-rate","customAlias":"rate-redirect"}
                """;
        mockMvc.perform(post("/api/links")
                        .header("X-Forwarded-For", "203.0.113.50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated());

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/rate-redirect")
                            .header("X-Forwarded-For", "203.0.113.50"))
                    .andExpect(status().isFound());
        }

        mockMvc.perform(get("/rate-redirect").header("X-Forwarded-For", "203.0.113.50"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}