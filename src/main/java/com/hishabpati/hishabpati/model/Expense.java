package com.hishabpati.hishabpati.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;
    private String userId;
    private String title;
    private ExpenseCategory category;
    private Double amount;
    private LocalDate date;
    private String note;

}