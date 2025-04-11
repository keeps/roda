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
import java.util.Map;
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
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.model.BrowseFileResponse;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
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

  public static HashMap<String, Widget> getRepresentationInfoDetailsMap(BrowseRepresentationResponse response) {
    HashMap<String, Widget> values = new HashMap<>();
    IndexedRepresentation representation = response.getIndexedRepresentation();

    values.put(messages.representationId(), createIdHTML(response));

    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      values.put(messages.aipCreated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy())));
    }

    if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      values.put(messages.aipUpdated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(representation.getType())) {
      values.put(messages.representationType(), createRepresentationTypeHTML(response));
    }

    return values;
  }

  public static void updateInfoSliderPanel(BrowseRepresentationResponse response, SliderPanel infoSliderPanel) {
    IndexedRepresentation representation = response.getIndexedRepresentation();

    HashMap<String, Widget> values = getRepresentationInfoDetailsMap(response);
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedRepresentation.class.getName())));

    addLinkIfCentralInstance(values, representation.getInstanceName(), representation.isLocalInstance(),
      representation.getInstanceId());

    populate(infoSliderPanel, values);
  }

  public static HashMap<String, Widget> getAipInfoDetailsMap(BrowseAIPResponse response) {
    HashMap<String, Widget> values = new HashMap<>();
    IndexedAIP aip = response.getIndexedAIP();

    values.put(messages.itemId(), createIdHTML(response));

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      values.put(messages.aipCreated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy())));
    }

    if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      values.put(messages.aipUpdated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(aip.getLevel())) {
      values.put(messages.aipLevel(), createAipLevelHTML(response));
    }

    if (StringUtils.isNotBlank(aip.getType())) {
      values.put(messages.aipType(), createAipTypeHTML(response));
    }

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

    return values;
  }

  public static void updateInfoSliderPanel(BrowseAIPResponse response, SliderPanel infoSliderPanel) {
    IndexedAIP aip = response.getIndexedAIP();

    HashMap<String, Widget> values = getAipInfoDetailsMap(response);
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedAIP.class.getName())));

    addLinkIfCentralInstance(values, response.getIndexedAIP().getInstanceName(),
      response.getIndexedAIP().isLocalInstance(), aip.getInstanceId());

    if (!response.getIndexedAIP().getPermissions().getUsers().equals(new Permissions().getUsers())
      || !response.getIndexedAIP().getPermissions().getGroups().equals(new Permissions().getGroups())) {
      values.put(messages.aipPermissionDetails(), createAipPermissionDetailsHTML(response.getIndexedAIP()));
    }
    populate(infoSliderPanel, values);
  }

  private static Widget createAipPermissionDetailsHTML(IndexedAIP aip) {
    Permissions permissions = aip.getPermissions();

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
    if (aipActions.canAct(AipActions.AipAction.UPDATE_PERMISSIONS, aip).canAct()) {
      table.addStyleName("slider-aip-permissions-table-with-grant");
      SingleSelectionModel<Entry<String, Set<Permissions.PermissionType>>> selectionModel = new SingleSelectionModel<>(
        item -> item.getKey().substring(2));
      selectionModel.addSelectionChangeHandler(event -> aipActions.act(AipActions.AipAction.UPDATE_PERMISSIONS, aip));
      table.setSelectionModel(selectionModel);
    }

    ListDataProvider<Entry<String, Set<Permissions.PermissionType>>> dataProvider = new ListDataProvider<>(entryList);
    dataProvider.addDataDisplay(table);

    return table;
  }

  public static HashMap<String, Widget> getFileInfoDetailsMap(IndexedFile file, List<String> riRules) {
    HashMap<String, Widget> values = new HashMap<>();

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), createIdHTML(riRules, fileName, file.getUUID()));

      if (file.getSize() > 0) {
        values.put(messages.viewRepresentationInfoSize(),
          new InlineHTML(SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize()))));
      }

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getExtension())) {
          values.put(messages.viewRepresentationInfoExtension(),
            createExtensionHTML(riRules, fileFormat.getExtension()));
        }

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), createMimetypeHTML(riRules, fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            createFormatDesignationHTML(riRules, fileFormat.getFormatDesignation()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), createPronomHTML(riRules, fileFormat.getPronom()));
        }
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          createCreatingApplicationNameHTML(riRules, file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          createCreatingApplicationVersionHTML(riRules, file.getCreatingApplicationVersion()));
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
    return values;
  }

  public static void createFileInfoSliderPanel(IndexedFile file, BrowseFileResponse response,
    SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = getFileInfoDetailsMap(file, response.getRepresentationInformationFields());
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.oneOfAObject(IndexedFile.class.getName())));

    Long risksCounter = response.getRiskCounterResponse().getResult();
    Long preservationEventsCounter = response.getPreservationCounterResponse().getResult();

    if (file != null) {
      addLinkIfCentralInstance(values, file.getInstanceName(), file.isLocalInstance(), file.getInstanceId());

      List<String> history = new ArrayList<>();
      history.add(file.getAipId());
      history.add(file.getRepresentationId());
      history.addAll(file.getPath());
      history.add(file.getId());

      if (risksCounter >= 0) {
        Anchor risksLink = new Anchor(messages.aipRiskIncidences(risksCounter),
          HistoryUtils.createHistoryHashLink(RiskIncidenceRegister.RESOLVER, history));
        values.put(messages.preservationRisks(), risksLink);
      }

      if (preservationEventsCounter >= 0) {
        Anchor eventsLink = new Anchor(messages.aipEvents(preservationEventsCounter),
          HistoryUtils.createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, file.getAipId(),
            file.getRepresentationUUID(), file.getUUID()));
        values.put(messages.preservationEvents(), eventsLink);
      }

      SafeUri uri = RestUtils.createTechnicalMetadataHTMLUri(file.getUUID());
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
            } else {
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

  private static FlowPanel createExtensionHTML(List<String> representationInformationFields, String extension) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_EXTENSION, extension);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(extension),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_EXTENSION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createMimetypeHTML(List<String> representationInformationFields, String mimetype) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_MIMETYPE, mimetype);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(mimetype),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_FORMAT_MIMETYPE),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createPronomHTML(List<String> representationInformationFields, String pronom) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_PRONOM, pronom);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(pronom),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_PRONOM),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createFormatDesignationHTML(List<String> representationInformationFields,
    String designation) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_DESIGNATION, designation);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(designation),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_FORMAT_DESIGNATION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationNameHTML(List<String> representationInformationFields,
    String createApplicationName) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_NAME, createApplicationName);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationName), riFilter, panel,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_NAME),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationVersionHTML(List<String> representationInformationFields,
    String createApplicationVersion) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_VERSION, createApplicationVersion);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationVersion), riFilter, panel,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_VERSION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(List<String> representationInformationFields, String filename, String uuid) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.INDEX_UUID, uuid);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(filename),
      riFilter, panel, representationInformationFields.contains(RodaConstants.INDEX_UUID), "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.INDEX_UUID, aip.getId());

    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getId()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_UUID, representation.getUUID());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getId()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.INDEX_UUID));

    return panel;
  }

  private static FlowPanel createAipTypeHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_TYPE, aip.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getType()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.AIP_TYPE));
    return panel;
  }

  private static FlowPanel createAipLevelHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_LEVEL, aip.getLevel());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(DescriptionLevelUtils.getElementLevelLabel(aip.getLevel())), riFilter, panel,
      response.getRepresentationInformationFields().contains(RodaConstants.AIP_LEVEL));

    return panel;
  }

  private static FlowPanel createRepresentationTypeHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_TYPE, representation.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getType()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.REPRESENTATION_TYPE));

    return panel;
  }

  public static void addLinkIfCentralInstance(Map<String, Widget> values, String instanceName, boolean localToInstance,
    String instanceId) {
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
