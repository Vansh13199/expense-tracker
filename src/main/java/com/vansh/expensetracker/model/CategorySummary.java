package com.vansh.expensetracker.model;

import java.math.BigDecimal;

/** Total spend and expense count for one category. */
public record CategorySummary(
        Category category,
        BigDecimal totalAmount,
        long count) {
}
