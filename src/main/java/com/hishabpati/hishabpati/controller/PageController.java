package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.dto.ExpenseSaveDTO;
import com.hishabpati.hishabpati.dto.UserSaveDTO;
import com.hishabpati.hishabpati.model.Expense;
import com.hishabpati.hishabpati.model.ExpenseCategory;
import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.service.ExpenseService;
import com.hishabpati.hishabpati.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final ExpenseService expenseService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name, @RequestParam String email,
                         @RequestParam String password, Model model) {
        if (userService.emailExists(email)) {
            model.addAttribute("error", "This email is already registered.");
            return "signup";
        }
        userService.register(new UserSaveDTO(name, email, password));
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("metrics", expenseService.getMetrics(user.getId()));
        model.addAttribute("recentExpenses", expenseService.findRecent(user.getId()));
        return "dashboard";
    }

    @GetMapping("/expenses")
    public String expenses(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("expenses", expenseService.findByUser(user.getId()));
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("editing", null);
        return "expenses";
    }

    @GetMapping("/expenses/{id}/edit")
    public String editExpense(@PathVariable String id, Authentication authentication, Model model) {
        User user = currentUser(authentication);
        Expense expense = expenseService.findById(id, user.getId());
        if (expense == null) {
            return "redirect:/expenses";
        }
        model.addAttribute("user", user);
        model.addAttribute("expenses", expenseService.findByUser(user.getId()));
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("editing", expense);
        return "expenses";
    }

    @PostMapping("/expenses")
    public String addExpense(@RequestParam String title, @RequestParam ExpenseCategory category,
                             @RequestParam Double amount, @RequestParam(required = false) String date,
                             @RequestParam(required = false) String note, Authentication authentication) {
        User user = currentUser(authentication);
        expenseService.add(user.getId(), new ExpenseSaveDTO(title, category, amount, parseDate(date), note));
        return "redirect:/expenses";
    }

    @PostMapping("/expenses/{id}")
    public String updateExpense(@PathVariable String id, @RequestParam String title,
                                @RequestParam ExpenseCategory category, @RequestParam Double amount,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false) String note, Authentication authentication) {
        User user = currentUser(authentication);
        expenseService.update(id, user.getId(), new ExpenseSaveDTO(title, category, amount, parseDate(date), note));
        return "redirect:/expenses";
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable String id, Authentication authentication) {
        User user = currentUser(authentication);
        expenseService.delete(id, user.getId());
        return "redirect:/expenses";
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }

    private LocalDate parseDate(String date) {
        return (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
    }

}