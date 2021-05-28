package com.excite.truenote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.excite.truenote.R;
import com.excite.truenote.activities.CreateNoteActivity;
import com.excite.truenote.adapters.NotesAdapter;
import com.excite.truenote.database.NotesDatabase;
import com.excite.truenote.entities.Note;
import com.excite.truenote.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    public static final String VIEW_UPDATE_KEY = "isViewOrUpdate";
    public static final String NOTE_KEY = "note";

    private String[] sortOptions = {"A-Z", "Z-A", "By last change date", "By category"};

    private RecyclerView notesRecyclerView;
    private List<Note> mNoteList;
    private NotesAdapter mNotesAdapter;

    private int noteClickedPosition = 1;

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
        mNotesAdapter = new NotesAdapter(mNoteList, this);
        notesRecyclerView.setAdapter(mNotesAdapter);

        // onCreate() срабатывает при запуске приложения, а при запуске приложения необходимо отобразить все доступные заметки
        // false - так как в данном случае не одна заметка не должна быть удалена
        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNotesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mNoteList.size() != 0) {
                    mNotesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.sortNotes: {
                showSortDialog();
                break;
            }
            case R.id.addCategory: {

                break;
            }
            case R.id.settings: {

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra(VIEW_UPDATE_KEY, true);
        intent.putExtra(NOTE_KEY, note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

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

                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    mNoteList.addAll(notes);
                    mNotesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    mNoteList.add(0, notes.get(0));
                    mNotesAdapter.notifyItemChanged(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    // в случае редактирования заметки вначале эта заметка удаляется, а затем в её же место
                    // вставляется отредактированная (эта же) заметка из БД
                    mNoteList.remove(noteClickedPosition);

                    // если после удаления и попытки вставить новую заметку выяснилось, что она удалена, то ничего не вставляем
                    // и просто обновляем экран
                    if (isNoteDeleted) {
                        mNotesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else { // иначе вставляем отредактированную заметку
                        mNoteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        mNotesAdapter.notifyItemChanged(noteClickedPosition);
                    }

                }
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
            // в случае возврата результата на экране уже отображены заметки, потому нужно добавить в перечню только одну заметку
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                // в случаее кода запроса на изменение заметки, нужно заменить лишь одну заметку
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra(CreateNoteActivity.NOTE_DELETE_KEY, false));
            }
        }
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_sort_option))
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.sort_option_selected) + sortOptions[which],
                                Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dialog =  builder.create();
        dialog.show();
    }

}