package com.hishabpati.hishabpati.service;

import com.hishabpati.hishabpati.dto.CategoryTotal;
import com.hishabpati.hishabpati.dto.ExpenseSaveDTO;
import com.hishabpati.hishabpati.dto.Metrics;
import com.hishabpati.hishabpati.model.Expense;
import com.hishabpati.hishabpati.model.ExpenseCategory;
import com.hishabpati.hishabpati.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public Expense add(String userId, ExpenseSaveDTO dto) {
        Expense expense = Expense.builder()
                .userId(userId)
                .title(dto.title())
                .category(dto.category())
                .amount(dto.amount())
                .date(dto.date() == null ? LocalDate.now() : dto.date())
                .note(dto.note())
                .build();
        return expenseRepository.save(expense);
    }

    public List<Expense> findByUser(String userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Expense> findRecent(String userId) {
        return expenseRepository.findTop5ByUserIdOrderByDateDesc(userId);
    }

    public Expense findById(String id, String userId) {
        return expenseRepository.findByIdAndUserId(id, userId).orElse(null);
    }

    public Expense update(String id, String userId, ExpenseSaveDTO dto) {
        Expense expense = findById(id, userId);
        if (expense == null) {
            return null;
        }
        expense.setTitle(dto.title());
        expense.setCategory(dto.category());
        expense.setAmount(dto.amount());
        expense.setDate(dto.date() == null ? LocalDate.now() : dto.date());
        expense.setNote(dto.note());
        return expenseRepository.save(expense);
    }

    public boolean delete(String id, String userId) {
        Expense expense = findById(id, userId);
        if (expense == null) {
            return false;
        }
        expenseRepository.delete(expense);
        return true;
    }

    public Metrics getMetrics(String userId) {
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(userId);
        long totalExpenses = expenses.size();
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        Map<ExpenseCategory, Double> grouped = expenses.stream().collect(
                Collectors.groupingBy(Expense::getCategory, LinkedHashMap::new, Collectors.summingDouble(Expense::getAmount)));
        List<CategoryTotal> categoryTotals = grouped.entrySet().stream()
                .map(entry -> new CategoryTotal(entry.getKey().name(), entry.getValue()))
                .toList();
        return new Metrics(totalExpenses, totalSpent, categoryTotals);
    }

}