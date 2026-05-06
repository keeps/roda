package org.roda.wui.client.browse.tabs.file;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FileDetailsTab extends GenericMetadataCardPanel<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final List<String> riRules;

  public FileDetailsTab(IndexedFile data, List<String> riRules) {
    this.riRules = riRules;
    setData(data);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedFile data) {
    return null;
  }

  @Override
  protected void buildFields(IndexedFile file) {
    String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();

    addSeparator(messages.detailsFile());

    buildField(messages.viewRepresentationInfoFilename()).withWidget(createIdHTML(riRules, fileName, file.getUUID()))
      .build();
    if (file.getSize() > 0) {
      buildField(messages.viewRepresentationInfoSize())
        .withHtml(SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize()))).build();
    }

    if (file.getFileFormat() != null) {
      FileFormat fileFormat = file.getFileFormat();
      buildField(messages.viewRepresentationInfoExtension())
        .withWidget(createExtensionHTML(riRules, fileFormat.getExtension())).build();
      buildField(messages.viewRepresentationInfoMimetype())
        .withWidget(createMimetypeHTML(riRules, fileFormat.getMimeType())).build();
      buildField(messages.viewRepresentationInfoFormat())
        .withWidget(createFormatDesignationHTML(riRules, fileFormat.getFormatDesignation())).build();
      buildField(messages.viewRepresentationInfoPronom()).withWidget(createPronomHTML(riRules, fileFormat.getPronom()))
        .build();
    }

    buildField(messages.viewRepresentationInfoCreatingApplicationName())
      .withWidget(createCreatingApplicationNameHTML(riRules, file.getCreatingApplicationName())).build();

    buildField(messages.viewRepresentationInfoDateCreatedByApplication())
      .withWidget(createCreatingApplicationVersionHTML(riRules, file.getCreatingApplicationVersion())).build();

    buildField(messages.viewRepresentationInfoDateCreatedByApplication())
      .withValue(file.getDateCreatedByApplication()).build();

    if (file.getHash() != null && !file.getHash().isEmpty()) {
      FlowPanel list = new FlowPanel();
      list.addStyleName("generic-multiline");
      for (String hash : file.getHash()) {
        list.add(new HTMLPanel("span", SafeHtmlUtils.htmlEscape(hash)));
      }
      buildField(messages.viewRepresentationInfoHash()).withWidget(list).build();
    }

    if (file.isReference()) {
      buildField(messages.viewRepresentationInfoStoragePath())
        .withHtml(SafeHtmlUtils.fromString(file.getReferenceURL())).build();
    } else {
      buildField(messages.viewRepresentationInfoStoragePath()).withValue(file.getStoragePath()).build();
    }
  }

  private FlowPanel createRepresentationInformationWrapperPanel(String value, String riFilter, boolean createIcon) {
    FlowPanel panel = new FlowPanel();
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(value), riFilter,
      panel, createIcon, "browseFileInformationIcon");
    return panel;
  }

  private FlowPanel createCreatingApplicationVersionHTML(List<String> representationInformationFields,
    String createApplicationVersion) {
    if (StringUtils.isBlank(createApplicationVersion)) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_VERSION, createApplicationVersion);

    return createRepresentationInformationWrapperPanel(createApplicationVersion, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_VERSION));
  }

  private FlowPanel createExtensionHTML(List<String> representationInformationFields, String extension) {
    if (StringUtils.isBlank(extension)) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_EXTENSION, extension);
    return createRepresentationInformationWrapperPanel(extension, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_EXTENSION));
  }

  private FlowPanel createMimetypeHTML(List<String> representationInformationFields, String mimetype) {
    if (StringUtils.isBlank(mimetype)) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_MIMETYPE, mimetype);

    return createRepresentationInformationWrapperPanel(mimetype, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_FORMAT_MIMETYPE));
  }

  private FlowPanel createFormatDesignationHTML(List<String> representationInformationFields, String designation) {
    if (StringUtils.isBlank(designation)) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_DESIGNATION, designation);
    return createRepresentationInformationWrapperPanel(designation, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_FORMAT_DESIGNATION));
  }

  private FlowPanel createPronomHTML(List<String> representationInformationFields, String pronom) {
    if (StringUtils.isBlank(pronom)) {
      return null;
    }
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_PRONOM, pronom);

    return createRepresentationInformationWrapperPanel(pronom, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_PRONOM));
  }

  private FlowPanel createCreatingApplicationNameHTML(List<String> representationInformationFields,
    String createApplicationName) {
    if (StringUtils.isBlank(createApplicationName)) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_NAME, createApplicationName);

    return createRepresentationInformationWrapperPanel(createApplicationName, riFilter,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_NAME));
  }

  private FlowPanel createIdHTML(List<String> representationInformationFields, String filename, String uuid) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.INDEX_UUID, uuid);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(filename),
      riFilter, panel, representationInformationFields.contains(RodaConstants.INDEX_UUID), "browseFileInformationIcon");
    return panel;
  }
}
