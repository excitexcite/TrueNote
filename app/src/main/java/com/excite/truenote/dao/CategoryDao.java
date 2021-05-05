package com.excite.truenote.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;


import com.excite.truenote.entities.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Transaction
    @Query("SELECT * FROM categories ORDER BY categoryId DESC")
    List<Category> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category category);

    @Delete
    void deleteCategory(Category category);



}
