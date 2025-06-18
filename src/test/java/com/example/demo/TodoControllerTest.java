package com.example.demo;

import com.example.demo.application.service.TodoService;
import com.example.demo.domain.model.Todo;
import com.example.demo.domain.model.TodoStatus;
import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor; // Not strictly needed if we verify service calls directly
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
// import java.util.Optional; // Service layer handles this

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
// import static org.junit.jupiter.api.Assertions.assertEquals; // Not for controller tests directly on Todo props
// import static org.junit.jupiter.api.Assertions.assertNull; // Not for controller tests directly on Todo props
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link TodoController} のユニットテストクラス。
 * MockMvcを使用してコントローラーの各エンドポイントの動作をテストし、
 * {@link TodoService} の呼び出しを検証します。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTPリクエストをシミュレートするためのMockMvcオブジェクト

    @MockBean
    private TodoService todoService; // TodoServiceのモックオブジェクト

    /**
     * GET /todos エンドポイントのテスト。
     * TodoService.findAllTodos() が呼ばれ、結果がモデルに追加され、正しいビュー名("todos")が返されることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void getAllTodos_shouldCallServiceAndReturnTodosViewWithData() throws Exception {
        Todo todo1 = new Todo(1L, "Service Todo 1", null, TodoStatus.PENDING);
        Todo todo2 = new Todo(2L, "Service Todo 2", LocalDate.now().plusDays(1), TodoStatus.COMPLETED);
        when(todoService.findAllTodos()).thenReturn(Arrays.asList(todo1, todo2));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attribute("todos", hasSize(2)))
                .andExpect(model().attribute("statuses", TodoStatus.values())) // Verify statuses attribute
                .andExpect(content().string(containsString("Service Todo 1")))
                .andExpect(content().string(containsString("Service Todo 2")));

        verify(todoService).findAllTodos();
    }

    /**
     * POST /todos エンドポイントのテスト（正常系、期限なし）。
     * TodoService.createTodo() が適切な引数で呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void createTodo_shouldCallServiceAndRedirect() throws Exception {
        String title = "New Service Todo";
        // モックされたcreateTodoがnullを返しても、コントローラーはリダイレクトする
        when(todoService.createTodo(eq(title), isNull(LocalDate.class))).thenReturn(null);

        mockMvc.perform(post("/todos").param("title", title))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).createTodo(eq(title), isNull(LocalDate.class));
    }

    /**
     * POST /todos エンドポイントのテスト（正常系、期限あり）。
     * TodoService.createTodo() が適切な引数（期限含む）で呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void createTodo_withDeadline_shouldCallServiceWithDeadlineAndRedirect() throws Exception {
        String title = "New Todo With Deadline";
        String deadlineString = "2024-12-31";
        LocalDate deadline = LocalDate.parse(deadlineString);
        when(todoService.createTodo(eq(title), eq(deadline))).thenReturn(null);

        mockMvc.perform(post("/todos")
                        .param("title", title)
                        .param("deadline", deadlineString))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).createTodo(eq(title), eq(deadline));
    }

    /**
     * POST /todos エンドポイントのテスト（タイトルが空の場合）。
     * タイトルが空の場合、TodoService.createTodo() が呼ばれずにリダイレクトされることを確認します。
     * (コントローラーレベルでの基本的なバリデーション)
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void createTodo_withEmptyTitle_shouldNotCallServiceAndRedirect() throws Exception {
        mockMvc.perform(post("/todos").param("title", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService, never()).createTodo(anyString(), any());
    }

    /**
     * GET /todos/{id}/toggleStatus エンドポイントのテスト。
     * TodoService.cycleTodoStatus() が呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void toggleStatus_shouldCallServiceAndRedirect() throws Exception {
        Long todoId = 1L;
        // cycleTodoStatusがTodoオブジェクトを返すが、コントローラーはそれを使用せずリダイレクトする
        when(todoService.cycleTodoStatus(todoId)).thenReturn(new Todo(todoId, "Toggled", null, TodoStatus.IN_PROGRESS));

        mockMvc.perform(get("/todos/" + todoId + "/toggleStatus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).cycleTodoStatus(todoId);
    }

    /**
     * GET /todos/{id}/toggleStatus エンドポイントのテスト（Serviceが例外をスローする場合）。
     * ServiceがIllegalArgumentExceptionをスローした場合でも、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void toggleStatus_whenServiceThrowsException_shouldRedirect() throws Exception {
        Long todoId = 99L;
        when(todoService.cycleTodoStatus(todoId)).thenThrow(new IllegalArgumentException("Not found"));

        mockMvc.perform(get("/todos/" + todoId + "/toggleStatus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).cycleTodoStatus(todoId);
    }

    /**
     * POST /todos/{id}/status エンドポイントのテスト。
     * TodoService.changeTodoStatus() が呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void setTodoStatus_shouldCallServiceAndRedirect() throws Exception {
        Long todoId = 1L;
        TodoStatus newStatus = TodoStatus.COMPLETED;
        when(todoService.changeTodoStatus(todoId, newStatus)).thenReturn(new Todo(todoId, "Status Changed", null, newStatus));

        mockMvc.perform(post("/todos/" + todoId + "/status")
                        .param("status", newStatus.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).changeTodoStatus(todoId, newStatus);
    }

    /**
     * POST /todos/{id}/updateDetails エンドポイントのテスト。
     * TodoService.updateTodoDetails() が呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void updateTodoDetails_shouldCallServiceAndRedirect() throws Exception {
        Long todoId = 1L;
        String newTitle = "Updated Title";
        String newDeadlineString = "2025-01-01";
        LocalDate newDeadline = LocalDate.parse(newDeadlineString);
        TodoStatus newStatus = TodoStatus.IN_PROGRESS;

        when(todoService.updateTodoDetails(eq(todoId), eq(newTitle), eq(newDeadline), eq(newStatus)))
            .thenReturn(new Todo(todoId, newTitle, newDeadline, newStatus));

        mockMvc.perform(post("/todos/" + todoId + "/updateDetails")
                        .param("title", newTitle)
                        .param("deadline", newDeadlineString)
                        .param("status", newStatus.name())) // status パラメータを追加
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).updateTodoDetails(eq(todoId), eq(newTitle), eq(newDeadline), eq(newStatus));
    }

    /**
     * POST /todos/{id}/updateDetails エンドポイントのテスト（タイトルが空の場合）。
     * コントローラレベルのバリデーションにより、TodoService.updateTodoDetails() が呼ばれずにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void updateTodoDetails_withEmptyTitle_shouldNotCallServiceAndRedirect() throws Exception {
        Long todoId = 1L;
        String newDeadlineString = "2025-01-01";
        TodoStatus newStatus = TodoStatus.PENDING;


        mockMvc.perform(post("/todos/" + todoId + "/updateDetails")
                        .param("title", "  ") // Empty title
                        .param("deadline", newDeadlineString)
                        .param("status", newStatus.name())) // status パラメータを追加
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService, never()).updateTodoDetails(anyLong(), anyString(), any(LocalDate.class), any(TodoStatus.class));
    }


    /**
     * GET /todos/{id}/delete エンドポイントのテスト。
     * TodoService.deleteTodoById() が呼ばれ、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void deleteTodo_shouldCallServiceAndRedirect() throws Exception {
        Long todoId = 1L;
        // deleteTodoByIdはvoidなので、doNothing()を使用（デフォルトの動作ですが明示的に）
        doNothing().when(todoService).deleteTodoById(todoId);

        mockMvc.perform(get("/todos/" + todoId + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));

        verify(todoService).deleteTodoById(todoId);
    }
}
