package com.vansh.expensetracker;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vansh.expensetracker.controller.ExpenseController;
import com.vansh.expensetracker.exception.ExpenseNotFoundException;
import com.vansh.expensetracker.model.Category;
import com.vansh.expensetracker.model.CategorySummary;
import com.vansh.expensetracker.model.Expense;
import com.vansh.expensetracker.model.ExpenseSummary;
import com.vansh.expensetracker.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    private Expense sampleExpense() {
        Expense expense = new Expense("Groceries at Big Bazaar",
                new BigDecimal("49.99"), Category.FOOD, LocalDate.of(2026, 11, 13));
        expense.setId(1L);
        return expense;
    }

    @Test
    void createExpense_returns201() throws Exception {
        when(expenseService.createExpense(any(Expense.class))).thenReturn(sampleExpense());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Groceries at Big Bazaar","amount":49.99,
                                 "category":"FOOD","date":"2026-11-13"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Groceries at Big Bazaar")))
                .andExpect(jsonPath("$.amount", is(49.99)))
                .andExpect(jsonPath("$.category", is("FOOD")))
                .andExpect(jsonPath("$.date", is("2026-11-13")));
    }

    @Test
    void getAllExpenses_returns200() throws Exception {
        Expense second = new Expense("Metro card top-up",
                new BigDecimal("20.00"), Category.TRANSPORT, LocalDate.of(2026, 11, 12));
        second.setId(2L);
        when(expenseService.getAllExpenses()).thenReturn(List.of(sampleExpense(), second));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Groceries at Big Bazaar")))
                .andExpect(jsonPath("$[1].category", is("TRANSPORT")));
    }

    @Test
    void getExpenseById_returns200() throws Exception {
        when(expenseService.getExpenseById(1L)).thenReturn(sampleExpense());

        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(49.99)));
    }

    @Test
    void getExpenseById_notFound_returns404() throws Exception {
        when(expenseService.getExpenseById(99L)).thenThrow(new ExpenseNotFoundException(99L));

        mockMvc.perform(get("/api/expenses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Expense not found with id: 99")));
    }

    @Test
    void getExpenseById_nonNumeric_returns400() throws Exception {
        mockMvc.perform(get("/api/expenses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void updateExpense_returns200() throws Exception {
        Expense updated = sampleExpense();
        updated.setDescription("Groceries at Big Bazaar (weekly)");
        updated.setAmount(new BigDecimal("59.99"));
        when(expenseService.updateExpense(eq(1L), any(Expense.class))).thenReturn(updated);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Groceries at Big Bazaar (weekly)","amount":59.99,
                                 "category":"FOOD","date":"2026-11-13"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Groceries at Big Bazaar (weekly)")))
                .andExpect(jsonPath("$.amount", is(59.99)));
    }

    @Test
    void deleteExpense_returns204() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExpense_notFound_returns404() throws Exception {
        doThrow(new ExpenseNotFoundException(99L)).when(expenseService).deleteExpense(99L);

        mockMvc.perform(delete("/api/expenses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensesByCategory_returns200() throws Exception {
        when(expenseService.getExpensesByCategory("FOOD")).thenReturn(List.of(sampleExpense()));

        mockMvc.perform(get("/api/expenses/category/FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("FOOD")));
    }

    @Test
    void getExpensesByCategory_invalid_returns400() throws Exception {
        when(expenseService.getExpensesByCategory("NOPE"))
                .thenThrow(new IllegalArgumentException("Invalid category: NOPE"));

        mockMvc.perform(get("/api/expenses/category/NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void getExpensesByDateRange_returns200() throws Exception {
        when(expenseService.getExpensesBetween(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(sampleExpense()));

        mockMvc.perform(get("/api/expenses/date-range")
                        .param("start", "2026-11-01")
                        .param("end", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getExpensesByDateRange_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/expenses/date-range")
                        .param("start", "2026-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_returns200() throws Exception {
        ExpenseSummary summary = new ExpenseSummary(
                new BigDecimal("69.99"), 2,
                List.of(new CategorySummary(Category.FOOD, new BigDecimal("49.99"), 1),
                        new CategorySummary(Category.TRANSPORT, new BigDecimal("20.00"), 1)));
        when(expenseService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(69.99)))
                .andExpect(jsonPath("$.totalExpenses", is(2)))
                .andExpect(jsonPath("$.byCategory", hasSize(2)))
                .andExpect(jsonPath("$.byCategory[0].category", is("FOOD")))
                .andExpect(jsonPath("$.byCategory[0].totalAmount", is(49.99)));
    }

    @Test
    void createExpense_invalid_returns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","amount":0,
                                 "category":null,"date":"2999-01-01"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.description", is("Description is required")))
                .andExpect(jsonPath("$.fieldErrors.amount", is("Amount must be at least 0.01")))
                .andExpect(jsonPath("$.fieldErrors.category", is("Category is required")))
                .andExpect(jsonPath("$.fieldErrors.date", is("Date cannot be in the future")));
    }

    @Test
    void createExpense_invalidCategory_returns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Test","amount":10.00,
                                 "category":"NOPE","date":"2026-11-13"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
