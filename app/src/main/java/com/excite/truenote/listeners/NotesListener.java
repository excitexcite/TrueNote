package com.excite.truenote.listeners;

import com.excite.truenote.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note, int position);

}
