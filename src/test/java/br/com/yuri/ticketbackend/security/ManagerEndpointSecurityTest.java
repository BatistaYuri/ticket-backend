package br.com.yuri.ticketbackend.security;

import br.com.yuri.ticketbackend.queue.controller.ManagerQueueController;
import br.com.yuri.ticketbackend.queue.service.QueueService;
import br.com.yuri.ticketbackend.security.config.SecurityConfig;
import br.com.yuri.ticketbackend.security.controller.AuthenticationController;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                AuthenticationController.class,
                ManagerQueueController.class
        },
        properties = {
                "app.jwt.secret="
                        + "ticket-backend-secret-key-1234567890"
        }
)
@Import(SecurityConfig.class)
class ManagerEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueService queueService;

    @Test
    void shouldGenerateManagerTokenWithoutParameters()
            throws Exception {
        mockMvc.perform(
                        post("/api/auth/manager-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectManagerEndpointWithoutToken()
            throws Exception {
        mockMvc.perform(
                        post("/api/manager/queue/reset")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowManagerEndpointWithGeneratedToken()
            throws Exception {
        String responseBody = mockMvc.perform(
                        post("/api/auth/manager-token")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(
                responseBody,
                "$.token"
        );

        mockMvc.perform(
                        post("/api/manager/queue/reset")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        verify(queueService).resetQueue();
    }
}