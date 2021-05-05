package com.excite.truenote.adapters;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excite.truenote.R;
import com.excite.truenote.entities.Note;
import com.excite.truenote.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> mNotes;
    private NotesListener mNotesListener;
    private Timer mTimer;
    private  List<Note> mNotesSource;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        mNotesListener = notesListener;
        mNotes = notes;
        mNotesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(mNotes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotesListener.onNoteClicked(mNotes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textSubtitle;
        TextView textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            imageNote = itemView.findViewById(R.id.imageNote);
            layoutNote = itemView.findViewById(R.id.layoutNote);
        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            // если картинка не была удалена
            if(note.getImagePath() != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }
        }

    }

    public void  searchNotes(final String searchKeyword) {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    mNotes = mNotesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note :mNotesSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    mNotes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

}
