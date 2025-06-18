package com.example.demo.application.service.impl;

import com.example.demo.application.service.TodoService;
import com.example.demo.domain.model.Todo;
import com.example.demo.domain.model.TodoStatus;
import com.example.demo.TodoRepository; // Assuming TodoRepository is in com.example.demo package
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> findAllTodos() {
        return todoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Todo> findTodoById(Long id) {
        return todoRepository.findById(id);
    }

    @Override
    @Transactional
    public Todo createTodo(String title, LocalDate deadline) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        Todo newTodo = new Todo(null, title, deadline, TodoStatus.PENDING);
        return todoRepository.save(newTodo);
    }

    @Override
    @Transactional
    public Todo saveTodo(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("Todo cannot be null");
        }
        // Basic validation, can be expanded
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty for Todo");
        }
        if (todo.getStatus() == null) {
             todo.setStatus(TodoStatus.PENDING); // Default status if not set
        }
        return todoRepository.save(todo);
    }

    @Override
    @Transactional
    public void deleteTodoById(Long id) {
        if (!todoRepository.existsById(id)) {
            // Or throw a custom EntityNotFoundException
            // For now, just log or do nothing if it doesn't exist, to avoid error on delete if already deleted
            // System.out.println("Todo with id " + id + " not found for deletion.");
            return;
        }
        todoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Todo changeTodoStatus(Long id, TodoStatus newStatus) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id)); // Or a custom EntityNotFoundException
        todo.setStatus(newStatus);
        return todoRepository.save(todo);
    }

    @Override
    @Transactional
    public Todo cycleTodoStatus(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id)); // Or a custom EntityNotFoundException

        TodoStatus currentStatus = todo.getStatus();
        switch (currentStatus) {
            case PENDING:
                todo.setStatus(TodoStatus.IN_PROGRESS);
                break;
            case IN_PROGRESS:
                todo.setStatus(TodoStatus.COMPLETED);
                break;
            case COMPLETED:
                todo.setStatus(TodoStatus.PENDING); // Cycle back to PENDING
                break;
            case CANCELLED:
                // Do nothing if CANCELLED, or throw exception if it's an invalid action
                break;
            default:
                throw new IllegalStateException("Unknown status: " + currentStatus);
        }
        return todoRepository.save(todo);
    }

    @Override
    @Transactional
     public Todo updateTodoDetails(Long id, String newTitle, LocalDate newDeadline, TodoStatus newStatus) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id)); // Or a custom EntityNotFoundException

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("New title cannot be empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        todo.setTitle(newTitle);
        todo.setDeadline(newDeadline); // Deadline can be null
        todo.setStatus(newStatus);
        return todoRepository.save(todo);
    }
}
