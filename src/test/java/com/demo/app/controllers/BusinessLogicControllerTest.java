package com.demo.app.controllers;

import com.demo.app.services.UserCredentialsService;
import com.demo.app.services.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BusinessLogicControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserCredentialsService userCredentialsService;
    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @WithMockUser(username = "Tanya", roles = {"USER"})
    void greeting() throws Exception {
        this.mockMvc.perform(get("/greeting")
                .with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello world"));
    }

    @Test
    @WithMockUser(username = "Tanya", roles = {"USER"})
    void generateGreeting() throws Exception {
        this.mockMvc.perform(get("/generateGreeting")
                .with(csrf().asHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "Tanya", authorities = {"greete"})
    void greetingToUser() throws Exception {
        this.mockMvc.perform(get("/greetingToUser")
                .with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Tanya"));
    }
}