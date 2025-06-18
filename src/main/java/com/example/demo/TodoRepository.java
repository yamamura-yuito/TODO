package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Todo} エンティティのCRUD操作を行うためのSpring Data JPA リポジトリインターフェース。
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
