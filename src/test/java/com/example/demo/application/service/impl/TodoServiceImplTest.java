package com.example.demo.application.service.impl;

import com.example.demo.TodoRepository;
import com.example.demo.domain.model.Todo;
import com.example.demo.domain.model.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

    private Todo todo1, todo2;

    @BeforeEach
    void setUp() {
        todo1 = new Todo(1L, "Test Todo 1", LocalDate.now(), TodoStatus.PENDING);
        todo2 = new Todo(2L, "Test Todo 2", LocalDate.now().plusDays(1), TodoStatus.IN_PROGRESS);
    }

    @Test
    void findAllTodos_shouldReturnListOfTodos() {
        when(todoRepository.findAll()).thenReturn(Arrays.asList(todo1, todo2));
        List<Todo> todos = todoService.findAllTodos();
        assertEquals(2, todos.size());
        assertEquals(todo1.getTitle(), todos.get(0).getTitle());
        verify(todoRepository).findAll();
    }

    @Test
    void findTodoById_shouldReturnTodo_whenFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        Optional<Todo> foundTodo = todoService.findTodoById(1L);
        assertTrue(foundTodo.isPresent());
        assertEquals(todo1.getTitle(), foundTodo.get().getTitle());
        verify(todoRepository).findById(1L);
    }

    @Test
    void findTodoById_shouldReturnEmpty_whenNotFound() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());
        Optional<Todo> foundTodo = todoService.findTodoById(3L);
        assertFalse(foundTodo.isPresent());
        verify(todoRepository).findById(3L);
    }

    @Test
    void createTodo_shouldSaveAndReturnTodoWithPendingStatus() {
        String title = "New Created Todo";
        LocalDate deadline = LocalDate.now().plusDays(5);

        // ArgumentCaptor を使用して保存される Todo オブジェクトをキャプチャ
        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        // save メソッドが呼ばれた際に、キャプチャした引数をそのまま返すように設定
        when(todoRepository.save(todoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Todo createdTodo = todoService.createTodo(title, deadline);

        assertNotNull(createdTodo);
        assertEquals(title, createdTodo.getTitle());
        assertEquals(deadline, createdTodo.getDeadline());
        assertEquals(TodoStatus.PENDING, createdTodo.getStatus()); // Default status

        // キャプチャされた Todo オブジェクトの検証 (IDはnullのままのはず)
        assertNull(todoCaptor.getValue().getId());
        assertEquals(title, todoCaptor.getValue().getTitle());
        assertEquals(deadline, todoCaptor.getValue().getDeadline());
        assertEquals(TodoStatus.PENDING, todoCaptor.getValue().getStatus());

        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void createTodo_withEmptyTitle_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.createTodo("", LocalDate.now());
        });
        assertEquals("Title cannot be empty", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }


    @Test
    void saveTodo_shouldSaveAndReturnTodo() {
        when(todoRepository.save(todo1)).thenReturn(todo1);
        Todo savedTodo = todoService.saveTodo(todo1);
        assertNotNull(savedTodo);
        assertEquals(todo1.getTitle(), savedTodo.getTitle());
        verify(todoRepository).save(todo1);
    }

    @Test
    void saveTodo_withNullTodo_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.saveTodo(null);
        });
        assertEquals("Todo cannot be null", exception.getMessage());
         verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void saveTodo_withNullTitle_shouldThrowIllegalArgumentException() {
        todo1.setTitle(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.saveTodo(todo1);
        });
        assertEquals("Title cannot be empty for Todo", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void saveTodo_withNullStatus_shouldSetDefaultStatusAndSave() {
        todo1.setStatus(null);
        when(todoRepository.save(todo1)).thenReturn(todo1);
        Todo savedTodo = todoService.saveTodo(todo1);
        assertEquals(TodoStatus.PENDING, savedTodo.getStatus());
        verify(todoRepository).save(todo1);
    }


    @Test
    void deleteTodoById_shouldCallRepositoryDelete_whenExists() {
        when(todoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(todoRepository).deleteById(1L);
        todoService.deleteTodoById(1L);
        verify(todoRepository).deleteById(1L);
    }

    @Test
    void deleteTodoById_shouldNotCallRepositoryDelete_whenNotExists() {
        when(todoRepository.existsById(3L)).thenReturn(false);
        todoService.deleteTodoById(3L);
        verify(todoRepository, never()).deleteById(3L);
    }


    @Test
    void changeTodoStatus_shouldUpdateStatus_whenTodoExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo updatedTodo = todoService.changeTodoStatus(1L, TodoStatus.COMPLETED);

        assertEquals(TodoStatus.COMPLETED, updatedTodo.getStatus());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(todo1);
    }

    @Test
    void changeTodoStatus_shouldThrowException_whenTodoNotExists() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.changeTodoStatus(3L, TodoStatus.COMPLETED);
        });
        assertEquals("Todo not found with id: 3", exception.getMessage());
        verify(todoRepository).findById(3L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void cycleTodoStatus_fromPendingToInProgress_whenTodoExists() {
        todo1.setStatus(TodoStatus.PENDING);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo cycledTodo = todoService.cycleTodoStatus(1L);

        assertEquals(TodoStatus.IN_PROGRESS, cycledTodo.getStatus());
        verify(todoRepository).save(todo1);
    }

    @Test
    void cycleTodoStatus_fromInProgressToCompleted_whenTodoExists() {
        todo1.setStatus(TodoStatus.IN_PROGRESS);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo cycledTodo = todoService.cycleTodoStatus(1L);

        assertEquals(TodoStatus.COMPLETED, cycledTodo.getStatus());
        verify(todoRepository).save(todo1);
    }

    @Test
    void cycleTodoStatus_fromCompletedToPending_whenTodoExists() {
        todo1.setStatus(TodoStatus.COMPLETED);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo cycledTodo = todoService.cycleTodoStatus(1L);

        assertEquals(TodoStatus.PENDING, cycledTodo.getStatus());
        verify(todoRepository).save(todo1);
    }

    @Test
    void cycleTodoStatus_whenCancelled_shouldNotChangeStatus() {
        todo1.setStatus(TodoStatus.CANCELLED);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
         when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0)); // save is still called

        Todo cycledTodo = todoService.cycleTodoStatus(1L);

        assertEquals(TodoStatus.CANCELLED, cycledTodo.getStatus());
        verify(todoRepository).save(todo1); // save is called, but status remains CANCELLED
    }


    @Test
    void cycleTodoStatus_shouldThrowException_whenTodoNotExists() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.cycleTodoStatus(3L);
        });
        assertEquals("Todo not found with id: 3", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void updateTodoDetails_shouldUpdateFields_whenTodoExists() {
        String newTitle = "Updated Title";
        LocalDate newDeadline = LocalDate.now().plusDays(10);
        TodoStatus newStatus = TodoStatus.COMPLETED;

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo updatedTodo = todoService.updateTodoDetails(1L, newTitle, newDeadline, newStatus);

        assertEquals(newTitle, updatedTodo.getTitle());
        assertEquals(newDeadline, updatedTodo.getDeadline());
        assertEquals(newStatus, updatedTodo.getStatus());
        verify(todoRepository).save(todo1);
    }

    @Test
    void updateTodoDetails_withEmptyTitle_shouldThrowIllegalArgumentException() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1)); // Need to mock findById for this test
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.updateTodoDetails(1L, " ", LocalDate.now(), TodoStatus.PENDING);
        });
        assertEquals("New title cannot be empty", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void updateTodoDetails_withNullStatus_shouldThrowIllegalArgumentException() {
         when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1)); // Need to mock findById for this test
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.updateTodoDetails(1L, "A Title", LocalDate.now(), null);
        });
        assertEquals("New status cannot be null", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }


    @Test
    void updateTodoDetails_shouldThrowException_whenTodoNotExists() {
        when(todoRepository.findById(3L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            todoService.updateTodoDetails(3L, "Any Title", LocalDate.now(), TodoStatus.PENDING);
        });
        assertEquals("Todo not found with id: 3", exception.getMessage());
        verify(todoRepository, never()).save(any(Todo.class));
    }
}
