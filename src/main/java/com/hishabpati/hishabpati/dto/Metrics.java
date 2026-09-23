package com.hishabpati.hishabpati.dto;

import java.util.List;

public record Metrics(long totalExpenses, double totalSpent, List<CategoryTotal> categoryTotals) {
}