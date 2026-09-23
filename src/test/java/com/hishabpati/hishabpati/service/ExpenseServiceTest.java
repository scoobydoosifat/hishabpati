package com.hishabpati.hishabpati.service;

import com.hishabpati.hishabpati.dto.CategoryTotal;
import com.hishabpati.hishabpati.dto.ExpenseSaveDTO;
import com.hishabpati.hishabpati.dto.Metrics;
import com.hishabpati.hishabpati.model.Expense;
import com.hishabpati.hishabpati.model.ExpenseCategory;
import com.hishabpati.hishabpati.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void addCreatesExpenseForUserWithDefaultDate() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseService.add("u1",
                new ExpenseSaveDTO("Lunch", ExpenseCategory.FOOD, 250.0, null, "biryani"));

        assertNotNull(result);
        assertEquals("u1", result.getUserId());
        assertEquals("Lunch", result.getTitle());
        assertEquals(ExpenseCategory.FOOD, result.getCategory());
        assertEquals(250.0, result.getAmount());
        assertEquals(LocalDate.now(), result.getDate());
        assertEquals("biryani", result.getNote());
    }

    @Test
    void addUsesProvidedDate() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate date = LocalDate.of(2026, 9, 20);

        Expense result = expenseService.add("u1",
                new ExpenseSaveDTO("Groceries", ExpenseCategory.UTILITIES, 1200.0, date, null));

        assertEquals(date, result.getDate());
    }

    @Test
    void updateReturnsNullWhenExpenseDoesNotBelongToUser() {
        when(expenseRepository.findByIdAndUserId("x1", "u1")).thenReturn(Optional.empty());

        Expense result = expenseService.update("x1", "u1",
                new ExpenseSaveDTO("Lunch", ExpenseCategory.FOOD, 100.0, null, null));

        assertNull(result);
    }

    @Test
    void updateChangesFieldsAndSaves() {
        Expense existing = Expense.builder()
                .id("e1").userId("u1").title("Old").category(ExpenseCategory.OTHER)
                .amount(10.0).date(LocalDate.now()).build();
        when(expenseRepository.findByIdAndUserId("e1", "u1")).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseService.update("e1", "u1",
                new ExpenseSaveDTO("New Title", ExpenseCategory.FOOD, 300.0, null, "updated"));

        assertEquals("e1", result.getId());
        assertEquals("New Title", result.getTitle());
        assertEquals(ExpenseCategory.FOOD, result.getCategory());
        assertEquals(300.0, result.getAmount());
        verify(expenseRepository).save(existing);
    }

    @Test
    void deleteReturnsFalseWhenExpenseDoesNotBelongToUser() {
        when(expenseRepository.findByIdAndUserId("x1", "u1")).thenReturn(Optional.empty());

        assertFalse(expenseService.delete("x1", "u1"));
    }

    @Test
    void deleteRemovesOwnedExpense() {
        Expense expense = Expense.builder().id("e1").userId("u1").build();
        when(expenseRepository.findByIdAndUserId("e1", "u1")).thenReturn(Optional.of(expense));

        assertTrue(expenseService.delete("e1", "u1"));
        verify(expenseRepository).delete(expense);
    }

    @Test
    void getMetricsAggregatesTotalsByCategory() {
        Expense lunch = Expense.builder().id("e1").userId("u1").title("Lunch")
                .category(ExpenseCategory.FOOD).amount(200.0).date(LocalDate.now()).build();
        Expense bus = Expense.builder().id("e2").userId("u1").title("Bus")
                .category(ExpenseCategory.TRANSPORT).amount(50.0).date(LocalDate.now()).build();
        Expense dinner = Expense.builder().id("e3").userId("u1").title("Dinner")
                .category(ExpenseCategory.FOOD).amount(150.0).date(LocalDate.now()).build();
        when(expenseRepository.findByUserIdOrderByDateDesc("u1")).thenReturn(List.of(lunch, bus, dinner));

        Metrics metrics = expenseService.getMetrics("u1");

        assertEquals(3, metrics.totalExpenses());
        assertEquals(400.0, metrics.totalSpent());
        assertEquals(2, metrics.categoryTotals().size());
        CategoryTotal food = metrics.categoryTotals().stream()
                .filter(cat -> cat.category().equals("FOOD")).findFirst().orElseThrow();
        CategoryTotal transport = metrics.categoryTotals().stream()
                .filter(cat -> cat.category().equals("TRANSPORT")).findFirst().orElseThrow();
        assertEquals(350.0, food.total());
        assertEquals(50.0, transport.total());
    }

    @Test
    void findRecentReturnsLatestFive() {
        Expense e1 = Expense.builder().id("e1").userId("u1").build();
        when(expenseRepository.findTop5ByUserIdOrderByDateDesc("u1")).thenReturn(List.of(e1));

        List<Expense> result = expenseService.findRecent("u1");

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getId());
    }

}