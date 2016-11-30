/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowseFile;
import org.roda.wui.client.browse.BrowseFolder;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import config.i18n.client.ClientMessages;

public class BreadcrumbUtils {

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  public static List<BreadcrumbItem> getAipBreadcrumbs(List<IndexedAIP> aipAncestors, IndexedAIP aip) {
    return getAipBreadcrumbs(aipAncestors, aip, false);
  }

  public static List<BreadcrumbItem> getAipBreadcrumbs(List<IndexedAIP> aipAncestors, IndexedAIP aip, boolean events) {
    List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    breadcrumb
      .add(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", Browse.RESOLVER.getHistoryPath()));

    if (aipAncestors != null) {
      for (IndexedAIP ancestor : aipAncestors) {
        if (ancestor != null) {
          SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
          String breadcrumbTitle = getBreadcrumbTitle(ancestor);
          List<String> historyTokens = new ArrayList<String>();

          if (events) {
            historyTokens = getViewItemEventsHistoryToken(ancestor.getId());
          } else {
            historyTokens = getViewItemHistoryToken(ancestor.getId());
          }

          BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, breadcrumbTitle, historyTokens);
          breadcrumb.add(1, ancestorBreadcrumb);
        } else {
          SafeHtml breadcrumbLabel = DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.AIP_GHOST, false);
          BreadcrumbItem unknownAncestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, "", new Command() {

            @Override
            public void execute() {
              // TODO find better error message
              Toast.showError(messages.unknownAncestorError());
            }
          });
          breadcrumb.add(unknownAncestorBreadcrumb);
        }
      }
    }

    // AIP
    breadcrumb
      .add(new BreadcrumbItem(getBreadcrumbLabel(aip), getBreadcrumbTitle(aip), getViewItemHistoryToken(aip.getId())));

    return breadcrumb;
  }

  public static List<BreadcrumbItem> getRepresentatioBreadcrumbs(BrowseItemBundle itemBundle, String aipId,
    String representationUUID) {
    IndexedAIP aip = itemBundle.getAip();
    List<IndexedAIP> aipAncestors = itemBundle.getAIPAncestors();
    List<IndexedRepresentation> representations = itemBundle.getRepresentations();
    IndexedRepresentation representation = selectRepresentation(representations, representationUUID);

    List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    breadcrumb
      .add(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", Browse.RESOLVER.getHistoryPath()));

    if (aipAncestors != null) {
      for (IndexedAIP ancestor : aipAncestors) {
        if (ancestor != null) {
          SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
          String breadcrumbTitle = getBreadcrumbTitle(ancestor);
          BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, breadcrumbTitle,
            ListUtils.concat(Browse.RESOLVER.getHistoryPath(), ancestor.getId()));
          breadcrumb.add(1, ancestorBreadcrumb);
        } else {
          SafeHtml breadcrumbLabel = DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.AIP_GHOST, false);
          BreadcrumbItem unknownAncestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, "", new Command() {

            @Override
            public void execute() {
              // TODO find better error message
              Toast.showError(messages.unknownAncestorError());
            }
          });
          breadcrumb.add(unknownAncestorBreadcrumb);
        }
      }
    }

    // AIP
    breadcrumb.add(new BreadcrumbItem(getBreadcrumbLabel(aip), getBreadcrumbTitle(aip),
      ListUtils.concat(Browse.RESOLVER.getHistoryPath(), aipId)));

    // Representation
    breadcrumb.add(new BreadcrumbItem(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true),
      representation.getType(),
      ListUtils.concat(BrowseRepresentation.RESOLVER.getHistoryPath(), aipId, representationUUID)));

    return breadcrumb;
  }

  public static List<BreadcrumbItem> getFileBreadcrumbs(BrowseItemBundle itemBundle, String aipId,
    String representationUUID, IndexedFile file) {
    List<BreadcrumbItem> fullBreadcrumb = new ArrayList<>();
    List<BreadcrumbItem> fileBreadcrumb = new ArrayList<>();

    IndexedAIP aip = itemBundle.getAip();
    List<IndexedRepresentation> representations = itemBundle.getRepresentations();
    IndexedRepresentation representation = selectRepresentation(representations, representationUUID);

    // AIP breadcrumb
    fullBreadcrumb.add(new BreadcrumbItem(getBreadcrumbLabel(aip), getBreadcrumbTitle(aip),
      ListUtils.concat(Browse.RESOLVER.getHistoryPath(), aipId)));

    if (file != null) {
      List<String> filePath = file.getPath();
      List<String> fileAncestorsPath = file.getAncestorsPath();

      if (filePath != null && fileAncestorsPath != null && filePath.size() == fileAncestorsPath.size()) {
        for (int i = 0; i < filePath.size(); i++) {
          String folderName = filePath.get(i);
          String folderUUID = fileAncestorsPath.get(i);

          fileBreadcrumb
            .add(new BreadcrumbItem(getBreadcrumbLabel(folderName, RodaConstants.VIEW_REPRESENTATION_FOLDER),
              folderName, ListUtils.concat(BrowseFolder.RESOLVER.getHistoryPath(), aipId, representationUUID, folderUUID)));
        }
      }

      String fileLabel = file.getOriginalName() != null ? file.getOriginalName() : file.getId();

      fileBreadcrumb.add(new BreadcrumbItem(
        file.isDirectory() ? getBreadcrumbLabel(fileLabel, RodaConstants.VIEW_REPRESENTATION_FOLDER)
          : getBreadcrumbLabel(fileLabel, RodaConstants.VIEW_REPRESENTATION_FILE),
        fileLabel, ListUtils.concat(BrowseFile.RESOLVER.getHistoryPath(), aipId, representationUUID, file.getId())));
    }

    // Representation breadcrumb
    fullBreadcrumb.add(new BreadcrumbItem(
      DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true), representation.getType(),
      ListUtils.concat(BrowseRepresentation.RESOLVER.getHistoryPath(), aipId, representationUUID)));

    fullBreadcrumb.addAll(fileBreadcrumb);
    return fullBreadcrumb;
  }

  public static List<BreadcrumbItem> getTransferredResourceBreadcrumbs(TransferredResource r) {
    List<BreadcrumbItem> ret = new ArrayList<BreadcrumbItem>();

    ret.add(
      new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", IngestTransfer.RESOLVER.getHistoryPath()));
    if (r != null) {

      // add parent
      if (r.getParentUUID() != null) {
        List<String> path = new ArrayList<String>();
        path.addAll(IngestTransfer.RESOLVER.getHistoryPath());
        path.add(r.getParentUUID());
        SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getParentId());
        ret.add(new BreadcrumbItem(breadcrumbLabel, r.getParentId(), path));
      }

      // add self
      List<String> path = new ArrayList<String>();
      path.addAll(IngestTransfer.RESOLVER.getHistoryPath());
      path.add(r.getUUID());
      SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getName());
      ret.add(new BreadcrumbItem(breadcrumbLabel, r.getName(), path));
    }

    return ret;
  }

  private static IndexedRepresentation selectRepresentation(List<IndexedRepresentation> representations,
    String representationUUID) {
    IndexedRepresentation rep = null;
    for (IndexedRepresentation representation : representations) {
      if (representation.getUUID().equals(representationUUID)) {
        rep = representation;
      }
    }
    return rep;
  }

  private static SafeHtml getBreadcrumbLabel(String label, String level) {
    SafeHtml elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(level, false);
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(label));
    SafeHtml breadcrumbLabel = builder.toSafeHtml();
    return breadcrumbLabel;
  }

  private static SafeHtml getBreadcrumbLabel(IndexedAIP aip) {
    SafeHtml breadcrumbLabel;
    SafeHtml elementLevelIconSafeHtml;
    if (aip.getGhost()) {
      elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.AIP_GHOST, true);
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      builder.append(elementLevelIconSafeHtml);
      breadcrumbLabel = builder.toSafeHtml();
    } else {
      elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false);
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      String label = aip.getTitle() != null ? aip.getTitle() : aip.getId();
      builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(label));
      breadcrumbLabel = builder.toSafeHtml();
    }

    return breadcrumbLabel;
  }

  private static String getBreadcrumbTitle(IndexedAIP aip) {
    String title;
    if (aip.getGhost()) {
      title = "";
    } else {
      title = aip.getTitle() != null ? aip.getTitle() : aip.getId();
    }

    return title;
  }

  private static final List<String> getViewItemEventsHistoryToken(String id) {
    return ListUtils.concat(Browse.RESOLVER.getHistoryPath(), PreservationEvents.RESOLVER.getHistoryToken(), id);
  }

  private static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(Browse.RESOLVER.getHistoryPath(), id);
  }
}
