package com.example.demo;

import com.example.demo.application.service.TodoService;
import com.example.demo.domain.model.Todo; // Keep this if Todo is used in method signatures or model attributes
import com.example.demo.domain.model.TodoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays; // For TodoStatus.values() - not directly, but useful for context
// import java.util.Optional; // No longer needed if service handles Optional internally or throws exceptions

/**
 * TODOアイテムに関するHTTPリクエストを処理するコントローラークラス。
 * アプリケーションサービス層 (TodoService) を介してビジネスロジックを実行します。
 */
@Controller
public class TodoController {

    private final TodoService todoService;

    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * すべてのTODOアイテムを取得し、一覧表示用のビューに渡します。
     * また、ステータス選択用に {@link TodoStatus} の全値もビューに渡します。
     *
     * @param model ビューに渡すデータを格納するモデル
     * @return TODOアイテム一覧表示用のビュー名 ("todos")
     */
    @GetMapping("/todos")
    public String getAllTodos(Model model) {
        model.addAttribute("todos", todoService.findAllTodos());
        model.addAttribute("statuses", TodoStatus.values()); // ステータスリストをモデルに追加
        return "todos"; // Thymeleaf テンプレートの名前
    }

    /**
     * 新しいTODOアイテムを作成します。
     *
     * @param title 新しいTODOアイテムのタイトル
     * @param deadline 新しいTODOアイテムの期限（文字列形式 YYYY-MM-DD）。省略可能。
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @PostMapping("/todos")
    public String createTodo(@RequestParam String title,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        // titleのバリデーションはService層に任せることもできる
        if (title != null && !title.trim().isEmpty()) {
            todoService.createTodo(title, deadline);
        }
        return "redirect:/todos";
    }

    /**
     * 指定されたIDのTODOアイテムのステータスを次の状態に進めます。
     * (例: PENDING -> IN_PROGRESS -> COMPLETED -> PENDING)
     *
     * @param id ステータスを変更するTODOアイテムのID
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @GetMapping("/todos/{id}/toggleStatus")
    public String toggleStatus(@PathVariable Long id) {
        try {
            todoService.cycleTodoStatus(id);
        } catch (IllegalArgumentException e) {
            // Log error or add flash attribute to show error message on redirect
            System.err.println("Error cycling status for TODO " + id + ": " + e.getMessage());
        }
        return "redirect:/todos";
    }

    /**
     * 指定されたIDのTODOアイテムのステータスを特定の値に設定します。
     *
     * @param id ステータスを変更するTODOアイテムのID
     * @param status 設定する新しいステータス
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @PostMapping("/todos/{id}/status")
    public String setTodoStatus(@PathVariable Long id, @RequestParam TodoStatus status) {
        try {
            todoService.changeTodoStatus(id, status);
        } catch (IllegalArgumentException e) {
            // Log error or add flash attribute
            System.err.println("Error setting status for TODO " + id + " to " + status + ": " + e.getMessage());
        }
        return "redirect:/todos";
    }

    /**
     * 指定されたIDのTODOアイテムの詳細（タイトル、期限）を更新します。
     *
     * @param id 更新するTODOアイテムのID
     * @param title 新しいタイトル
     * @param deadline 新しい期限
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @PostMapping("/todos/{id}/updateDetails")
    public String updateTodoDetails(@PathVariable Long id,
                                    @RequestParam String title,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
                                    @RequestParam TodoStatus status) { // status パラメータを追加
        try {
            if (title == null || title.trim().isEmpty()) {
                // Basic validation in controller, or rely on service layer
                // Consider adding a flash attribute with an error message
                return "redirect:/todos"; // Or redirect to an edit page with an error
            }
            todoService.updateTodoDetails(id, title, deadline, status); // サービス呼び出しを更新
        } catch (IllegalArgumentException e) {
            // Log error or add flash attribute
            System.err.println("Error updating details for TODO " + id + ": " + e.getMessage());
        }
        return "redirect:/todos";
    }


    /**
     * 指定されたIDのTODOアイテムを削除します。
     *
     * @param id 削除するTODOアイテムのID
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @GetMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id) {
        // Service layer's deleteTodoById might not throw an exception if not found,
        // depending on its implementation (as in TodoServiceImpl example).
        todoService.deleteTodoById(id);
        return "redirect:/todos";
    }
}
