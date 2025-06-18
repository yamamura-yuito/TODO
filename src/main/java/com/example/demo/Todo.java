package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODOアイテムを表すエンティティクラス。
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    /**
     * TODOアイテムのID。データベースによって自動生成されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TODOアイテムのタイトル。
     */
    private String title;

    /**
     * TODOアイテムの完了状態。trueなら完了、falseなら未完了。
     */
    private boolean completed;
}
