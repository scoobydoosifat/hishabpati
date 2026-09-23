package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.service.ExpenseService;
import com.hishabpati.hishabpati.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.RedirectView;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ExpenseService expenseService;

    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JsonMapper mapper = JsonMapper.builder().build();
        PageController controller = new PageController(userService, expenseService);
        View stubView = new AbstractView() {
            @Override
            protected void renderMergedOutputModel(Map<String, Object> model,
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response) {
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper))
                .setViewResolvers((viewName, locale) ->
                        viewName.startsWith("redirect:")
                                ? new RedirectView(viewName.substring("redirect:".length()))
                                : stubView)
                .build();
        principal = new UsernamePasswordAuthenticationToken("sifat@example.com", null, List.of());
        when(userService.findByEmail("sifat@example.com")).thenReturn(
                User.builder().id("u1").name("Sifat").email("sifat@example.com").build());
    }

    @Test
    void indexReturnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void loginReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void signupReturnsSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void signupPostRedirectsToLoginWhenEmailIsAvailable() throws Exception {
        when(userService.emailExists("sifat@example.com")).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/signup")
                        .param("name", "Sifat")
                        .param("email", "sifat@example.com")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void signupPostShowsErrorWhenEmailIsTaken() throws Exception {
        when(userService.emailExists("sifat@example.com")).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/signup")
                        .param("name", "Sifat")
                        .param("email", "sifat@example.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void dashboardRendersMetricsAndRecentExpenses() throws Exception {
        when(expenseService.getMetrics("u1")).thenReturn(
                new com.hishabpati.hishabpati.dto.Metrics(2, 300.0, java.util.List.of()));
        when(expenseService.findRecent("u1")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/dashboard").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("user", User.builder().id("u1").name("Sifat").email("sifat@example.com").build()))
                .andExpect(model().attributeExists("metrics"))
                .andExpect(model().attributeExists("recentExpenses"));
    }

    @Test
    void expensesPageRendersList() throws Exception {
        when(expenseService.findByUser("u1")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/expenses").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses"))
                .andExpect(model().attributeExists("expenses"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("editing", nullValue()));
    }

    @Test
    void addExpenseRedirectsToList() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/expenses")
                        .principal(principal)
                        .param("title", "Lunch")
                        .param("category", "FOOD")
                        .param("amount", "250.00")
                        .param("note", "biryani"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
    }

    @Test
    void deleteExpenseRedirectsToList() throws Exception {
        when(expenseService.delete("e1", "u1")).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/expenses/e1/delete")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
    }

}