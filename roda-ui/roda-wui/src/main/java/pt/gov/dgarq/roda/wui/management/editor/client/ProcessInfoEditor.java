/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.Archref;
import pt.gov.dgarq.roda.core.data.eadc.Archrefs;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Note;
import pt.gov.dgarq.roda.core.data.eadc.Notes;
import pt.gov.dgarq.roda.core.data.eadc.P;
import pt.gov.dgarq.roda.core.data.eadc.ProcessInfo;
import pt.gov.dgarq.roda.wui.common.client.widgets.RedactionTypePicker;
import pt.gov.dgarq.roda.wui.common.client.widgets.RedactionTypePicker.RedactionType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
public class ProcessInfoEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final HorizontalPanel reason;
  private final Label reasonLabel;
  private final TextArea reasonBox;
  private final List<ProcessInfoEntryEditor> processInfoEntries;
  private final VerticalPanel processInfoEntriesLayout;

  private static String ATTRIBUTE_ALTRENDER = "redaction";
  private static String ATTRIBUTE_ALTRENDER_REASON = "reason_for_redaction";
  private static String ATTRIBUTE_ALTRENDER_P_INPUT = "input";
  private static String ATTRIBUTE_ALTRENDER_P_OUTPUT = "output";
  private static String ATTRIBUTE_ALTRENDER_P_NOTE = "reason_for_output";

  /**
   * Listener for process info entries
   * 
   */
  public interface ProcessInfoEntryListener extends ChangeListener {
    /**
     * Called when an process info entry is removed
     * 
     * @param sender
     */
    public void onRemove(ProcessInfoEntryEditor sender);
  }

  public ProcessInfoEditor() {
    layout = new DockPanel();
    reason = new HorizontalPanel();
    reasonLabel = new Label(constants.processInfoReason());
    reasonBox = new TextArea();
    processInfoEntriesLayout = new VerticalPanel();

    reason.add(reasonLabel);
    reason.add(reasonBox);

    layout.add(reason, DockPanel.NORTH);
    layout.add(processInfoEntriesLayout, DockPanel.CENTER);

    processInfoEntries = new Vector<ProcessInfoEntryEditor>();
    listeners = new Vector<ChangeListener>();

    ChangeListener changeListener = new ChangeListener() {

      public void onChange(Widget sender) {
        ProcessInfoEditor.this.onChange(sender);
      }
    };

    createProcessInfoEntry(RedactionType.INPUT);
    createProcessInfoEntry(RedactionType.OUTPUT);

    updateLayout();

    reasonBox.addChangeListener(changeListener);

    layout.addStyleName("wui-editor-processinfo");
    processInfoEntriesLayout.addStyleName("wui-editor-processinfo-layout");
    reason.addStyleName("wui-editor-processinfo-reason");
    reason.setCellWidth(reasonBox, "100%");
    reasonLabel.addStyleName("wui-editor-processinfo-reason-label");
    reasonBox.addStyleName("wui-editor-processinfo-reason-box");
  }

  protected ProcessInfoEntryEditor createProcessInfoEntry(RedactionType type) {
    ProcessInfoEntryEditor editor = new ProcessInfoEntryEditor(type);
    editor.addProcessInfoEntryListener(new ProcessInfoEntryListener() {

      public void onRemove(ProcessInfoEntryEditor sender) {
        processInfoEntries.remove(sender);
        updateLayout();
        ProcessInfoEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        ProcessInfoEditor.this.onChange(sender);
      }
    });
    processInfoEntries.add(editor);
    return editor;
  }

  protected void updateLayout() {
    processInfoEntriesLayout.clear();
    for (ProcessInfoEntryEditor processInfoEntryEditor : processInfoEntries) {
      processInfoEntriesLayout.add(processInfoEntryEditor.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    ProcessInfo processInfo = null;
    List<P> plist = new Vector<P>();

    for (ProcessInfoEntryEditor processInfoEntryEditor : processInfoEntries) {
      P p = processInfoEntryEditor.getProcessInfoEntry();
      if (p != null) {
        plist.add(p);
      }
    }
    if (plist.size() != 0) {
      processInfo = new ProcessInfo();
      processInfo.setAttributeAltrender(ATTRIBUTE_ALTRENDER);
      if (plist.size() != 0) {
        processInfo.setpList(plist.toArray(new P[] {}));
      }
      if (!reasonBox.getText().isEmpty()) {
        P p = new P();
        p.setAttributeAltrender(ATTRIBUTE_ALTRENDER_REASON);
        p.setText(reasonBox.getText());
        processInfo.setNote(new Note(p));
      }
    }

    return processInfo;
  }

  public void setValue(EadCValue value) {
    if (value instanceof ProcessInfo) {
      ProcessInfo processInfo = (ProcessInfo) value;
      for (int i = 0; i < processInfo.getpList().length; i++) {
        P p = processInfo.getpList()[i];
        if (p.getAttributeAltrender().equals(ATTRIBUTE_ALTRENDER_P_INPUT)) {
          processInfoEntries.get(0).setProcessInfoEntry(p);
        } else {
          processInfoEntries.get(1).setProcessInfoEntry(p);
        }
      }
      Note note = processInfo.getNote();
      if (note != null && note.getP() != null && note.getP().getText() != null) {
        reasonBox.setText(note.getP().getText());
      }

      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (reasonBox.getText().isEmpty() && (processInfoEntries.size() == 0));
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (ProcessInfoEntryEditor processInfoEntryEditor : processInfoEntries) {
      P p = processInfoEntryEditor.getProcessInfoEntry();
      if (p.getArchrefs() == null) {
        valid = false;
      }
    }

    return valid;
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    VerticalPanel panel = new VerticalPanel();

    VerticalPanel inputPanel = new VerticalPanel();
    VerticalPanel outputPanel = new VerticalPanel();

    ProcessInfo processInfo = (ProcessInfo) value;
    for (int i = 0; i < processInfo.getpList().length; i++) {
      P p = processInfo.getpList()[i];
      if (p.getAttributeAltrender().equals(ATTRIBUTE_ALTRENDER_P_INPUT)) {
        Label inputLabel = new Label(constants.processInfoInput());
        inputPanel.add(inputLabel);
        inputPanel.add(ArchrefEditor.getReadonlyWidget(p.getArchrefs().getArchrefs()[0]));
        inputLabel.addStyleName("wui-editor-processinfo-readonly-input-label");
      } else {
        Notes notes = p.getNotes();

        Label outputLabel = new Label(constants.processInfoOutput());
        outputPanel.add(outputLabel);

        if (notes != null) {
          Note note = notes.getNotes()[0];
          if (note != null && note.getP() != null && !note.getP().getText().isEmpty()) {
            FlexTable sectionPanel = new FlexTable();
            addToReadonlyWidget(constants.processInfoReasonForOutput(), note.getP().getText(), sectionPanel, 0);
            outputPanel.add(sectionPanel);
            sectionPanel.addStyleName("wui-editor-processinfo-readonly-output-note");
          }
        }

        outputPanel.add(ArchrefEditor.getReadonlyWidget(p.getArchrefs().getArchrefs()[0]));
        outputLabel.addStyleName("wui-editor-processinfo-readonly-output-label");
      }
    }

    Note note = processInfo.getNote();
    if (note != null && note.getP() != null && note.getP().getText() != null) {
      FlexTable sectionPanel = new FlexTable();
      addToReadonlyWidget(constants.processInfoReason(), note.getP().getText(), sectionPanel, 0);
      panel.add(sectionPanel);
      sectionPanel.addStyleName("wui-editor-processinfo-readonly-note");
    }

    if (inputPanel.getWidgetCount() > 0) {
      panel.add(inputPanel);
    }

    if (outputPanel.getWidgetCount() > 0) {
      panel.add(outputPanel);
    }

    panel.addStyleName("wui-editor-processinfo-readonly");
    inputPanel.addStyleName("wui-editor-processinfo-readonly-input");
    outputPanel.addStyleName("wui-editor-processinfo-readonly-output");

    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-processinfo-readonly-column0");
      column1.addStyleName("wui-editor-processinfo-readonly-column1");
      row++;
    }
    return row;
  }

  /**
   * Editor for process info entry
   * 
   */
  public class ProcessInfoEntryEditor {

    private final List<ProcessInfoEntryListener> listeners;

    private final HorizontalPanel layout;

    private final VerticalPanel subLayout;
    private final HorizontalPanel redactionType;
    private final Label redactionTypeLabel;
    private final RedactionTypePicker redactionTypePicker;
    private final HorizontalPanel note;
    private final Label noteLabel;
    private final TextArea noteBox;
    private final ArchrefEditor archrefEditor;

    public ProcessInfoEntryEditor(RedactionType type) {
      layout = new HorizontalPanel();
      subLayout = new VerticalPanel();
      redactionType = new HorizontalPanel();
      redactionTypeLabel = new Label();
      redactionTypePicker = new RedactionTypePicker();
      redactionTypePicker.setVisible(false);
      note = new HorizontalPanel();
      noteLabel = new Label(constants.processInfoReasonForOutput());
      noteBox = new TextArea();
      archrefEditor = new ArchrefEditor();
      archrefEditor.setNoteVisible();

      redactionType.add(redactionTypeLabel);
      redactionType.add(redactionTypePicker);

      note.add(noteLabel);
      note.add(noteBox);

      subLayout.add(redactionType);
      subLayout.add(note);
      subLayout.add(archrefEditor.getWidget());

      layout.add(subLayout);

      if (type.equals(RedactionType.INPUT)) {
        redactionTypeLabel.setText(constants.processInfoInput());
        redactionTypePicker.setSelectedIndex(0);
        note.setVisible(false);
      } else {
        redactionTypeLabel.setText(constants.processInfoOutput());
        redactionTypePicker.setSelectedIndex(1);
      }

      listeners = new Vector<ProcessInfoEntryListener>();

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          ProcessInfoEntryEditor.this.onChange(sender);
        }

      };

      redactionTypePicker.addChangeListener(changeListener);
      archrefEditor.addChangeListener(changeListener);
      noteBox.addChangeListener(changeListener);

      layout.setCellWidth(subLayout, "100%");
      layout.addStyleName("wui-editor-processinfoentry");
      redactionType.addStyleName("wui-editor-processinfoentry-type");
      redactionType.setCellWidth(redactionTypePicker, "100%");
      redactionTypeLabel.addStyleName("wui-editor-processinfoentry-type-label");
      redactionTypePicker.addStyleName("wui-editor-processinfoentry-type-picker");
      note.addStyleName("wui-editor-processinfoentry-note");
      note.setCellWidth(noteBox, "100%");
      noteLabel.addStyleName("wui-editor-processinfoentry-note-label");
      noteBox.addStyleName("wui-editor-processinfoentry-note-box");
      subLayout.addStyleName("wui-editor-processinfoentry-center");
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
     * Set the process info entry value
     * 
     * @param processInforEntry
     */
    public void setProcessInfoEntry(P p) {

      Archrefs archrefs = p.getArchrefs();
      if (archrefs != null) {
        Archref archref = archrefs.getArchrefs()[0];
        if (archref != null) {
          archrefEditor.setValue(archref);
        }
      }
      Notes notes = p.getNotes();
      if (notes != null) {
        Note note = notes.getNotes()[0];
        if (note != null && note.getP() != null && !note.getP().getText().isEmpty()) {
          noteBox.setText(note.getP().getText());
        }
      }
    }

    /**
     * Get the process info entry value
     * 
     * @return
     */
    public P getProcessInfoEntry() {
      P p = new P();
      if (archrefEditor.isValid() && archrefEditor.getValue() != null) {
        List<Archref> archrefs = new ArrayList<Archref>();
        archrefs.add(archrefEditor.getValue());
        p.setArchrefs(new Archrefs(archrefs.toArray(new Archref[] {})));
      }
      if (redactionTypePicker.getSelectedIDType().equals(RedactionTypePicker.RedactionType.INPUT)) {
        p.setAttributeAltrender(ATTRIBUTE_ALTRENDER_P_INPUT);
      } else {
        p.setAttributeAltrender(ATTRIBUTE_ALTRENDER_P_OUTPUT);
      }

      if (!noteBox.getText().isEmpty()) {
        List<Note> notes = new ArrayList<Note>();
        Note note = new Note(new P(noteBox.getText()));
        note.setAttributeAltrender(ATTRIBUTE_ALTRENDER_P_NOTE);
        notes.add(note);
        p.setNotes(new Notes(notes.toArray(new Note[] {})));
      }
      return p;
    }

    /**
     * Add a process info entry listener
     * 
     * @param listener
     */
    public void addProcessInfoEntryListener(ProcessInfoEntryListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a process info entry listener
     * 
     * @param listener
     */
    public void removeProcessInfoEntryListener(ProcessInfoEntryListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (ProcessInfoEntryListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (ProcessInfoEntryListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
