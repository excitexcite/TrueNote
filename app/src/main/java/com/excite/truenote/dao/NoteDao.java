package com.excite.truenote.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.excite.truenote.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Transaction
    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);

}
