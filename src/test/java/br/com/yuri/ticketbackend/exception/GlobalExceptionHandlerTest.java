package br.com.yuri.ticketbackend.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnBadRequestWhenRequestValidationFails()
            throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/test/validated"))
                .andExpect(jsonPath("$.fieldErrors.length()")
                        .value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field")
                        .value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Name is required"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed()
            throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request body is invalid or malformed"))
                .andExpect(jsonPath("$.path")
                        .value("/test/validated"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException()
            throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid argument"))
                .andExpect(jsonPath("$.path")
                        .value("/test/illegal-argument"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictForIllegalStateException()
            throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid application state"))
                .andExpect(jsonPath("$.path")
                        .value("/test/illegal-state"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation()
            throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "The request conflicts with the current database state"
                ))
                .andExpect(jsonPath("$.path")
                        .value("/test/data-integrity"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenResourceDoesNotExist()
            throws Exception {
        mockMvc.perform(get("/test/missing-resource"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Resource not found"))
                .andExpect(jsonPath("$.path")
                        .value("/test/missing-resource"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnMethodNotAllowedWhenMethodIsNotSupported()
            throws Exception {
        mockMvc.perform(get("/test/validated"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error")
                        .value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").value(
                        "HTTP method GET is not supported for this endpoint"
                ))
                .andExpect(jsonPath("$.path")
                        .value("/test/validated"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException()
            throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected internal error occurred"))
                .andExpect(jsonPath("$.path")
                        .value("/test/unexpected"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @RestController
    @RequestMapping("/test")
    static class ExceptionTestController {

        @PostMapping("/validated")
        ResponseEntity<Void> validate(
                @Valid @RequestBody TestRequest request
        ) {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/illegal-argument")
        void throwIllegalArgumentException() {
            throw new IllegalArgumentException(
                    "Invalid argument"
            );
        }

        @GetMapping("/illegal-state")
        void throwIllegalStateException() {
            throw new IllegalStateException(
                    "Invalid application state"
            );
        }

        @GetMapping("/data-integrity")
        void throwDataIntegrityViolationException() {
            throw new DataIntegrityViolationException(
                    "Database constraint violation"
            );
        }

        @GetMapping("/missing-resource")
        void throwNoResourceFoundException()
                throws NoResourceFoundException {
            throw new NoResourceFoundException(
                    HttpMethod.GET,
                    "/test/missing-resource",
                    "missing-resource"
            );
        }

        @GetMapping("/unexpected")
        void throwUnexpectedException() {
            throw new RuntimeException(
                    "Sensitive internal error"
            );
        }
    }

    record TestRequest(
            @NotBlank(message = "Name is required")
            String name
    ) {
    }
}