/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.HashMap;
import java.util.Map.Entry;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class InfoSliderHelper {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private InfoSliderHelper() {
    // do nothing
  }

  protected static <T extends IsIndexed> void updateInfoObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedRepresentation) {
      updateInfoSliderPanel((IndexedRepresentation) object, slider);
    } else if (object instanceof IndexedAIP) {
      updateInfoSliderPanel((IndexedAIP) object, slider);
    } else {
      // do nothing
    }
  }

  private static void updateInfoSliderPanel(IndexedAIP aip, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (aip != null) {
      if (StringUtils.isNotBlank(aip.getLevel())) {
        values.put(messages.aipLevel(),
          new InlineHTML(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), true)));
      }

      if (StringUtils.isNotBlank(aip.getTitle())) {
        values.put(messages.aipGenericTitle(), new InlineHTML(SafeHtmlUtils.fromString(aip.getTitle())));
      }

      if (aip.getDateInitial() != null || aip.getDateFinal() != null) {
        values.put(messages.aipDates(), new InlineHTML(
          SafeHtmlUtils.fromString(Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), true))));
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void updateInfoSliderPanel(IndexedRepresentation representation, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (representation != null) {
      if (StringUtils.isNotBlank(messages.representationType())) {
        values.put(messages.representationType(),
          new InlineHTML(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true)));
      }

      if (StringUtils.isNotBlank(messages.representationFiles())) {
        values.put(messages.representationFiles(), new InlineHTML(SafeHtmlUtils.fromString(
          messages.numberOfFiles(representation.getNumberOfDataFiles(), representation.getNumberOfDataFolders()))));
      }

      if (representation.getNumberOfDataFiles() + representation.getNumberOfDataFolders() > 0) {
        values.put(messages.representationFiles(), new InlineHTML(SafeHtmlUtils.fromString(
          messages.numberOfFiles(representation.getNumberOfDataFiles(), representation.getNumberOfDataFolders()))));
      }

      values.put(messages.representationOriginal(), new InlineHTML(SafeHtmlUtils.fromString(
        representation.isOriginal() ? messages.originalRepresentation() : messages.alternativeRepresentation())));
    }

    populate(infoSliderPanel, values);
  }

  public static void updateInfoSliderPanel(BrowseFileBundle bundle, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));
    IndexedFile file = bundle.getFile();

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), createIdHTML(bundle, fileName, file.getUUID()));

      if (file.getSize() > 0) {
        values.put(messages.viewRepresentationInfoSize(),
          new InlineHTML(SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize()))));
      }

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), createMimetypeHTML(bundle, fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            createExtensionHTML(bundle, fileFormat.getFormatDesignation()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), createPronomHTML(bundle, fileFormat.getPronom()));
        }
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          createCreatingApplicationNameHTML(bundle, file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          createCreatingApplicationVersionHTML(bundle, file.getCreatingApplicationVersion()));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          new InlineHTML(SafeHtmlUtils.fromString(file.getDateCreatedByApplication())));
      }

      if (file.getHash() != null && !file.getHash().isEmpty()) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        for (String hash : file.getHash()) {
          if (first) {
            first = false;
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(hash));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));
        }
        values.put(messages.viewRepresentationInfoHash(), new InlineHTML(b.toSafeHtml()));
      }

      if (file.getStoragePath() != null) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void populate(SliderPanel infoSliderPanel, HashMap<String, Widget> values) {
    for (Entry<String, Widget> entry : values.entrySet()) {
      FlowPanel entryPanel = new FlowPanel();

      Label keyLabel = new Label(entry.getKey());
      Widget valueLabel = entry.getValue();

      entryPanel.add(keyLabel);
      entryPanel.add(valueLabel);
      infoSliderPanel.addContent(entryPanel);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entryPanel.addStyleName("infoFileEntry");
    }
  }

  private static FlowPanel createMimetypeHTML(BrowseFileBundle bundle, String mimetype) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_MIMETYPE, mimetype);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(mimetype),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_FORMAT_MIMETYPE),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createPronomHTML(BrowseFileBundle bundle, String pronom) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_PRONOM, pronom);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(pronom),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_PRONOM),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createExtensionHTML(BrowseFileBundle bundle, String designation) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_DESIGNATION, designation);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(designation),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_FORMAT_DESIGNATION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationNameHTML(BrowseFileBundle bundle, String createApplicationName) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_NAME, createApplicationName);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationName), riFilter, panel,
      bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_CREATING_APPLICATION_NAME),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationVersionHTML(BrowseFileBundle bundle,
    String createApplicationVersion) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_VERSION, createApplicationVersion);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationVersion), riFilter, panel,
      bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_CREATING_APPLICATION_VERSION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseFileBundle bundle, String filename, String uuid) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.INDEX_UUID, uuid);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(filename),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID),
      "browseFileInformationIcon");
    return panel;
  }
}
