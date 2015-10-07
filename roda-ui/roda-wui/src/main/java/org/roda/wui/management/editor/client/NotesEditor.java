/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Note;
import org.roda.core.data.eadc.Notes;
import org.roda.core.data.eadc.P;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class NotesEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final VerticalPanel layout;

  private final WUIButton addNote;
  private final VerticalPanel notesLayout;
  private final List<NoteEditor> noteEditors;
  private final List<ChangeListener> listeners;

  /**
   * Listener for note
   * 
   */
  public interface NoteListener extends ChangeListener {
    /**
     * Called when an note is removed
     * 
     * @param sender
     */
    public void onRemove(NoteEditor sender);
  }

  /**
   * Editor for notes
   */
  public NotesEditor() {
    layout = new VerticalPanel();
    addNote = new WUIButton(constants.notesAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    notesLayout = new VerticalPanel();

    layout.add(notesLayout);
    layout.add(addNote);

    noteEditors = new Vector<NoteEditor>();
    listeners = new Vector<ChangeListener>();

    addNote.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createNoteEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.addStyleName("wui-editor-notes");
    addNote.addStyleName("wui-editor-notes-add");
    notesLayout.addStyleName("wui-editor-notes-layout");
  }

  protected NoteEditor createNoteEditor() {
    NoteEditor editor = new NoteEditor();
    editor.addNoteListener(new NoteListener() {
      public void onRemove(NoteEditor sender) {
        noteEditors.remove(sender);
        updateLayout();
        NotesEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        NotesEditor.this.onChange(sender);
      }
    });
    noteEditors.add(editor);
    return editor;
  }

  protected void updateLayout() {
    notesLayout.clear();
    for (NoteEditor noteEditor : noteEditors) {
      notesLayout.add(noteEditor.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<Note> notes = new Vector<Note>();
    for (NoteEditor noteEditor : noteEditors) {
      Note note = noteEditor.getNote();
      if (note != null) {
        notes.add(note);
      }
    }
    return notes.size() == 0 ? null : new Notes(notes.toArray(new Note[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof Notes) {
      Notes notes = (Notes) value;
      noteEditors.clear();
      for (int i = 0; i < notes.getNotes().length; i++) {
        Note note = notes.getNotes()[i];
        if (note.getP() != null) {
          NoteEditor noteEditor = createNoteEditor();
          noteEditor.setNote(note);
        }
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return noteEditors.size() == 0;
  }

  public boolean isValid() {
    return true;
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    FlexTable panel = new FlexTable();
    Notes notes = (Notes) value;
    for (int i = 0; i < notes.getNotes().length; i++) {
      Note note = notes.getNotes()[i];
      if (note.getP() != null) {
        Label column0 = new Label(note.getP().getText());
        panel.setWidget(i, 0, column0);
        panel.getColumnFormatter().setWidth(0, "100%");
        column0.addStyleName("wui-editor-notes-readonly-column0");
      }
    }

    panel.addStyleName("wui-editor-notes-readonly");
    return panel;
  }

  /**
   * Editor for note
   * 
   */
  public class NoteEditor {

    private final List<NoteListener> listeners;

    private final HorizontalPanel layout;

    private final VerticalPanel subLayout;
    private final TextArea noteArea;
    private final Image remove;

    public NoteEditor() {
      layout = new HorizontalPanel();
      subLayout = new VerticalPanel();
      noteArea = new TextArea();
      remove = commonImageBundle.minus().createImage();

      subLayout.add(noteArea);

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<NoteListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          NoteEditor.this.onChange(sender);
        }

      };

      noteArea.addChangeListener(changeListener);

      layout.setCellWidth(subLayout, "100%");
      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-note");
      subLayout.setCellWidth(noteArea, "100%");
      subLayout.addStyleName("wui-editor-note-center");
      remove.addStyleName("wui-editor-note-remove");
      noteArea.addStyleName("wui-editor-note-area");
    }

    /**
     * Get editor widget
     * 
     * @return
     */
    public Widget getWidget() {
      return layout;
    }

    /**
     * Set the note value
     * 
     * @param note
     */
    public void setNote(Note note) {
      if (note.getP().getText() != null) {
        noteArea.setText(note.getP().getText());
      }
    }

    /**
     * Get the note value
     * 
     * @return
     */
    public Note getNote() {
      Note note = null;
      if (!noteArea.getText().isEmpty()) {
        note = new Note(new P(noteArea.getText()));
      }
      return note;
    }

    /**
     * Add a note listener
     * 
     * @param listener
     */
    public void addNoteListener(NoteListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a note listener
     * 
     * @param listener
     */
    public void removeNoteListener(NoteListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (NoteListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (NoteListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
