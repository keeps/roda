/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.distributed.ShowDistributedInstance;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import config.i18n.client.ClientMessages;

public class InfoSliderHelper {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private InfoSliderHelper() {
    // do nothing
  }

  protected static <T extends IsIndexed> void updateInfoObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedAIP) {
      updateInfoSliderPanel((IndexedAIP) object, slider);
    } else if (object instanceof IndexedRepresentation) {
      updateInfoSliderPanel((IndexedRepresentation) object, slider);
    } else {
      // do nothing
    }
  }

  private static void updateInfoSliderPanel(IndexedAIP aip, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedAIP.class.getName())));

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
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedRepresentation.class.getName())));

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

  public static void updateInfoSliderPanel(BrowseRepresentationBundle bundle, SliderPanel infoSliderPanel) {
    IndexedRepresentation representation = bundle.getRepresentation();

    HashMap<String, Widget> values = new HashMap<>();
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedRepresentation.class.getName())));

    values.put(messages.representationId(), createIdHTML(bundle));

    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      values.put(messages.aipCreated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy())));
    }

    if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      values.put(messages.aipUpdated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(representation.getType())) {
      values.put(messages.representationType(), createRepresentationTypeHTML(bundle));
    }

    addLinkIfCentralInstance(values, bundle.getInstanceName(), bundle.isLocalToInstance(),
      representation.getInstanceId());

    populate(infoSliderPanel, values);
  }

  public static void updateInfoSliderPanel(BrowseAIPBundle bundle, SliderPanel infoSliderPanel) {
    IndexedAIP aip = bundle.getAip();

    HashMap<String, Widget> values = new HashMap<>();
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedAIP.class.getName())));

    values.put(messages.itemId(), createIdHTML(bundle));

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      values.put(messages.aipCreated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy())));
    }

    if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      values.put(messages.aipUpdated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(aip.getLevel())) {
      values.put(messages.aipLevel(), createAipLevelHTML(bundle));
    }

    if (StringUtils.isNotBlank(aip.getType())) {
      values.put(messages.aipType(), createAipTypeHTML(bundle));
    }

    addLinkIfCentralInstance(values, bundle.getInstanceName(), bundle.isLocalToInstance(), aip.getInstanceId());

    if (!aip.getIngestSIPIds().isEmpty()) {
      FlowPanel sipIds = new FlowPanel();
      for (String ingestSIPId : aip.getIngestSIPIds()) {
        sipIds.add(new HTMLPanel("p", ingestSIPId));
      }
      values.put(messages.sipId(), sipIds);
    }

    if (StringUtils.isNotBlank(aip.getIngestJobId())) {
      Anchor anchor = new Anchor();
      anchor.setText(aip.getIngestJobId());
      anchor.setHref(HistoryUtils.createHistoryHashLink(ShowJob.RESOLVER, aip.getIngestJobId(),
        RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, aip.getId()));

      values.put(messages.processIdTitle(), anchor);
    }

    if (!aip.getIngestUpdateJobIds().isEmpty()) {
      FlowPanel jobIdsList = new FlowPanel();
      jobIdsList.addStyleName("slider-info-entry-value-aip-ingest-jobs");

      for (String updateJobId : aip.getIngestUpdateJobIds()) {
        Anchor anchor = new Anchor();
        anchor.setText(updateJobId);
        anchor.setHref(HistoryUtils.createHistoryHashLink(ShowJob.RESOLVER, updateJobId,
          RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, aip.getId()));
        jobIdsList.add(anchor);
      }

      values.put(messages.updateProcessIdTitle(), jobIdsList);
    }

    if (!bundle.getAip().getPermissions().getUsers().equals(new Permissions().getUsers())
      || !bundle.getAip().getPermissions().getGroups().equals(new Permissions().getGroups())) {
      values.put(messages.aipPermissionDetails(), createAipPermissionDetailsHTML(bundle));
    }
    populate(infoSliderPanel, values);
  }

  private static Widget createAipPermissionDetailsHTML(BrowseAIPBundle bundle) {
    Permissions permissions = bundle.getAip().getPermissions();

    final String CSS_HAS_PERMISSION = "";
    final String CSS_NO_PERMISSION = " slider-aip-permissions-table-icon-fade";

    List<Entry<String, Set<Permissions.PermissionType>>> entryList = new ArrayList<>();
    for (String username : new TreeSet<>(permissions.getUsernames())) {
      entryList.add(new AbstractMap.SimpleEntry<>("u-" + username, permissions.getUserPermissions(username)));
    }
    for (String groupname : new TreeSet<>(permissions.getGroupnames())) {
      entryList.add(new AbstractMap.SimpleEntry<>("g-" + groupname, permissions.getGroupPermissions(groupname)));
    }

    CellTable<Entry<String, Set<Permissions.PermissionType>>> table = new CellTable<>();
    table.addStyleName("slider-aip-permissions-table");

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> userGroupIconColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        if (object.getKey().startsWith("u-")) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>");
        } else {
          return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-users'></i>");
        }
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> nameColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String name = object.getKey().substring(2);
        return SafeHtmlUtils.fromSafeConstant(
          "<span title='" + SafeHtmlUtils.htmlEscape(name) + "'>" + SafeHtmlUtils.htmlEscape(name) + "</span>");
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> iconReadColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String extraIconCss = object.getValue().contains(Permissions.PermissionType.READ) ? CSS_HAS_PERMISSION
          : CSS_NO_PERMISSION;
        return SafeHtmlUtils
          .fromSafeConstant("<i title='" + messages.objectPermissionDescription(Permissions.PermissionType.READ)
            + "' class='fa fa-eye" + extraIconCss + "'></i>");
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> iconCreateColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String extraIconCss = object.getValue().contains(Permissions.PermissionType.CREATE) ? CSS_HAS_PERMISSION
          : CSS_NO_PERMISSION;
        return SafeHtmlUtils
          .fromSafeConstant("<i title='" + messages.objectPermissionDescription(Permissions.PermissionType.CREATE)
            + "' class='fa fa-sitemap" + extraIconCss + "'></i>");
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> iconEditColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String extraIconCss = object.getValue().contains(Permissions.PermissionType.UPDATE) ? CSS_HAS_PERMISSION
          : CSS_NO_PERMISSION;
        return SafeHtmlUtils
          .fromSafeConstant("<i title='" + messages.objectPermissionDescription(Permissions.PermissionType.UPDATE)
            + "' class='fa fa-edit" + extraIconCss + "'></i>");
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> iconDeleteColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String extraIconCss = object.getValue().contains(Permissions.PermissionType.DELETE) ? CSS_HAS_PERMISSION
          : CSS_NO_PERMISSION;
        return SafeHtmlUtils
          .fromSafeConstant("<i title='" + messages.objectPermissionDescription(Permissions.PermissionType.DELETE)
            + "' class='fa fa-ban" + extraIconCss + "'></i>");
      }
    };

    Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml> iconGrantColumn = new Column<Entry<String, Set<Permissions.PermissionType>>, SafeHtml>(
      new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Entry<String, Set<Permissions.PermissionType>> object) {
        String extraIconCss = object.getValue().contains(Permissions.PermissionType.GRANT) ? CSS_HAS_PERMISSION
          : CSS_NO_PERMISSION;
        return SafeHtmlUtils
          .fromSafeConstant("<i title='" + messages.objectPermissionDescription(Permissions.PermissionType.GRANT)
            + "' class='fa fa-unlock" + extraIconCss + "'></i>");
      }
    };

    table.addColumn(userGroupIconColumn);
    table.addColumn(nameColumn);
    table.addColumn(iconReadColumn);
    table.addColumn(iconCreateColumn);
    table.addColumn(iconEditColumn);
    table.addColumn(iconDeleteColumn);
    table.addColumn(iconGrantColumn);

    table.setColumnWidth(userGroupIconColumn, 23, Style.Unit.PX);
    table.setColumnWidth(iconReadColumn, 23, Style.Unit.PX);
    table.setColumnWidth(iconCreateColumn, 23, Style.Unit.PX);
    table.setColumnWidth(iconEditColumn, 23, Style.Unit.PX);
    table.setColumnWidth(iconDeleteColumn, 23, Style.Unit.PX);
    table.setColumnWidth(iconGrantColumn, 23, Style.Unit.PX);

    nameColumn.setCellStyleNames("nowrap slider-aip-permissions-table-name");

    AipActions aipActions = AipActions.get();
    if (aipActions.canAct(AipActions.AipAction.UPDATE_PERMISSIONS, bundle.getAip())) {
      table.addStyleName("slider-aip-permissions-table-with-grant");
      SingleSelectionModel<Entry<String, Set<Permissions.PermissionType>>> selectionModel = new SingleSelectionModel<>(
        item -> item.getKey().substring(2));
      selectionModel
        .addSelectionChangeHandler(event -> aipActions.act(AipActions.AipAction.UPDATE_PERMISSIONS, bundle.getAip()));
      table.setSelectionModel(selectionModel);
    }

    ListDataProvider<Entry<String, Set<Permissions.PermissionType>>> dataProvider = new ListDataProvider<>(entryList);
    dataProvider.addDataDisplay(table);

    return table;
  }

  public static void updateInfoSliderPanel(BrowseFileBundle bundle, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedFile.class.getName())));
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

        if (StringUtils.isNotBlank(fileFormat.getExtension())) {
          values.put(messages.viewRepresentationInfoExtension(),
            createExtensionHTML(bundle, fileFormat.getExtension()));
        }

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), createMimetypeHTML(bundle, fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            createFormatDesignationHTML(bundle, fileFormat.getFormatDesignation()));
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
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      if (file.isReference()) {
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getReferenceURL()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
      } else {
        if (file.getStoragePath() != null) {
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

          values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
        }
      }
    }

    addLinkIfCentralInstance(values, bundle.getInstanceName(), bundle.isLocalToInstance(), file.getInstanceId());

    List<String> history = new ArrayList<>();
    history.add(file.getAipId());
    history.add(file.getRepresentationId());
    history.addAll(file.getPath());
    history.add(file.getId());

    Long preservationEventCount = bundle.getPreservationEventCount();
    Long riskIncidenceCount = bundle.getRiskIncidenceCount();

    if (riskIncidenceCount >= 0) {
      Anchor risksLink = new Anchor(messages.aipRiskIncidences(bundle.getRiskIncidenceCount()),
        HistoryUtils.createHistoryHashLink(RiskIncidenceRegister.RESOLVER, history));
      values.put(messages.preservationRisks(), risksLink);
    }

    if (preservationEventCount >= 0) {
      Anchor eventsLink = new Anchor(messages.aipEvents(bundle.getPreservationEventCount()),
        HistoryUtils.createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, file.getAipId(),
          file.getRepresentationUUID(), file.getUUID()));
      values.put(messages.preservationEvents(), eventsLink);
    }

    SafeUri uri = RestUtils.createTechnicalMetadataHTMLUri(file.getAipId(), file.getUUID(), "html", null);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());

    Anchor technicalInformationAnchor = new Anchor();
    technicalInformationAnchor.setStyleName("clickable");
    technicalInformationAnchor.setText(messages.showTechnicalMetadata());

    // technicalInformation
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            if (!response.getText().isEmpty()) {
              values.put(messages.viewTechnicalInformation(), technicalInformationAnchor);
              technicalInformationAnchor
                .addClickHandler(e -> Dialogs.showTechnicalMetadataInformation(messages.viewTechnicalMetadata(),
                  messages.downloadButton(), messages.closeButton(), file, response.getText()));
            }

          }
          else {
            values.put(messages.viewTechnicalInformation(), technicalInformationAnchor);
            technicalInformationAnchor
              .addClickHandler(e -> Dialogs.showTechnicalMetadataInformation(messages.viewTechnicalMetadata(),
                messages.downloadButton(), messages.closeButton(), file, null));
          }
          populate(infoSliderPanel, values);
        }

        @Override
        public void onError(Request request, Throwable throwable) {
          populate(infoSliderPanel, values);
        }
      });
    } catch (RequestException e) {
      throw new RuntimeException(e);
    }

  }

  private static void populate(SliderPanel infoSliderPanel, HashMap<String, Widget> values) {
    for (Entry<String, Widget> entry : values.entrySet()) {
      FlowPanel entryPanel = new FlowPanel();

      Label keyLabel = new Label(entry.getKey());
      Widget valueLabel = entry.getValue();

      entryPanel.add(keyLabel);
      entryPanel.add(valueLabel);
      infoSliderPanel.addContent(entryPanel);

      keyLabel.addStyleName("slider-info-entry-key");
      valueLabel.addStyleName("slider-info-entry-value");
      entryPanel.addStyleName("slider-info-entry");
    }
  }

  private static FlowPanel createExtensionHTML(BrowseFileBundle bundle, String extension) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_EXTENSION, extension);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(extension),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.FILE_EXTENSION),
      "browseFileInformationIcon");
    return panel;
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

  private static FlowPanel createFormatDesignationHTML(BrowseFileBundle bundle, String designation) {
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

  private static FlowPanel createIdHTML(BrowseAIPBundle bundle) {
    IndexedAIP aip = bundle.getAip();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.INDEX_UUID, aip.getId());

    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getId()),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseRepresentationBundle bundle) {
    IndexedRepresentation representation = bundle.getRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_UUID, representation.getUUID());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getId()), riFilter, panel,
      bundle.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID));

    return panel;
  }

  private static FlowPanel createAipTypeHTML(BrowseAIPBundle bundle) {
    IndexedAIP aip = bundle.getAip();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_TYPE, aip.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getType()),
      riFilter, panel, bundle.getRepresentationInformationFields().contains(RodaConstants.AIP_TYPE));
    return panel;
  }

  private static FlowPanel createAipLevelHTML(BrowseAIPBundle bundle) {
    IndexedAIP aip = bundle.getAip();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_LEVEL, aip.getLevel());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(DescriptionLevelUtils.getElementLevelLabel(aip.getLevel())), riFilter, panel,
      bundle.getRepresentationInformationFields().contains(RodaConstants.AIP_LEVEL));

    return panel;
  }

  private static FlowPanel createRepresentationTypeHTML(BrowseRepresentationBundle bundle) {
    IndexedRepresentation representation = bundle.getRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_TYPE, representation.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getType()), riFilter, panel,
      bundle.getRepresentationInformationFields().contains(RodaConstants.REPRESENTATION_TYPE));

    return panel;
  }

  public static void addLinkIfCentralInstance(HashMap<String, Widget> values, String instanceName,
    boolean localToInstance, String instanceId) {
    if (StringUtils.isNotBlank(instanceId)) {
      String distributedMode = ConfigurationManager.getStringWithDefault(
        RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);
      if (RodaConstants.DistributedModeType.CENTRAL.name().equals(distributedMode)) {
        if (localToInstance) {
          values.put(messages.distributedInstanceLabel(), new Label(instanceName));
        } else {
          Anchor anchor = new Anchor();
          if (StringUtils.isNotBlank(instanceName)) {
            anchor.setText(instanceName);
          } else {
            anchor.setText(instanceId);
          }
          anchor.setHref(HistoryUtils.createHistoryHashLink(ShowDistributedInstance.RESOLVER, instanceId));
          values.put(messages.distributedInstanceLabel(), anchor);
        }
      } else {
        values.put(messages.itemInstanceId(), new Label(instanceId));
      }
    }
  }
}
