package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.dto.ExpenseSaveDTO;
import com.hishabpati.hishabpati.dto.Metrics;
import com.hishabpati.hishabpati.model.Expense;
import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.service.ExpenseService;
import com.hishabpati.hishabpati.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseApiController {

    private final UserService userService;
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(expenseService.findByUser(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpense(@PathVariable String id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Expense expense = expenseService.findById(id, user.getId());
        return expense == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(expense);
    }

    @PostMapping
    public ResponseEntity<Void> createExpense(@RequestBody ExpenseSaveDTO dto, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Expense saved = expenseService.add(user.getId(), dto);
        return ResponseEntity.created(URI.create("/api/expenses/" + saved.getId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable String id, @RequestBody ExpenseSaveDTO dto,
                                                 Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Expense updated = expenseService.update(id, user.getId(), dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return expenseService.delete(id, user.getId())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Metrics> getMetrics(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(expenseService.getMetrics(user.getId()));
    }

}