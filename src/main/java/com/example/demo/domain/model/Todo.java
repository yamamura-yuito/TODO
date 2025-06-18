package com.example.demo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    @Column(nullable = false) // タイトルは必須とする例
    private String title;

    /**
     * TODOアイテムの期限。
     */
    private LocalDate deadline;

    /**
     * TODOアイテムのステータス。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // ステータスは必須とする例
    private TodoStatus status;

    // 'completed' フィールドは削除され、'status' フィールドに統合されました。
    // 必要に応じて、status が COMPLETED かどうかを判定するヘルパーメソッドを追加できます。
    // public boolean isCompleted() {
    //     return this.status == TodoStatus.COMPLETED;
    // }
}
