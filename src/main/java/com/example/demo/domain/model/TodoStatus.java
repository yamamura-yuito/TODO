package com.example.demo.domain.model;

public enum TodoStatus {
    PENDING("未着手"),
    IN_PROGRESS("作業中"),
    COMPLETED("完了"),
    CANCELLED("キャンセル");

    private final String displayName;

    TodoStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
