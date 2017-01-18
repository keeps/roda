package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.FileActions.FileAction;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class OptionsSliderHelper {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private OptionsSliderHelper() {

  }

  static <T extends IsIndexed> void updateOptionsObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedFile) {
      updateOptionsSliderPanel((IndexedFile) object, slider);
    } else if (object instanceof IndexedRepresentation) {
      updateOptionsSliderPanel((IndexedRepresentation) object, slider);
    } else if (object instanceof IndexedAIP) {
      updateOptionsSliderPanel((IndexedAIP) object, slider);
    } else {
      // do nothing
    }
  }

  private static void updateOptionsSliderPanel(IndexedAIP aip, SliderPanel slider) {
    // TODO Auto-generated method stub

  }

  private static void updateOptionsSliderPanel(IndexedRepresentation representation, SliderPanel slider) {
    // TODO Auto-generated method stub

  }

  private static void updateOptionsSliderPanel(final IndexedFile file, final SliderPanel slider) {
    slider.clear();

    // MANAGEMENT
    slider.addTitle(new Label(messages.sidebarFoldersFilesTitle()));

    // DOWNLOAD, RENAME, MOVE, REMOVE, UPLOAD_FILES, CREATE_FOLDER

    // Download
    if (FileActions.canAct(FileAction.DOWNLOAD, file)) {
      slider.addContent(FileActions.createButton(messages.downloadButton(), FileAction.DOWNLOAD, file,
        "btn-default-alt", "btn-download"));
    }

    // Rename
    if (FileActions.canAct(FileAction.RENAME, file)) {
      slider.addContent(
        FileActions.createButton(messages.renameButton(), FileAction.RENAME, file, "btn-default-alt", "btn-edit"));
    }

    // Move
    if (FileActions.canAct(FileAction.MOVE, file)) {
      slider.addContent(
        FileActions.createButton(messages.moveButton(), FileAction.MOVE, file, "btn-default-alt", "btn-edit"));
    }

    // Upload files
    if (FileActions.canAct(FileAction.UPLOAD_FILES, file)) {
      slider.addContent(FileActions.createButton(messages.uploadFilesButton(), FileAction.UPLOAD_FILES, file,
        "btn-default-alt", "btn-upload"));
    }

    // Create folder
    if (FileActions.canAct(FileAction.CREATE_FOLDER, file)) {
      slider.addContent(FileActions.createButton(messages.createFolderButton(), FileAction.CREATE_FOLDER, file,
        "btn-default-alt", "btn-plus"));
    }

    // Remove
    if (FileActions.canAct(FileAction.REMOVE, file)) {
      slider.addContent(
        FileActions.createButton(messages.removeButton(), FileAction.REMOVE, file, "btn-danger", "btn-ban"));
    }

    // PRESERVATION
    slider.addTitle(new Label(messages.preservationTitle()));

    // NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS

    // New process
    if (FileActions.canAct(FileAction.NEW_PROCESS, file)) {
      slider.addContent(FileActions.createButton(messages.newProcessPreservation(), FileAction.NEW_PROCESS, file,
        "btn-default-alt", "btn-play"));
    }

    // Identify formats
    if (FileActions.canAct(FileAction.IDENTIFY_FORMATS, file)) {
      slider.addContent(FileActions.createButton(messages.identifyFormatsButton(), FileAction.IDENTIFY_FORMATS, file,
        "btn-default-alt", "btn-play"));
    }

    // Show events
    if (FileActions.canAct(FileAction.SHOW_EVENTS, file)) {
      slider.addContent(FileActions.createButton(messages.preservationEvents(), FileAction.SHOW_EVENTS, file,
        "btn-default-alt", "btn-play"));
    }

    // Show risks
    if (FileActions.canAct(FileAction.SHOW_RISKS, file)) {
      slider.addContent(FileActions.createButton(messages.preservationRisks(), FileAction.SHOW_RISKS, file,
        "btn-default-alt", "btn-play"));
    }

  }

}
