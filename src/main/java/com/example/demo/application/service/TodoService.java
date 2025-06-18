package com.example.demo.application.service;

import com.example.demo.domain.model.Todo;
import com.example.demo.domain.model.TodoStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoService {

    List<Todo> findAllTodos();

    Optional<Todo> findTodoById(Long id);

    Todo createTodo(String title, LocalDate deadline);

    Todo saveTodo(Todo todo); // For general save/update, e.g., if the whole object is available

    void deleteTodoById(Long id);

    Todo changeTodoStatus(Long id, TodoStatus newStatus);

    Todo cycleTodoStatus(Long id);

    Todo updateTodoDetails(Long id, String newTitle, LocalDate newDeadline, TodoStatus newStatus);
}
