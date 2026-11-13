package com.vansh.expensetracker.service;

import com.vansh.expensetracker.exception.ExpenseNotFoundException;
import com.vansh.expensetracker.model.Category;
import com.vansh.expensetracker.model.CategorySummary;
import com.vansh.expensetracker.model.Expense;
import com.vansh.expensetracker.model.ExpenseSummary;
import com.vansh.expensetracker.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(Category.from(category));
    }

    public List<Expense> getExpensesBetween(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateBetween(start, end);
    }

    public ExpenseSummary getSummary() {
        List<CategorySummary> byCategory = expenseRepository.categoryBreakdown().stream()
                .map(row -> new CategorySummary(
                        (Category) row[0],
                        (BigDecimal) row[1],
                        (Long) row[2]))
                .toList();
        return new ExpenseSummary(
                expenseRepository.totalAmount(),
                expenseRepository.count(),
                byCategory);
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        expense.setId(null);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Long id, Expense updated) {
        Expense existing = getExpenseById(id);
        existing.setDescription(updated.getDescription());
        existing.setAmount(updated.getAmount());
        existing.setCategory(updated.getCategory());
        existing.setDate(updated.getDate());
        return expenseRepository.save(existing);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense existing = getExpenseById(id);
        expenseRepository.delete(existing);
    }
}
