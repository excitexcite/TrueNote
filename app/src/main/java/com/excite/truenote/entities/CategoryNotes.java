package com.excite.truenote.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CategoryNotes {

    @Embedded
    public Category mCategory;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "noteId"
    )
    public List<Note> mNotes;

}
