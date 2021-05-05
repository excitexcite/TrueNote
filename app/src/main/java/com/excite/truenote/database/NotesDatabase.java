package com.excite.truenote.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.excite.truenote.dao.NoteDao;
import com.excite.truenote.entities.Category;
import com.excite.truenote.entities.Note;
import com.excite.truenote.entities.NoteCategory;

@Database(entities = {Note.class, Category.class, NoteCategory.class}, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase sNotesDatabase;
    private static String DB_NAME = "test_db";

    public static synchronized NotesDatabase getDatabase(Context context) {
        if (sNotesDatabase == null) {
            sNotesDatabase = Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    DB_NAME
            ).build();
        }
        return  sNotesDatabase;
    }

    public abstract NoteDao noteDao();

}
