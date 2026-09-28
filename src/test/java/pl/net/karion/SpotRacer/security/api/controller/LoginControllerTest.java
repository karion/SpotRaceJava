package pl.net.karion.SpotRacer.security.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.*;


class LoginControllerTest extends IntegrationTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldHaveSameExpirationTimeInTokenAndInResponse() throws Exception {
        User user = userFixture.createUser();

        String body = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(user.getEmail(), UserFixture.UNIVERSAL_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andReturn()
        ;

        String json = result.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");
        String expiresAt = JsonPath.read(json, "$.expiresAt");

        Instant expiresAtInstant = Instant.parse(expiresAt);

        Instant tokenExpiry = jwtDecoder.decode(token).getExpiresAt();

        assertThat(expiresAtInstant).isEqualTo(tokenExpiry);
    }
}