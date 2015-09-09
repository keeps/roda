/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Index;
import pt.gov.dgarq.roda.core.data.eadc.Indexentry;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class KeywordsEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final VerticalPanel layout;

  private final WUIButton addKeyword;
  private final FlowPanel keywordsLayout;
  private final List<KeywordEditor> keywordEditors;
  private final List<ChangeListener> listeners;

  /**
   * Listener for keyword
   * 
   */
  public interface KeywordListener extends ChangeListener {
    /**
     * Called when an keyword is removed
     * 
     * @param sender
     */
    public void onRemove(KeywordEditor sender);
  }

  /**
   * Editor for notes
   */
  public KeywordsEditor() {
    layout = new VerticalPanel();
    addKeyword = new WUIButton(constants.keywordsAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    keywordsLayout = new FlowPanel();

    layout.add(keywordsLayout);
    layout.add(addKeyword);

    keywordEditors = new Vector<KeywordEditor>();
    listeners = new Vector<ChangeListener>();

    addKeyword.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createKeywordEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.addStyleName("wui-editor-keywords");
    addKeyword.addStyleName("wui-editor-keywords-add");
    keywordsLayout.addStyleName("wui-editor-keywords-layout");
  }

  protected KeywordEditor createKeywordEditor() {
    KeywordEditor editor = new KeywordEditor();
    editor.addKeywordListener(new KeywordListener() {
      public void onRemove(KeywordEditor sender) {
        keywordEditors.remove(sender);
        updateLayout();
        KeywordsEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        KeywordsEditor.this.onChange(sender);
      }
    });
    keywordEditors.add(editor);
    return editor;
  }

  protected void updateLayout() {
    keywordsLayout.clear();
    for (KeywordEditor keywordEditor : keywordEditors) {
      keywordsLayout.add(keywordEditor.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<Indexentry> keywords = new Vector<Indexentry>();
    for (KeywordEditor keywordEditor : keywordEditors) {
      String keyword = keywordEditor.getKeyword();
      if (keyword != null) {
        keywords.add(new Indexentry(keyword));
      }
    }
    return keywords.size() == 0 ? null : new Index(keywords.toArray(new Indexentry[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof Index) {
      Index index = (Index) value;
      keywordEditors.clear();
      for (int i = 0; i < index.getIndexes().length; i++) {
        Indexentry indexentry = index.getIndexes()[i];
        if (indexentry.getSubject() != null) {
          KeywordEditor keywordEditor = createKeywordEditor();
          keywordEditor.setKeyword(indexentry.getSubject());
        }
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return keywordEditors.size() == 0;
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
    Index index = (Index) value;
    for (int i = 0; i < index.getIndexes().length; i++) {
      Indexentry indexentry = index.getIndexes()[i];
      if (indexentry.getSubject() != null) {
        Label column0 = new Label(indexentry.getSubject());
        panel.setWidget(i, 0, column0);
        panel.getColumnFormatter().setWidth(0, "100%");
        column0.addStyleName("wui-editor-keywords-readonly-column0");
      }
    }

    panel.addStyleName("wui-editor-keywords-readonly");
    return panel;
  }

  /**
   * Editor for keyword
   * 
   */
  public class KeywordEditor {

    private final List<KeywordListener> listeners;

    private final HorizontalPanel layout;

    private final TextBox subLayout;
    private final Image remove;

    public KeywordEditor() {
      layout = new HorizontalPanel();
      subLayout = new TextBox();
      remove = commonImageBundle.minus().createImage();

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<KeywordListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          KeywordEditor.this.onChange(sender);
        }

      };

      subLayout.addChangeListener(changeListener);

      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-keyword");
      subLayout.addStyleName("wui-editor-keyword-text");
      remove.addStyleName("wui-editor-keyword-remove");
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
     * Set the keyword value
     * 
     * @param keyword
     */
    public void setKeyword(String keyword) {
      if (keyword != null && !keyword.isEmpty()) {
        subLayout.setText(keyword);
      }
    }

    /**
     * Get the keyword value
     * 
     * @return
     */
    public String getKeyword() {
      return (subLayout.getText().isEmpty()) ? null : subLayout.getText();
    }

    /**
     * Add a keyword listener
     * 
     * @param listener
     */
    public void addKeywordListener(KeywordListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a keyword listener
     * 
     * @param listener
     */
    public void removeKeywordListener(KeywordListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (KeywordListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (KeywordListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
