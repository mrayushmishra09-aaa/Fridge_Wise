package com.example.fridgewise;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    long insert(TodoItem todoItem);

    @Update
    void update(TodoItem todoItem);

    @Delete
    void delete(TodoItem todoItem);

    @Query("SELECT * FROM todo_items ORDER BY id DESC")
    List<TodoItem> getAllTodos();

    @Query("SELECT * FROM todo_items WHERE isCompleted = 0 ORDER BY id DESC")
    List<TodoItem> getPendingTodos();

    @Query("SELECT * FROM todo_items WHERE isCompleted = 1 ORDER BY id DESC")
    List<TodoItem> getCompletedTodos();
}
