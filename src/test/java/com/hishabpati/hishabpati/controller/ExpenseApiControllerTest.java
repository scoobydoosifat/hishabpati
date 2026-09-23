package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.dto.ExpenseSaveDTO;
import com.hishabpati.hishabpati.dto.Metrics;
import com.hishabpati.hishabpati.model.Expense;
import com.hishabpati.hishabpati.model.ExpenseCategory;
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
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseApiControllerTest {

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
        ExpenseApiController controller = new ExpenseApiController(userService, expenseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper))
                .build();
        principal = new UsernamePasswordAuthenticationToken("sifat@example.com", null, List.of());
        when(userService.findByEmail("sifat@example.com")).thenReturn(
                User.builder().id("u1").name("Sifat").email("sifat@example.com").build());
    }

    @Test
    void getExpensesReturnsAllExpensesForUser() throws Exception {
        Expense expense = Expense.builder()
                .id("e1").userId("u1").title("Lunch").category(ExpenseCategory.FOOD)
                .amount(250.0).date(LocalDate.of(2026, 9, 23)).note("biryani").build();
        when(expenseService.findByUser("u1")).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/expenses").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("e1"))
                .andExpect(jsonPath("$[0].title").value("Lunch"))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].amount").value(250.0));
    }

    @Test
    void getExpenseReturnsFoundExpense() throws Exception {
        Expense expense = Expense.builder().id("e1").userId("u1").title("Lunch").build();
        when(expenseService.findById("e1", "u1")).thenReturn(expense);

        mockMvc.perform(get("/api/expenses/e1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Lunch"));
    }

    @Test
    void getExpenseReturnsNotFoundForOtherUser() throws Exception {
        when(expenseService.findById("e2", "u1")).thenReturn(null);

        mockMvc.perform(get("/api/expenses/e2").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExpenseReturnsCreatedWithLocation() throws Exception {
        when(expenseService.add(anyString(), any(ExpenseSaveDTO.class))).thenReturn(
                Expense.builder().id("e1").userId("u1").title("Lunch").build());

        mockMvc.perform(post("/api/expenses").principal(principal)
                        .contentType("application/json")
                        .content("{\"title\":\"Lunch\",\"category\":\"FOOD\",\"amount\":250.0,\"date\":\"2026-09-23\",\"note\":\"\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/expenses/e1"));
    }

    @Test
    void updateExpenseReturnsUpdatedExpense() throws Exception {
        when(expenseService.update(anyString(), anyString(), any(ExpenseSaveDTO.class))).thenReturn(
                Expense.builder().id("e1").userId("u1").title("Lunch").category(ExpenseCategory.FOOD)
                        .amount(300.0).date(LocalDate.of(2026, 9, 23)).build());

        mockMvc.perform(put("/api/expenses/e1").principal(principal)
                        .contentType("application/json")
                        .content("{\"title\":\"Lunch\",\"category\":\"FOOD\",\"amount\":300.0,\"date\":\"2026-09-23\",\"note\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300.0));
    }

    @Test
    void updateExpenseReturnsNotFoundForOtherUser() throws Exception {
        when(expenseService.update(anyString(), anyString(), any(ExpenseSaveDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/expenses/e2").principal(principal)
                        .contentType("application/json")
                        .content("{\"title\":\"Lunch\",\"category\":\"FOOD\",\"amount\":300.0,\"date\":\"2026-09-23\",\"note\":\"\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpenseReturnsNoContent() throws Exception {
        when(expenseService.delete("e1", "u1")).thenReturn(true);

        mockMvc.perform(delete("/api/expenses/e1").principal(principal))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExpenseReturnsNotFound() throws Exception {
        when(expenseService.delete("e2", "u1")).thenReturn(false);

        mockMvc.perform(delete("/api/expenses/e2").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMetricsReturnsSummary() throws Exception {
        when(expenseService.getMetrics("u1")).thenReturn(new Metrics(2, 350.0, List.of()));

        mockMvc.perform(get("/api/expenses/summary").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(2))
                .andExpect(jsonPath("$.totalSpent").value(350.0));
    }

}