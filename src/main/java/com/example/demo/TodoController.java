package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * TODOアイテムに関するHTTPリクエストを処理するコントローラークラス。
 */
@Controller
public class TodoController {

    @Autowired
    private TodoRepository todoRepository; // TODOアイテムのデータアクセス用リポジトリ

    /**
     * すべてのTODOアイテムを取得し、一覧表示用のビューに渡します。
     *
     * @param model ビューに渡すデータを格納するモデル
     * @return TODOアイテム一覧表示用のビュー名 ("todos")
     */
    @GetMapping("/todos")
    public String getAllTodos(Model model) {
        model.addAttribute("todos", todoRepository.findAll());
        return "todos"; // Thymeleaf テンプレートの名前
    }

    /**
     * 新しいTODOアイテムを作成します。
     *
     * @param title 新しいTODOアイテムのタイトル
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @PostMapping("/todos")
    public String createTodo(@RequestParam String title) {
        if (title != null && !title.trim().isEmpty()) {
            todoRepository.save(new Todo(null, title, false));
        }
        return "redirect:/todos";
    }

    /**
     * 指定されたIDのTODOアイテムの完了状態をトグルします（未完了なら完了に、完了なら未完了に）。
     *
     * @param id 状態をトグルするTODOアイテムのID
     * @return TODOアイテム一覧ページへのリダイレクトパス ("/todos")
     */
    @GetMapping("/todos/{id}/toggle")
    public String toggleTodo(@PathVariable Long id) {
        Optional<Todo> todoOptional = todoRepository.findById(id);
        if (todoOptional.isPresent()) {
            Todo todo = todoOptional.get();
            todo.setCompleted(!todo.isCompleted());
            todoRepository.save(todo);
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
        todoRepository.deleteById(id);
        return "redirect:/todos";
    }
}
