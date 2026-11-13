package com.vansh.expensetracker.model;

import java.math.BigDecimal;
import java.util.List;

/** Totals across all expenses, with a per-category breakdown. */
public record ExpenseSummary(
        BigDecimal totalAmount,
        long totalExpenses,
        List<CategorySummary> byCategory) {
}
