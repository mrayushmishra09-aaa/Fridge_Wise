package com.example.fridgewise;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * DAO (Data Access Object)
 * This interface defines the operations you can perform on the "documents" table.
 */
@Dao
public interface DocumentDao {

    // 1. Get all documents, sorted by ID so the newest ones appear first (or last)
    @Query("SELECT * FROM documents ORDER BY id DESC")
    List<DocumentItem> getAllDocuments();

    // 2. Insert a new document
    @Insert
    void insert(DocumentItem document);

    // 3. Update an existing document (e.g., changing its name)
    @Update
    void update(DocumentItem document);

    // 4. Delete a document
    @Delete
    void delete(DocumentItem document);

    // 5. Bonus: Count documents (useful for that banner in your UI!)
    @Query("SELECT COUNT(*) FROM documents")
    int getCount();
}
