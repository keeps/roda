/**
 *
 */
package pt.gov.dgarq.roda.wui.ingest.submit.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import pt.gov.dgarq.roda.wui.common.fileupload.client.FileNameConstraints;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class ContentModelSelector implements SourcesChangeEvents {

  private static List<ContentModelInfo> contentModels = new Vector<ContentModelInfo>();

  static {
    for (String type : SimpleRepresentationObject.TYPES) {
      ContentModelInfo cModelInfo = new ContentModelInfo(type);
      FileNameConstraints fileConstraints = new FileNameConstraints();
      if (type.equals(SimpleRepresentationObject.EMAIL)) {
        fileConstraints.addConstraint(new String[] {"eml", "msg"}, 1);
      } else if (type.equals(SimpleRepresentationObject.STRUCTURED_TEXT)) {
        fileConstraints.addConstraint(new String[] {"pdf", "doc", "docx", "odt", "rtf", "txt"}, 1);
      } else if (type.equals(SimpleRepresentationObject.PRESENTATION)) {
        fileConstraints.addConstraint(new String[] {"ppt", "pptx", "odp"}, 1);
      } else if (type.equals(SimpleRepresentationObject.SPREADSHEET)) {
        fileConstraints.addConstraint(new String[] {"xls", "xlsx", "ods"}, 1);
      } else if (type.equals(SimpleRepresentationObject.VECTOR_GRAPHIC)) {
        fileConstraints.addConstraint(new String[] {"cdr", "ai", "shp", "dwg"}, 1);
      } else if (type.equals(SimpleRepresentationObject.DIGITALIZED_WORK)) {
        fileConstraints.addConstraint(new String[] {"jpg", "jpeg", "png", "tif", "tiff", "bmp", "gif", "ico", "xpm",
          "tga", "jp2"}, -1);
      } else if (type.equals(SimpleRepresentationObject.RELATIONAL_DATABASE)) {
        fileConstraints.addConstraint(new String[] {"xml"}, 1);
        fileConstraints.addConstraint(null, -1);
      } else if (type.equals(SimpleRepresentationObject.VIDEO)) {
        fileConstraints.addConstraint(new String[] {"mpeg", "mpg", "m2p", "m2v", "mpv2", "mp2v", "vob", "avi", "mov",
          "qt", "mp4", "wmv"}, 1);
      } else if (type.equals(SimpleRepresentationObject.AUDIO)) {
        fileConstraints.addConstraint(new String[] {"wav", "mp3", "mp4", "ogg", "flac", "wma"}, 1);
      } else {
        fileConstraints = FileNameConstraints.DEFAULT_FILENAME_CONSTRAINT;
      }
      cModelInfo.setFilenameConstraints(fileConstraints);
      contentModels.add(cModelInfo);

    }

  }
  private VerticalPanel layout;
  private List<ContentModelItem> items;
  private List<ChangeListener> listeners;

  /**
   * Create a new content model selector
   */
  public ContentModelSelector() {
    layout = new VerticalPanel();
    listeners = new Vector<ChangeListener>();
    items = new Vector<ContentModelItem>();
    for (ContentModelInfo cModelInfo : contentModels) {
      ContentModelItem cModelItem = new ContentModelItem(cModelInfo);
      cModelItem.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          ContentModelSelector.this.onChange();
        }
      });
      items.add(cModelItem);
    }
    items.get(0).setChecked(true);
    updateLayout();
    layout.addStyleName("wui-cModelSelector");
  }

  private void updateLayout() {
    layout.clear();
    for (ContentModelItem item : items) {
      layout.add(item.getWidget());
    }
  }

  /**
   * Get selected content model info
   *
   * @return
   */
  public ContentModelInfo getSelected() {
    ContentModelInfo ret = null;
    for (ContentModelItem item : items) {
      if (item.isChecked()) {
        ret = item.getCModelInfo();
      }
    }

    return ret;
  }

  /**
   * Content model item selector
   */
  public class ContentModelItem implements SourcesChangeEvents {

    private ContentModelInfo cModelInfo;
    private AccessibleFocusPanel focus;
    private DockPanel layout;
    private RadioButton radioButton;
    private Label title;
    private Label description;
    private HorizontalPanel iconsLayout;
    private List<ChangeListener> listeners;

    /**
     * Create a new content model item selector
     *
     * @param cModelInfo
     *          the content model info
     */
    public ContentModelItem(ContentModelInfo cModelInfo) {
      this.cModelInfo = cModelInfo;

      focus = new AccessibleFocusPanel();
      layout = new DockPanel();
      radioButton = new RadioButton("contentModelSelector");
      title = new Label(cModelInfo.getTitle());
      description = new Label(cModelInfo.getDescription());
      iconsLayout = new HorizontalPanel();
      listeners = new Vector<ChangeListener>();

      for (Image icon : cModelInfo.getIcons()) {
        iconsLayout.add(icon);
        icon.addStyleName("icon");
      }

      focus.setWidget(layout);
      layout.add(radioButton, DockPanel.WEST);
      layout.add(title, DockPanel.NORTH);
      layout.add(description, DockPanel.CENTER);
      layout.add(iconsLayout, DockPanel.SOUTH);

      focus.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          if (!radioButton.isChecked()) {
            radioButton.setChecked(true);
            onChange();
          }
        }
      });

      radioButton.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          onChange();
        }
      });

      layout.setCellVerticalAlignment(radioButton, HasAlignment.ALIGN_MIDDLE);

      focus.addStyleName("contentModel");
      layout.addStyleName("contentModel-layout");
      radioButton.addStyleName("contentModel-radio");
      title.addStyleName("contentModel-title");
      description.addStyleName("contentModel-description");
      iconsLayout.addStyleName("contentModel-icons");

    }

    /**
     * Is this content model selected
     *
     * @return
     */
    public boolean isChecked() {
      return radioButton.isChecked();
    }

    /**
     * Set this content model selected
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
      radioButton.setChecked(checked);
    }

    /**
     * Get widget
     *
     * @return
     */
    public Widget getWidget() {
      return focus;
    }

    /**
     * Get associated content model info
     *
     * @return
     */
    public ContentModelInfo getCModelInfo() {
      return cModelInfo;
    }

    public void addChangeListener(ChangeListener listener) {
      listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
      listeners.remove(listener);
    }

    protected void onChange() {
      for (ChangeListener listener : listeners) {
        listener.onChange(getWidget());
      }
    }
  }

  /**
   * Get widget
   *
   * @return
   */
  public Widget getWidget() {
    return layout;
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  protected void onChange() {
    for (ChangeListener listener : listeners) {
      listener.onChange(getWidget());
    }
  }
}
