/**
 * 
 */
package org.roda.wui.common.fileupload.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;
import org.roda.wui.common.fileupload.client.FileNameConstraints.FileNameConstraint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.FileUploadConstants;

/**
 * @author Luis Faria
 * 
 */
public class FileUploadPanel implements SourcesChangeEvents {

  /**
   * The name of the form used to upload
   */
  public static final String FILE_UPLOAD_NAME = "file_to_upload";

  private static final int ADD_TIMER_DELAY_MS = 500;

  /**
   * Return string in case of success
   */
  public static final String UPLOAD_SUCCESS = "OK";

  /**
   * Return string in case of an error
   */
  public static final String UPLOAD_FAILURE = "ERROR";

  private static final String FILE_UPLOAD_SERVER = GWT.getModuleBaseURL() + "fileUpload";

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static FileUploadConstants constants = (FileUploadConstants) GWT.create(FileUploadConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  /**
   * Dummy file upload to allow users to add a file
   * 
   */
  public class FileUploadDummy {

    private final FileUpload fileUpload;

    private final HorizontalPanel layout;

    private final Label fileNamePath;

    private final Image remove;

    /**
     * Create a new dummy file upload
     * 
     * @param filename
     * @param fileUpload
     */
    public FileUploadDummy(String filename, FileUpload fileUpload) {
      this.fileUpload = fileUpload;
      fileUpload.setVisible(false);

      layout = new HorizontalPanel();
      fileNamePath = new Label(filename);
      remove = commonImageBundle.minus().createImage();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          formLayout.remove(FileUploadDummy.this.fileUpload);
          dummiesLayout.remove(FileUploadDummy.this.getWidget());
          files.remove(FileUploadDummy.this);
          updateVisibles();
          onChange(sender);
        }

      });

      layout.add(fileNamePath);
      layout.add(remove);

