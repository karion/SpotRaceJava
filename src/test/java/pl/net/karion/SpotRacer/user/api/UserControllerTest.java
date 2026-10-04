package pl.net.karion.SpotRacer.user.api;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

class UserControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "john-api-" + suffix + "@example.com";
        String body = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                .contentType(APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
        ;
    }

    @Test
    void shouldGetUserByIdAsAdmin() throws Exception {

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Paulina", "Pobrana");

        mockMvc.perform(get("/api/user/{id}", saved.getId())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("Paulina"))
                .andExpect(jsonPath("$.lastname").value("Pobrana"))
        ;
    }

    @Test
    void shouldNotGetUserByRandomUuid() throws Exception {

        UUID id =  UUID.randomUUID();

        mockMvc.perform(get("/api/user/{id}", id.toString())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldFailAuthorisation() throws Exception {
        UUID id =  UUID.randomUUID();

        mockMvc.perform(get("/api/user/{id}", id.toString())
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldFailWhenCreateWithSameEmail() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Paulina", "Pobrana");

        String body = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
        ;
    }

    @Test
    void shouldFailOnIncorrectData() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        String body = """
                {
                  "email": "%s",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldFindUserWithSearch() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Sara", "Szukana");


        mockMvc.perform(get("/api/user?search=szukana")
                        .contentType(APPLICATION_JSON)
                        .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
        ;
    }

    @Test
    void shouldBlockOnRequestWithConflict() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "john-api-" + suffix + "@example.com";
        String body1 = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        String body2 = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "Other",
                  "lastname": "User"
                }
                """.formatted(email);


        Callable<MvcResult> requestA = () -> {
            return mockMvc.perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body1)
                )
                .andReturn()
                ;
        };

        Callable<MvcResult> requestB = () -> {
            return mockMvc.perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body2)
                )
                .andReturn()
                ;
        };

        //userRepository.existsByEmail(request.getEmail())) {

        CyclicBarrier barrier = new CyclicBarrier(2);

        MethodInterceptor interceptor = invocation -> {
            boolean matchingCall =
                invocation.getMethod().getName().equals("existsByEmail")
                    && invocation.getArguments().length == 1;

            // Wykonanie oryginalnej metody repozytorium.
            Object result = invocation.proceed();

            if (matchingCall) {
                barrier.await(5, TimeUnit.SECONDS);
            }

            return result;
        };

        Advised repositoryProxy = (Advised) userRepository;
        repositoryProxy.addAdvice(0, interceptor);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<MvcResult> futureA = executor.submit(requestA);
            Future<MvcResult> futureB = executor.submit(requestB);

            try {
                MvcResult resultA = futureA.get(10, TimeUnit.SECONDS);
                MvcResult resultB = futureB.get(10, TimeUnit.SECONDS);

                List<MockHttpServletResponse> responses = List.of(
                    resultA.getResponse(),
                    resultB.getResponse()
                );

                User user = this.userRepository.findByEmail(email).orElse(null);
                assertThat(user).isNotNull();
                assertThat(responses.stream().map(MockHttpServletResponse::getStatus))
                    .containsExactlyInAnyOrder(409, 201);
                UUID userIdInDb = user.getId();

                if (resultA.getResponse().getStatus() == 201) {
                    jsonPath("$.id")
                        .value(user.getId().toString())
                        .match(resultA);
                } else if (resultB.getResponse().getStatus() == 201) {
                    jsonPath("$.id")
                        .value(user.getId().toString())
                        .match(resultB);
                }
            } finally {
                futureA.cancel(true);
                futureB.cancel(true);
            }
        } finally {
            repositoryProxy.removeAdvice(interceptor);
        }
    }
}