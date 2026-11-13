package com.vansh.expensetracker.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Expense categories. Deserialization is case-insensitive, so
 * "food" and "FOOD" are both accepted in request bodies and URLs.
 */
public enum Category {
    FOOD,
    TRANSPORT,
    UTILITIES,
    ENTERTAINMENT,
    HEALTHCARE,
    SHOPPING,
    EDUCATION,
    OTHER;

    @JsonCreator
    public static Category from(String value) {
        if (value == null) {
            return null;
        }
        for (Category category : values()) {
            if (category.name().equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value
                + ". Valid values: FOOD, TRANSPORT, UTILITIES, ENTERTAINMENT, "
                + "HEALTHCARE, SHOPPING, EDUCATION, OTHER");
    }
}
