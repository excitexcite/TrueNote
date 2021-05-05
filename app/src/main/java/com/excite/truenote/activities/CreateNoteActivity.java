package com.excite.truenote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.excite.truenote.R;
import com.excite.truenote.database.NotesDatabase;
import com.excite.truenote.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private EditText inputNoteTitle;
    private EditText inputNoteSubtitle;
    private EditText inputNoteText;
    private TextView textDateTime;
    private ImageView imageNote;

    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        imageNote = findViewById(R.id.imageNote);

        textDateTime.setText(
                // ex: Tuesday, 04 May 2021 16:42 PM
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).
                        format(new Date())
        );

        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        selectedImagePath = "";

        //initMiscellaneous();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_note, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.addImage: {
                // получения разрешения на доступ к файлам устройств (галерее)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                CreateNoteActivity.this,
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_STORAGE_PERMISSION
                        );
                    } else {
                        selectImage();
                    }
                }
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // если разрешение было получено, то откроется галерея
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else { // иначе тост с ошибкой
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString().trim());
        note.setSubtitle(inputNoteSubtitle.getText().toString().trim());
        note.setNoteText(inputNoteText.getText().toString().trim());
        note.setDateTime(textDateTime.getText().toString());
        note.setImagePath(selectedImagePath);

        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext())
                        .noteDao()
                        .insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior bottomSgeetBehaviour = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSgeetBehaviour.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSgeetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSgeetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String pathFile;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null) {
            pathFile = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            pathFile = cursor.getString(index);
            cursor.close();
        }
        return pathFile;
    }
}