package com.excite.truenote.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.excite.truenote.R;
import com.excite.truenote.activities.CreateNoteActivity;
import com.excite.truenote.adapters.NotesAdapter;
import com.excite.truenote.database.NotesDatabase;
import com.excite.truenote.entities.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    private RecyclerView notesRecyclerView;
    private List<Note> mNoteList;
    private NotesAdapter mNotesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteToMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );
        
        mNoteList = new ArrayList<>();
        mNotesAdapter = new NotesAdapter(mNoteList);
        notesRecyclerView.setAdapter(mNotesAdapter);
        
        getNotes();
    }

    private void getNotes() {

        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //Log.d("MYNOTES", notes.toString());
                // если список пусть, значит осуществляется первый запуск приложения, то есть первый запрос к бд
                if (mNoteList.size() == 0) {
                    mNoteList.addAll(notes);
                    mNotesAdapter.notifyDataSetChanged();
                } else { // иначе приложение уже было запущено и необходимо лишь отобразить в списке добавленную заметку в начале списка
                    mNoteList.add(0, notes.get(0));
                    mNotesAdapter.notifyItemInserted(0);
                }
                notesRecyclerView.smoothScrollToPosition(0);
            }
        }

        new GetNoteTask().execute();

    }

    // без этой функции при создании заметки и возврата на главный экран созданная заметка не будет отображена
    // это связано с тем, что используется startActivityForResult() 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes();
        }
    }
}