      layout.addStyleName("wui-fileUpload-file");
      fileNamePath.addStyleName("file-path");
      remove.addStyleName("file-status-remove");
    }

    /**
     * Get dummy file upload widget
     * 
     * @return
     */
    public Widget getWidget() {
      return layout;
    }

    /**
     * Is dummy file upload widget enabled
     * 
     * @return
     */
    public boolean isEnabled() {
      return remove.isVisible();
    }

    /**
     * Set dummy file upload widget enabled
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
      remove.setVisible(enabled);
    }

    /**
     * Get file upload
     * 
     * @return
     */
    public FileUpload getFileUpload() {
      return fileUpload;
    }
  }

  private final VerticalPanel layout;

  private final FormPanel form;

  private final HorizontalPanel formLayout;

  private final FormPanel dummyFormPanel;

  private final HorizontalPanel addFileLayout;

  private FileUpload fileUpload;

  private final Timer addTimer;

  private final VerticalPanel dummiesLayout;

  private final List<FileUploadDummy> files;

  private boolean submitting;

  private FileNameConstraints constraints;

  private final List<ChangeListener> listeners;

  /**
   * Create a new file upload panel
   */
  public FileUploadPanel() {
    this(FileNameConstraints.DEFAULT_FILENAME_CONSTRAINT);
  }

  /**
   * Create a file upload panel with file number or extension restrictions
   * 
   * @param fileNumberLimit
   *          the maximum number of files permitted, -1 to disable
   * @param filenameConstraints
   *          the allowed filename extensions of chosen files, all extensions
   *          should be lower case. Insert null to disable
   */
  public FileUploadPanel(FileNameConstraints filenameConstraints) {

    layout = new VerticalPanel();
    files = new Vector<FileUploadDummy>();

    submitting = false;

    form = new FormPanel();
    form.setAction(FILE_UPLOAD_SERVER);
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    formLayout = new HorizontalPanel();
    form.setWidget(formLayout);
    form.setVisible(false);

    layout.add(form);

    dummyFormPanel = new FormPanel();
    addFileLayout = new HorizontalPanel();
    dummyFormPanel.setWidget(addFileLayout);

    fileUpload = new FileUpload();
    fileUpload.setName(FILE_UPLOAD_NAME);

    addTimer = new Timer() {

      @Override
      public void run() {
        if (fileUpload.getFilename().length() == 0) {
          // reschedule and return
          addTimer.schedule(ADD_TIMER_DELAY_MS);
          return;
        } else if (canFilenameBeAdded(fileUpload.getFilename())) {
          FileUploadDummy file = new FileUploadDummy(fileUpload.getFilename(), fileUpload);
          formLayout.add(fileUpload);
          dummiesLayout.add(file.getWidget());
          files.add(file);
        } else {
          Window.alert(constants.fileUploadInvalidFilename());
        }

        addFileLayout.remove(fileUpload);
        fileUpload = new FileUpload();
        fileUpload.setName(FILE_UPLOAD_NAME);
        addFileLayout.insert(fileUpload, 0);

        updateVisibles();
        onChange(null);

        addTimer.schedule(ADD_TIMER_DELAY_MS);

      }

    };

    addFileLayout.add(fileUpload);

    dummiesLayout = new VerticalPanel();
    layout.add(dummyFormPanel);
    layout.add(dummiesLayout);

    listeners = new Vector<ChangeListener>();

    form.addFormHandler(new FormHandler() {

      public void onSubmit(FormSubmitEvent event) {
        if (formLayout.getWidgetCount() == 0) {
          Window.alert(constants.fileUploadNoFilesWarning());
          event.setCancelled(true);
        } else {
          submitting = true;
          updateVisibles();
        }

      }

      public void onSubmitComplete(FormSubmitCompleteEvent event) {
        clear();
        submitting = false;
        updateVisibles();

      }

    });

    setConstraints(filenameConstraints);

    addTimer.schedule(ADD_TIMER_DELAY_MS);

    layout.addStyleName("wui-fileUpload");
    formLayout.addStyleName("fileUpload-form");
    fileUpload.addStyleName("fileUpload-form-browse");

  }

  /**
   * Get file upload panel widget
   * 
   * @return
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Upload all files to server
   * 
   * @param callback
   *          Handle the file codes of the uploaded files or the upload
   *          exception FileUploadException
   */
  public void submit(final AsyncCallback<String[]> callback) {

    // Create wait window
    final WUIWindow waitWindow = new WUIWindow(constants.waitWindowTitle(), 300, 50);

    HorizontalPanel waitWindowLayout = new HorizontalPanel();
    Image loadingImage = new Image(GWT.getModuleBaseURL() + "images/loading_999.gif");
    final Label loadingLabel = new Label(constants.waitWindowLabel());
    waitWindowLayout.add(loadingImage);
    waitWindowLayout.add(loadingLabel);
    waitWindow.setWidget(waitWindowLayout);
    WUIButton cancel = new WUIButton(constants.waitWindowCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    waitWindow.addToBottom(cancel);

    final Timer progressCheck = new Timer() {

      @Override
      public void run() {
        FileUploadProgress.Util.getInstance().getProgress(new AsyncCallback<Double>() {

          public void onFailure(Throwable caught) {
            // nothing to do

          }

          public void onSuccess(Double progress) {
            if (progress > 0) {
              loadingLabel.setText(constants.fileUploadProgress() + " "
                + NumberFormat.getPercentFormat().format(progress));
            }

            if (progress < 1) {
              schedule(1000);
            }
          }

        });
      }

    };
    progressCheck.schedule(1000);

    waitWindowLayout.addStyleName("waitWindow-layout");
    loadingImage.addStyleName("waitWindow-image");
    loadingLabel.addStyleName("waitWindow-label");

    // Form handler
    final FormHandler formHandler = new FormHandler() {

      public void onSubmit(FormSubmitEvent event) {
        // nothing to do
        waitWindow.show();
      }

      public void onSubmitComplete(FormSubmitCompleteEvent event) {
        waitWindow.hide();
        form.removeFormHandler(this);
        String results = event.getResults();
        logger.info("form: " + results);
        if (results.matches("\\<pre.*\\</pre\\>") || results.matches("\\<PRE.*\\</PRE\\>")) {
          results = results.substring(results.indexOf('>') + 1, results.length() - 6);
        }
        if (results.startsWith(UPLOAD_SUCCESS)) {
          String[] fileCodes = results.substring(UPLOAD_SUCCESS.length() + 1).split(" ");
          callback.onSuccess(fileCodes);
        } else {
          callback.onFailure(new FileUploadException(event.getResults()));
        }
      }
    };
    form.addFormHandler(formHandler);

    // Cancel upload action
    cancel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        // To cancel, remove from parent and add it again
        form.removeFromParent();
        layout.add(form);

        waitWindow.hide();

        form.removeFormHandler(formHandler);

        progressCheck.cancel();

        callback.onFailure(null);
      }

    });

    // Submit form
    form.submit();
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);

  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  private void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  /**
   * Clear file upload panel
   */
  public void clear() {
    formLayout.clear();
    dummiesLayout.clear();
    files.clear();
    onChange(getWidget());
  }

  /**
   * Is file upload panel empty
   * 
   * @return
   */
  public boolean isEmpty() {
    return formLayout.getWidgetCount() == 0;
  }

  private boolean isFilenameValid(String filename, Set<String> extensions) {
    boolean valid;
    int index = filename.lastIndexOf('.');
    if (filename.length() == 0) {
      valid = false;
    } else if (extensions == null) {
      valid = true;
    } else if (index != -1 && index < filename.length() - 1) {
      String extension = filename.substring(index + 1);
      valid = extensions.contains(extension.toLowerCase());
    } else {
      valid = false;
    }
    return valid;
  }

  /**
   * Get filenames of all added files
   * 
   * @return
   */
  private Set<String> getFilenames() {
    Set<String> ret = new HashSet<String>();
    for (FileUploadDummy fileUploadDummy : files) {
      ret.add(fileUploadDummy.getFileUpload().getFilename());
    }
    return ret;
  }

  /**
   * Update conditions or return null if filename does not apply to conditions
   * 
   * @param conditions
   * @param filename
   * @return
   */
  private FileNameConstraints updateConstraints(FileNameConstraints constraints, String filename) {
    boolean foundExtension = false;
    for (FileNameConstraint constraint : constraints.getConstraints()) {
      Integer count = constraint.getCount();
      if (count != 0 && isFilenameValid(filename, constraint.getExtensions())) {
        foundExtension = true;
        if (count > 0) {
          constraint.setCount(constraint.getCount() - 1);
        }
      }
    }
    return (foundExtension ? constraints : null);
  }

  /**
   * Can a filename be added using current restrictions
   * 
   * @param filenameToAdd
   *          filename to be added
   * @return
   */
  public boolean canFilenameBeAdded(String filenameToAdd) {
    Set<String> filenames = getFilenames();
    FileNameConstraints newConstraints = new FileNameConstraints(constraints);
    // update conditions with already added filenames
    for (String filename : filenames) {
      if (newConstraints != null) {
        newConstraints = updateConstraints(newConstraints, filename);
      }
    }

    if (newConstraints != null) {
      // Check filename to be added
      newConstraints = updateConstraints(newConstraints, filenameToAdd);
    }

    logger.debug("With constraints: " + constraints + " file " + filenameToAdd + " can be added? "
      + (newConstraints != null));

    return newConstraints != null;
  }

  /**
   * Check if more files can be added
   * 
   * @return
   */
  public boolean canMoreFilesBeAdded() {
    boolean ret = false;
    Set<String> filenames = getFilenames();
    FileNameConstraints newConstraints = new FileNameConstraints(constraints);
    // update conditions with already added filenames
    for (String filename : filenames) {
      if (newConstraints != null) {
        newConstraints = updateConstraints(newConstraints, filename);
      }
    }

    if (newConstraints != null) {
      for (FileNameConstraint constraint : newConstraints.getConstraints()) {
        if (constraint.getCount() != 0) {
          ret = true;
        }
      }
    }

    logger.debug("With files: " + filenames + " and constraints: " + constraints + " can more files be added? " + ret);

    return ret;
  }

  /**
   * Check if file upload respects all filename restrictions // addButton = new
   * Button(constants.fileUploadAddButton(), // new ClickListener() { // //
   * public void onClick(Widget sender) { // if
   * (fileUpload.getFilename().length() == 0) { // Window.alert(constants //
   * .fileUploadNoFileChosenWarning()); // } else if
   * (canFilenameBeAdded(fileUpload.getFilename())) { // FileUploadDummy file =
   * new FileUploadDummy( // fileUpload.getFilename(), fileUpload); //
   * formLayout.add(fileUpload); // dummiesLayout.add(file.getWidget()); //
   * files.add(file); // } else { //
   * Window.alert(constants.fileUploadInvalidFilename()); // } // //
   * addFileLayout.remove(fileUpload); // fileUpload = new FileUpload(); //
   * fileUpload.setName(FILE_UPLOAD_NAME); // addFileLayout.insert(fileUpload,
   * 0); // // updateVisibles(); // onChange(sender); // } // // });
   * 
   * @return
   */
  public boolean isValid() {
    boolean valid = true;
    Set<String> filenames = getFilenames();
    FileNameConstraints newConstraints = new FileNameConstraints(constraints);
    // update conditions with already added filenames
    for (String filename : filenames) {
      if (newConstraints != null) {
        newConstraints = updateConstraints(newConstraints, filename);
      }
    }

    if (newConstraints != null) {
      // check if no obligatory file is needed
      for (FileNameConstraint constraint : newConstraints.getConstraints()) {
        if (constraint.getCount() > 0) {
          valid = false;
        }
      }
    } else {
      valid = false;
    }

    return valid;
  }

  private void updateVisibles() {

  }

  /**
   * Set filename constraints
   * 
   * @param constraints
   */
  public void setConstraints(FileNameConstraints constraints) {
    this.constraints = constraints;
    updateVisibles();
  }

}
