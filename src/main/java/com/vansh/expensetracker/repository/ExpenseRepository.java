package com.vansh.expensetracker.repository;

import com.vansh.expensetracker.model.Category;
import com.vansh.expensetracker.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(Category category);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal totalAmount();

    /**
     * One row per category: [Category, SUM(amount), COUNT].
     */
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0), COUNT(e) FROM Expense e GROUP BY e.category")
    List<Object[]> categoryBreakdown();
}
