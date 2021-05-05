package com.excite.truenote.entities;

import androidx.room.Entity;

import java.io.Serializable;

@Entity(tableName = "note_category", primaryKeys = {"noteId", "categoryId"})
public class NoteCategory implements Serializable {

    public int noteId;
    public int categoryId;

}
