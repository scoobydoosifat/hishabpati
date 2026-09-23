package com.hishabpati.hishabpati.repository;

import com.hishabpati.hishabpati.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findByUserIdOrderByDateDesc(String userId);
    List<Expense> findTop5ByUserIdOrderByDateDesc(String userId);
    Optional<Expense> findByIdAndUserId(String id, String userId);

}