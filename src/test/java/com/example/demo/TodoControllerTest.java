package com.example.demo;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link TodoController} のユニットテストクラス。
 * MockMvcを使用してコントローラーの各エンドポイントの動作をテストします。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTPリクエストをシミュレートするためのMockMvcオブジェクト

    @MockBean
    private TodoRepository todoRepository; // TodoRepositoryのモックオブジェクト

    /**
     * GET /todos エンドポイントのテスト。
     * 存在するTODOアイテムがモデルに追加され、正しいビュー名("todos")が返されることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void getAllTodos_shouldReturnTodosViewWithData() throws Exception {
        Todo todo1 = new Todo(1L, "Test Todo 1", false);
        Todo todo2 = new Todo(2L, "Test Todo 2", true);
        // todoRepository.findAll()が呼ばれた際に、事前に定義したTODOリストを返すように設定
        when(todoRepository.findAll()).thenReturn(Arrays.asList(todo1, todo2));

        mockMvc.perform(get("/todos")) // GET /todos リクエストを実行
                .andExpect(status().isOk()) // HTTPステータスが200 OKであることを期待
                .andExpect(view().name("todos")) // 返されるビュー名が "todos" であることを期待
                .andExpect(model().attributeExists("todos")) // モデルに "todos" という名前の属性が存在することを期待
                .andExpect(model().attribute("todos", hasSize(2))) // "todos"属性のサイズが2であることを期待
                .andExpect(content().string(containsString("Test Todo 1"))) // レスポンス内容に "Test Todo 1" が含まれることを期待
                .andExpect(content().string(containsString("Test Todo 2"))); // レスポンス内容に "Test Todo 2" が含まれることを期待
    }

    /**
     * POST /todos エンドポイントのテスト（正常系）。
     * 新しいTODOが作成され、リポジトリに保存され、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void createTodo_shouldCreateTodoAndRedirect() throws Exception {
        mockMvc.perform(post("/todos").param("title", "New Todo")) // POST /todos リクエストを実行（パラメータ title="New Todo"）
                .andExpect(status().is3xxRedirection()) // HTTPステータスがリダイレクト(3xx)であることを期待
                .andExpect(redirectedUrl("/todos")); // リダイレクト先URLが "/todos" であることを期待

        ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);
        // todoRepository.save()が呼ばれたことを検証し、渡されたTodoオブジェクトをキャプチャ
        verify(todoRepository).save(todoArgumentCaptor.capture());
        assertEquals("New Todo", todoArgumentCaptor.getValue().getTitle()); // 保存されたTodoのタイトルが正しいことを確認
        assertFalse(todoArgumentCaptor.getValue().isCompleted()); // 保存されたTodoが未完了状態であることを確認
    }

    /**
     * POST /todos エンドポイントのテスト（タイトルが空の場合）。
     * タイトルが空または空白の場合、TODOが作成されず、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void createTodo_withEmptyTitle_shouldNotCreateTodoAndRedirect() throws Exception {
        mockMvc.perform(post("/todos").param("title", " ")) // POST /todos リクエストを実行（パラメータ title=" "）
                .andExpect(status().is3xxRedirection()) // HTTPステータスがリダイレクト(3xx)であることを期待
                .andExpect(redirectedUrl("/todos")); // リダイレクト先URLが "/todos" であることを期待

        // todoRepository.save()が一度も呼ばれなかったことを検証
        verify(todoRepository, never()).save(any(Todo.class));
    }

    /**
     * GET /todos/{id}/toggle エンドポイントのテスト（正常系）。
     * 指定されたTODOの完了状態がトグルされ、リポジトリに保存され、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void toggleTodo_shouldToggleCompletionAndRedirect() throws Exception {
        Todo todo = new Todo(1L, "Test Todo", false);
        // todoRepository.findById(1L)が呼ばれた際に、事前に定義したTodoオブジェクトをOptionalでラップして返すように設定
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        mockMvc.perform(get("/todos/1/toggle")) // GET /todos/1/toggle リクエストを実行
                .andExpect(status().is3xxRedirection()) // HTTPステータスがリダイレクト(3xx)であることを期待
                .andExpect(redirectedUrl("/todos")); // リダイレクト先URLが "/todos" であることを期待

        ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);
        // todoRepository.save()が呼ばれたことを検証し、渡されたTodoオブジェクトをキャプチャ
        verify(todoRepository).save(todoArgumentCaptor.capture());
        assertTrue(todoArgumentCaptor.getValue().isCompleted()); // 保存されたTodoが完了状態であることを確認
        assertEquals(1L, todoArgumentCaptor.getValue().getId()); // 保存されたTodoのIDが正しいことを確認
    }

    /**
     * GET /todos/{id}/toggle エンドポイントのテスト（TODOが存在しない場合）。
     * 指定されたIDのTODOが存在しない場合、エラーにならずに/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void toggleTodo_whenTodoNotFound_shouldRedirectWithoutError() throws Exception {
        // todoRepository.findById(99L)が呼ばれた際に、空のOptionalを返すように設定 (TODOが見つからないケース)
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/todos/99/toggle")) // GET /todos/99/toggle リクエストを実行
                .andExpect(status().is3xxRedirection()) // HTTPステータスがリダイレクト(3xx)であることを期待
                .andExpect(redirectedUrl("/todos")); // リダイレクト先URLが "/todos" であることを期待

        // todoRepository.save()が一度も呼ばれなかったことを検証
        verify(todoRepository, never()).save(any(Todo.class));
    }

    /**
     * GET /todos/{id}/delete エンドポイントのテスト。
     * 指定されたTODOが削除され、/todosにリダイレクトされることを確認します。
     * @throws Exception MockMvcの実行時例外
     */
    @Test
    public void deleteTodo_shouldDeleteTodoAndRedirect() throws Exception {
        // このテストケースでは、コントローラーはfindByIdを呼ばずに直接deleteByIdを呼ぶため、findByIdのモックは不要。
        // when(todoRepository.findById(1L)).thenReturn(Optional.of(new Todo(1L, "To be deleted", false)));

        mockMvc.perform(get("/todos/1/delete")) // GET /todos/1/delete リクエストを実行
                .andExpect(status().is3xxRedirection()) // HTTPステータスがリダイレクト(3xx)であることを期待
                .andExpect(redirectedUrl("/todos")); // リダイレクト先URLが "/todos" であることを期待

        // todoRepository.deleteById(1L)が呼ばれたことを検証
        verify(todoRepository).deleteById(1L);
    }
}
