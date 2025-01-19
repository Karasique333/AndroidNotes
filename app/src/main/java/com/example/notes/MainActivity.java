package com.example.notes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NotePrefs";
    private static final String KEY_NOTE_COUNT = "NoteCount";
    private LinearLayout notesContainer;
    private List<Note> noteList;
    private EditText titleEditText;
    private EditText contentEditText;
    private EditText searchEditText;
    private Button saveButton;
    private boolean isEditing = false;
    private Note editingNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setAppTheme();

        setContentView(R.layout.activity_main);

        notesContainer = findViewById(R.id.notesContainer);
        saveButton = findViewById(R.id.saveButton);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        searchEditText = findViewById(R.id.titleSearch);
        ImageButton themeSwitchButton = findViewById(R.id.themeSwitchButton);

        noteList = new ArrayList<>();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    updateNote();
                } else {
                    saveNote();
                }
            }
        });


        themeSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheme();
            }
        });


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadNotesFromPreferences();
        displayNotes();
    }

    private void displayNotes() {
        notesContainer.removeAllViews();
        for (Note note : noteList) {
            createNoteView(note);
        }
    }

    private void loadNotesFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int noteCount = sharedPreferences.getInt(KEY_NOTE_COUNT, 0);

        for (int i = 0; i < noteCount; i++) {
            String title = sharedPreferences.getString("note_title_" + i, "");
            String content = sharedPreferences.getString("note_content_" + i, "");

            Note note = new Note(title, content);
            noteList.add(note);
        }
    }

    private void saveNote() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        if (!title.isEmpty() && !content.isEmpty()) {
            Note note = new Note(title, content);
            noteList.add(note);
            saveNotesToPreferences();
            createNoteView(note);
            clearInputFields();
        }
    }

    private void updateNote() {
        if (editingNote != null) {
            editingNote.setTitle(titleEditText.getText().toString());
            editingNote.setContent(contentEditText.getText().toString());
            saveNotesToPreferences();
            refreshNoteViews();
            clearInputFields();
            saveButton.setText("Сохранить заметку");
            isEditing = false;
            editingNote = null;
        }
    }

    private void clearInputFields() {
        titleEditText.getText().clear();
        contentEditText.getText().clear();
    }

    private void createNoteView(final Note note) {
        View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView titleTextView = noteView.findViewById(R.id.titleTextView);
        TextView contentTextView = noteView.findViewById(R.id.contentTextView);

        titleTextView.setText(note.getTitle());
        contentTextView.setText(note.getContent());

        noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditNoteDialog(note);
            }
        });

        noteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(note);
                return true;
            }
        });

        notesContainer.addView(noteView);
    }

    private void openEditNoteDialog(Note note) {
        titleEditText.setText(note.getTitle());
        contentEditText.setText(note.getContent());
        saveButton.setText("Обновить заметку");
        isEditing = true;
        editingNote = note;
    }

    private void showDeleteDialog(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удалить заметку.");
        builder.setMessage("Вы хотите удалить эту заметку?");
        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteAndRefresh(note);
            }
        });
        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);


                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);


                int color;
                if (isDarkTheme) {
                    // Для тёмной темы
                    color = ContextCompat.getColor(getApplicationContext(), R.color.white);
                } else {
                    // Для светлой темы
                    color = ContextCompat.getColor(getApplicationContext(), R.color.black);
                }


                positiveButton.setTextColor(color);
                negativeButton.setTextColor(color);
            }
        });

        dialog.show();

    }


    private void deleteNoteAndRefresh(Note note) {
        noteList.remove(note);
        saveNotesToPreferences();
        refreshNoteViews();
    }

    private void refreshNoteViews() {
        notesContainer.removeAllViews();
        displayNotes();
    }

    private void saveNotesToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NOTE_COUNT, noteList.size());
        for (int i = 0; i < noteList.size(); i++) {
            Note note = noteList.get(i);
            editor.putString("note_title_" + i, note.getTitle());
            editor.putString("note_content_" + i, note.getContent());
        }
        editor.apply();
    }

    private void filterNotes(String query) {
        notesContainer.removeAllViews();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(query.toLowerCase())) {
                createNoteView(note);
            }
        }
    }

    private void toggleTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

        if (isDarkTheme) {
            setTheme(R.style.Theme_MyNotes_Dark);
        } else {
            setTheme(R.style.Theme_MyNotes_Light);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isDarkTheme", !isDarkTheme);
        editor.apply();

        recreate();
    }

    private void setAppTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

        if (isDarkTheme) {
            setTheme(R.style.Theme_MyNotes_Dark);
        } else {
            setTheme(R.style.Theme_MyNotes_Light);
        }
    }
}
