package com.hishabpati.hishabpati.dto;

import com.hishabpati.hishabpati.model.ExpenseCategory;

import java.time.LocalDate;

public record ExpenseSaveDTO(String title, ExpenseCategory category, Double amount, LocalDate date, String note) {
}