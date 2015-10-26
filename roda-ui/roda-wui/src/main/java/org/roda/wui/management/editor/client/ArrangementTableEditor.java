/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.ArrangementTable;
import org.roda.core.data.eadc.ArrangementTableBody;
import org.roda.core.data.eadc.ArrangementTableGroup;
import org.roda.core.data.eadc.ArrangementTableHead;
import org.roda.core.data.eadc.ArrangementTableRow;
import org.roda.core.data.eadc.EadCValue;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class ArrangementTableEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DockPanel layout;

  private final HorizontalPanel toolbar;

  private final WUIButton addRow;

  private final WUIButton addColumn;

  private final FlexTable tableLayout;

  private int columns;

  private final List<ArrangementTableCellEditor> header;

  private final List<List<ArrangementTableCellEditor>> rows;

  private final List<ChangeListener> listeners;

  public class ArrangementTableCellEditor {

    private final TextBoxBase editor;

    private final List<ChangeListener> listeners;

    public ArrangementTableCellEditor(boolean header) {
      if (header) {
        editor = new TextBox();
        editor.addStyleName("table-header-editor");
      } else {
        editor = new TextArea();
        editor.addStyleName("table-body-editor");
      }
      listeners = new Vector<ChangeListener>();
      editor.addKeyboardListener(new KeyboardListener() {

        public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyUp(Widget sender, char keyCode, int modifiers) {
          ArrangementTableCellEditor.this.onChange(editor);
        }

      });
    }

    public String getValue() {
      return editor.getText();
    }

    public void setValue(String value) {
      editor.setText(value);
    }

    public void addChangeListener(ChangeListener listener) {
      listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (ChangeListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    public Widget getWidget() {
      return editor;
    }

  }

  public ArrangementTableEditor() {
    layout = new DockPanel();
    toolbar = new HorizontalPanel();
    addRow = new WUIButton(constants.addRow(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    addColumn = new WUIButton(constants.addColumn(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    columns = 0;
    header = new Vector<ArrangementTableCellEditor>();
    rows = new Vector<List<ArrangementTableCellEditor>>();

    tableLayout = new FlexTable();

    listeners = new Vector<ChangeListener>();

    toolbar.add(addRow);
    toolbar.add(addColumn);

    layout.add(toolbar, DockPanel.NORTH);
    layout.add(tableLayout, DockPanel.CENTER);

    addRow.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        addEmptyRow();
        updateLayout();
        onChange(tableLayout);
      }

    });

    addColumn.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        addEmptyColumn();
        updateLayout();
        onChange(tableLayout);
      }

    });

    // Table basic skeleton
    columns = 1;
    header.add(createCellEditor(true, null));

    updateLayout();

    layout.addStyleName("wui-editor-table");
    toolbar.addStyleName("table-toolbar");
    addRow.addStyleName("table-add-row");
    addColumn.addStyleName("table-add-column");
    tableLayout.addStyleName("table-layout");

  }

  private void addEmptyRow() {
    List<ArrangementTableCellEditor> row = new Vector<ArrangementTableCellEditor>();
    for (int i = 0; i < columns; i++) {
      row.add(createCellEditor(false, null));
    }
    rows.add(row);

  }

  private void addEmptyColumn() {
    header.add(createCellEditor(true, null));
    for (List<ArrangementTableCellEditor> row : rows) {
      row.add(createCellEditor(false, null));
    }
    columns++;
    onChange(tableLayout);
  }

  private void removeRow(int rowIndex) {
    rows.remove(rowIndex);
    onChange(tableLayout);
  }

  private void removeColumn(int columnIndex) {
    header.remove(columnIndex);
    for (List<ArrangementTableCellEditor> row : rows) {
      row.remove(columnIndex);
    }
    columns--;
    onChange(tableLayout);
  }

  private void updateLayout() {
    tableLayout.clear();
    for (int i = 0; i < columns; i++) {
      tableLayout.setWidget(0, i, ((ArrangementTableCellEditor) header.get(i)).getWidget());
    }

    tableLayout.getRowFormatter().addStyleName(0, "table-header-row");

    for (int i = 0; i < rows.size(); i++) {
      final int index = i;
      List<ArrangementTableCellEditor> row = rows.get(i);
      for (int j = 0; j < columns; j++) {
        tableLayout.setWidget(i + 1, j, ((ArrangementTableCellEditor) row.get(j)).getWidget());
      }
      tableLayout.getRowFormatter().addStyleName(i + 1, "table-body-row");

      // row remove button
      Image rowRemove = commonImageBundle.minus().createImage();
      rowRemove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          removeRow(index);
          updateLayout();
        }

      });
      tableLayout.setWidget(i + 1, columns, rowRemove);
      rowRemove.addStyleName("table-row-remove");
    }

    // add column remove buttons
    if (columns > 1) {
      for (int i = 0; i < columns; i++) {
        final int index = i;
        Image columnRemove = commonImageBundle.minus().createImage();
        columnRemove.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            if (Window.confirm(constants.removeColumnConfirmation())) {
              removeColumn(index);
              updateLayout();
            }
          }

        });
        tableLayout.setWidget(rows.size() + 1, i, columnRemove);
        tableLayout.getCellFormatter().setHorizontalAlignment(rows.size() + 1, i, HasAlignment.ALIGN_CENTER);
        columnRemove.addStyleName("table-column-remove");

      }
    }

  }

  private ArrangementTableCellEditor createCellEditor(boolean header, String value) {
    ArrangementTableCellEditor cellEditor = new ArrangementTableCellEditor(header);
    if (value != null) {
      cellEditor.setValue(value);
    }

    cellEditor.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        ArrangementTableEditor.this.onChange(sender);
      }

    });

    return cellEditor;
  }

  public void setValue(EadCValue value) {
    if (value instanceof ArrangementTable) {
      ArrangementTable table = (ArrangementTable) value;

      header.clear();
      rows.clear();
      if (table.getArrangementTableGroups().length == 0) {
        // Table basic skeleton
        columns = 1;
        header.add(createCellEditor(true, null));

      } else {
        ArrangementTableGroup group = table.getArrangementTableGroups()[0];
        columns = (new Integer(group.getColumns())).intValue();
        if (group.getHead().getRows().length == 0) {
          for (int i = 0; i < columns; i++) {
            header.add(createCellEditor(true, null));
          }
        } else {
          ArrangementTableRow row = group.getHead().getRows()[0];
          for (int i = 0; i < row.getEntries().length; i++) {
            header.add(createCellEditor(true, row.getEntries()[i]));
          }
        }

        if (group.getHead().getRows().length > 1) {
          logger.warn("Arrangement table header as more than one row," + " ignoring all but the first one");
        }

        for (int i = 0; i < group.getBody().getRows().length; i++) {
          ArrangementTableRow row = group.getBody().getRows()[i];
          List<ArrangementTableCellEditor> rowValues = new Vector<ArrangementTableCellEditor>();
          for (int j = 0; j < row.getEntries().length; j++) {
            rowValues.add(createCellEditor(false, row.getEntries()[j]));
          }
          rows.add(rowValues);
        }

      }

      if (table.getArrangementTableGroups().length > 1) {
        logger.warn("Arrangement table as more than one group," + " ignoring all but the first one");
      }

      updateLayout();
    }
  }

  public EadCValue getValue() {
    ArrangementTable ret;
    if (rows.size() == 0) {
      ret = null;
    } else {
      // head
      ArrangementTableRow headRow = list2row(header);
      ArrangementTableHead head = new ArrangementTableHead(new ArrangementTableRow[] {headRow});

      // body
      ArrangementTableRow[] bodyRows = new ArrangementTableRow[rows.size()];
      for (int i = 0; i < rows.size(); i++) {
        bodyRows[i] = list2row(rows.get(i));
      }
      ArrangementTableBody body = new ArrangementTableBody(bodyRows);

      ArrangementTableGroup group = new ArrangementTableGroup(columns, head, body);

      ret = new ArrangementTable(new ArrangementTableGroup[] {group});

    }
    return ret;
  }

  private ArrangementTableRow list2row(List<ArrangementTableCellEditor> line) {
    String[] entries = new String[line.size()];
    for (int i = 0; i < line.size(); i++) {
      entries[i] = ((ArrangementTableCellEditor) line.get(i)).getValue();
    }
    return new ArrangementTableRow(entries);
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return rows.size() == 0;
  }

  public boolean isValid() {
    return true;
  }
}
