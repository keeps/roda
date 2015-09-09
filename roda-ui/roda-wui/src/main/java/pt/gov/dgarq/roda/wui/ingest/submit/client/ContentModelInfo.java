/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.submit.client;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

import pt.gov.dgarq.roda.wui.common.fileupload.client.FileNameConstraints;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

import config.i18n.client.IngestSubmitConstants;

/**
 * @author Luis Faria
 * 
 */
public class ContentModelInfo {

  private static IngestSubmitConstants constants = (IngestSubmitConstants) GWT.create(IngestSubmitConstants.class);

  private String contentModel;
  private FileNameConstraints filenameConstraints;
  private List<Image> icons;

  /**
   * Create a new content model info
   * 
   * @param contentModel
   *          the content model
   */
  public ContentModelInfo(String contentModel) {
    this.contentModel = contentModel;
    filenameConstraints = FileNameConstraints.DEFAULT_FILENAME_CONSTRAINT;
    icons = new Vector<Image>();
  }

  /**
   * Create a new content model info
   * 
   * @param id
   *          the content model id
   * @param filenameConstraints
   *          filename constraints for representations of this content model
   * @param icons
   *          the icons of the allowed file types
   */
  public ContentModelInfo(String id, FileNameConstraints filenameConstraints, List<Image> icons) {
    this.contentModel = id;
    this.filenameConstraints = filenameConstraints;
    this.icons = icons;
  }

  /**
   * Get the content model id
   * 
   * @return
   */
  public String getContentModel() {
    return contentModel;
  }

  /**
   * Set content model id
   * 
   * @param contentModel
   */
  public void setContentModel(String contentModel) {
    this.contentModel = contentModel;
  }

  /**
   * Get filename constraints
   * 
   * @return
   */
  public FileNameConstraints getFilenameConstraints() {
    return filenameConstraints;
  }

  /**
   * Set filename constraints
   * 
   * @param filenameConstraints
   */
  public void setFilenameConstraints(FileNameConstraints filenameConstraints) {
    this.filenameConstraints = filenameConstraints;
  }

  /**
   * Get icons of allowed file types
   * 
   * @return
   */
  public List<Image> getIcons() {
    return icons;
  }

  /**
   * Set icons of allowed file types
   * 
   * @param icons
   */
  public void setIcons(List<Image> icons) {
    this.icons = icons;
  }

  /**
   * Get title from constants (cModel_id_title). If no title defined, then the
   * id is returned.
   * 
   * @return
   */
  public String getTitle() {
    String title;
    try {
      title = constants.getString("cModel_" + contentModel + "_title");
    } catch (MissingResourceException e) {
      title = contentModel;
    }
    return title;
  }

  /**
   * Get description from constants (cModel_id_description). If no description
   * defined, then the null is returned.
   * 
   * @return
   */
  public String getDescription() {
    String description;
    try {
      description = constants.getString("cModel_" + contentModel + "_description");
    } catch (MissingResourceException e) {
      description = null;
    }
    return description;
  }

}
