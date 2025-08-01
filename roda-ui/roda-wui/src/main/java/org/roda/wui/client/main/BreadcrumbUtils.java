/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.disposal.DisposalDestroyedRecords;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import config.i18n.client.ClientMessages;

public class BreadcrumbUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private BreadcrumbUtils() {
    // do nothing
  }

  private static BreadcrumbItem getBreadcrumbItem(IndexedAIP aip) {
    return new BreadcrumbItem(getBreadcrumbLabel(aip), getBreadcrumbTitle(aip), getViewItemHistoryToken(aip.getId()));
  }

  public static BreadcrumbItem getBreadcrumbItem(IndexedRepresentation representation) {
    return new BreadcrumbItem(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true),
      representation.getType(), HistoryUtils.getHistoryBrowse(representation));
  }

  private static BreadcrumbItem getBreadcrumbItem(IndexedFile file) {
    String fileLabel = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
    SafeHtml breadcrumbLabel = getBreadcrumbLabel(fileLabel, getFileLevel(file));
    return new BreadcrumbItem(breadcrumbLabel, fileLabel, HistoryUtils.getHistoryBrowse(file));
  }

  private static String getFileLevel(IndexedFile file) {
    if (file.isDirectory()) {
      return RodaConstants.VIEW_REPRESENTATION_FOLDER;
    } else if (file.isReference()) {
      return RodaConstants.VIEW_REPRESENTATION_FILE_REFERENCE;
    } else {
      return RodaConstants.VIEW_REPRESENTATION_FILE;
    }
  }

  public static List<BreadcrumbItem> getAipBreadcrumbs(List<IndexedAIP> aipAncestors, IndexedAIP aip) {
    return getAipBreadcrumbs(aipAncestors, aip, false);
  }

  public static List<BreadcrumbItem> getAipBreadcrumbs(IndexedAIP aip) {
    return List.of(getBreadcrumbItem(aip));
  }

  public static List<BreadcrumbItem> getDIPBreadcrumbs(IndexedAIP aip, IndexedDIP dip, DIPFile dipFile,
    List<DIPFile> dipFileAncestors) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    // Catalogue
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle()),
      messages.allCollectionsTitle(), BrowseTop.RESOLVER.getHistoryPath()));
    // AIP
    ret.add(getBreadcrumbItem(aip));
    // DIP
    ret.add(getBreadcrumbItem(dip));
    if (dipFile != null) {
      // DIP File ancestors
      if (dipFile.getAncestorsUUIDs() != null) {
        for (DIPFile dipFileAncestor : dipFileAncestors) {
          ret.add(getBreadcrumbItem(dipFileAncestor));
        }
      }
      // DIP File
      ret.add(getBreadcrumbItem(dipFile));
    }
    return ret;
  }

  public static List<BreadcrumbItem> getAipBreadcrumbs(List<IndexedAIP> aipAncestors, IndexedAIP aip, boolean events) {
    List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    breadcrumb.add(firstBreadcrumbItem(aip));

    if (aipAncestors != null) {
      for (IndexedAIP ancestor : aipAncestors) {
        if (ancestor != null) {
          SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
          String breadcrumbTitle = getBreadcrumbTitle(ancestor);
          List<String> historyTokens;

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
              Toast.showError(messages.unknownAncestorError());
            }
          });
          breadcrumb.add(unknownAncestorBreadcrumb);
        }
      }
    }

    // AIP
    breadcrumb.add(getBreadcrumbItem(aip));

    return breadcrumb;
  }

  private static BreadcrumbItem firstBreadcrumbItem(IndexedAIP aip) {
    SafeHtml breadcrumbLabel = SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle());
    String breadcrumbTitle = messages.allCollectionsTitle();
    List<String> breadcrumbPath = BrowseTop.RESOLVER.getHistoryPath();

    if (AIPState.UNDER_APPRAISAL.equals(aip.getState())) {
      breadcrumbLabel = SafeHtmlUtils.fromSafeConstant(messages.ingestAppraisalTitle());
      breadcrumbTitle = messages.ingestAppraisalTitle();
      breadcrumbPath = IngestAppraisal.RESOLVER.getHistoryPath();
    }

    if (AIPState.DESTROYED.equals(aip.getState())) {
      breadcrumbLabel = SafeHtmlUtils.fromSafeConstant(messages.disposalDestroyedRecordsTitle());
      breadcrumbTitle = messages.disposalDestroyedRecordsTitle();
      breadcrumbPath = DisposalDestroyedRecords.RESOLVER.getHistoryPath();
    }

    return new BreadcrumbItem(breadcrumbLabel, breadcrumbTitle, breadcrumbPath);
  }

  public static List<BreadcrumbItem> getRepresentationBreadcrumbs(List<IndexedAIP> aipAncestors, IndexedAIP aip,
    IndexedRepresentation representation) {
    List<BreadcrumbItem> breadcrumb = new ArrayList<>();
    breadcrumb.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle()),
      messages.allCollectionsTitle(), BrowseTop.RESOLVER.getHistoryPath()));

    if (aipAncestors != null) {
      for (IndexedAIP ancestor : aipAncestors) {
        if (ancestor != null) {
          SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
          String breadcrumbTitle = getBreadcrumbTitle(ancestor);
          BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, breadcrumbTitle,
            ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), ancestor.getId()));
          breadcrumb.add(1, ancestorBreadcrumb);
        } else {
          SafeHtml breadcrumbLabel = DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.AIP_GHOST, false);
          BreadcrumbItem unknownAncestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, "", new Command() {

            @Override
            public void execute() {
              Toast.showError(messages.unknownAncestorError());
            }
          });
          breadcrumb.add(unknownAncestorBreadcrumb);
        }
      }
    }

    // AIP
    breadcrumb.add(getBreadcrumbItem(aip));

    // Representation
    breadcrumb.add(getBreadcrumbItem(representation));

    return breadcrumb;
  }

  public static List<BreadcrumbItem> getRepresentationBreadcrumbs(IndexedAIP aip,
    IndexedRepresentation representation) {
    return Arrays.asList(getBreadcrumbItem(aip), getBreadcrumbItem(representation));
  }

  public static List<BreadcrumbItem> getDIPBreadcrumbs(IndexedAIP aip, IndexedRepresentation representation,
    IndexedDIP dip, DIPFile dipFile, List<DIPFile> dipFileAncestors) {
    List<BreadcrumbItem> ret = new ArrayList<>();

    // Catalogue
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle()),
      messages.allCollectionsTitle(), BrowseTop.RESOLVER.getHistoryPath()));

    // AIP and Representation
    ret.add(getBreadcrumbItem(aip));
    ret.add(getBreadcrumbItem(representation));

    // DIP
    ret.add(getBreadcrumbItem(dip));
    if (dipFile != null) {
      // DIP File ancestors
      if (dipFile.getAncestorsUUIDs() != null) {
        for (DIPFile dipFileAncestor : dipFileAncestors) {
          ret.add(getBreadcrumbItem(dipFileAncestor));
        }
      }
      // DIP File
      ret.add(getBreadcrumbItem(dipFile));
    }
    return ret;
  }

  public static List<BreadcrumbItem> getFileBreadcrumbs(IndexedAIP aip, IndexedRepresentation representation,
    IndexedFile file) {

    List<BreadcrumbItem> fullBreadcrumb = new ArrayList<>();
    List<BreadcrumbItem> fileBreadcrumb = new ArrayList<>();

    // AIP
    fullBreadcrumb.add(getBreadcrumbItem(aip));

    // Representation
    fullBreadcrumb.add(getBreadcrumbItem(representation));

    if (file != null) {
      // File directory path
      List<String> filePath = file.getPath();
      List<String> fileAncestorsPath = file.getAncestorsPath();

      if (filePath != null && fileAncestorsPath != null && filePath.size() == fileAncestorsPath.size()) {
        for (int i = 0; i < filePath.size(); i++) {
          final String folderName = filePath.get(i);
          final String folderUUID = fileAncestorsPath.get(i);

          SafeHtml breadcrumbLabel = getBreadcrumbLabel(folderName, RodaConstants.VIEW_REPRESENTATION_FOLDER);
          fileBreadcrumb.add(new BreadcrumbItem(breadcrumbLabel, folderName, new Command() {

            @Override
            public void execute() {
              HistoryUtils.resolve(IndexedFile.class.getName(), folderUUID);
            }
          }));
        }
      }

      // File item
      fileBreadcrumb.add(getBreadcrumbItem(file));
    }

    fullBreadcrumb.addAll(fileBreadcrumb);
    return fullBreadcrumb;
  }

  public static List<BreadcrumbItem> getDIPBreadcrumbs(IndexedAIP aip, IndexedRepresentation representation,
    IndexedFile file, IndexedDIP dip, DIPFile dipFile, List<DIPFile> dipFileAncestors) {
    List<BreadcrumbItem> fullBreadcrumb = new ArrayList<>();
    List<BreadcrumbItem> fileBreadcrumb = new ArrayList<>();

    // Catalogue
    fullBreadcrumb.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle()),
      messages.allCollectionsTitle(), BrowseTop.RESOLVER.getHistoryPath()));

    // AIP
    fullBreadcrumb.add(getBreadcrumbItem(aip));

    // Representation
    fullBreadcrumb.add(getBreadcrumbItem(representation));

    if (file != null) {
      // File directory path
      List<String> filePath = file.getPath();
      List<String> fileAncestorsPath = file.getAncestorsPath();

      if (filePath != null && fileAncestorsPath != null && filePath.size() == fileAncestorsPath.size()) {
        for (int i = 0; i < filePath.size(); i++) {
          final String folderName = filePath.get(i);
          final String folderUUID = fileAncestorsPath.get(i);

          SafeHtml breadcrumbLabel = getBreadcrumbLabel(folderName, RodaConstants.VIEW_REPRESENTATION_FOLDER);
          fileBreadcrumb.add(new BreadcrumbItem(breadcrumbLabel, folderName, new Command() {

            @Override
            public void execute() {
              HistoryUtils.resolve(IndexedFile.class.getName(), folderUUID);
            }
          }));
        }
      }

      // File item
      fileBreadcrumb.add(getBreadcrumbItem(file));
    }

    fullBreadcrumb.addAll(fileBreadcrumb);

    // DIP
    fullBreadcrumb.add(getBreadcrumbItem(dip));
    if (dipFile != null) {
      // DIP File ancestors
      if (dipFile.getAncestorsUUIDs() != null) {
        for (DIPFile dipFileAncestor : dipFileAncestors) {
          fullBreadcrumb.add(getBreadcrumbItem(dipFileAncestor));
        }
      }
      // DIP File
      fullBreadcrumb.add(getBreadcrumbItem(dipFile));
    }
    return fullBreadcrumb;
  }

  public static List<BreadcrumbItem> getTransferredResourceBreadcrumbs(TransferredResource r) {
    List<BreadcrumbItem> ret = new ArrayList<>();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.transferredResourcesTitle()),
      messages.transferredResourcesTitle(), IngestTransfer.RESOLVER.getHistoryPath()));
    if (r != null) {

      // add parent
      if (r.getParentUUID() != null) {
        List<String> path = new ArrayList<>(IngestTransfer.RESOLVER.getHistoryPath());
        path.add(r.getParentUUID());
        SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getParentId());
        ret.add(new BreadcrumbItem(breadcrumbLabel, r.getParentId(), path));
      }

      // add self
      List<String> path = new ArrayList<>(IngestTransfer.RESOLVER.getHistoryPath());
      path.add(r.getUUID());
      SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getName());
      ret.add(new BreadcrumbItem(breadcrumbLabel, r.getName(), path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getDipBreadcrumbs(IndexedDIP dip, DIPFile dipFile,
    List<DIPFile> dipFileAncestors) {
    List<BreadcrumbItem> ret = new ArrayList<>();

    // Catalogue
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.allCollectionsTitle()),
      messages.allCollectionsTitle(), BrowseTop.RESOLVER.getHistoryPath()));

    // DIP
    ret.add(getBreadcrumbItem(dip));

    if (dipFile != null) {
      // DIP File ancestors
      if (dipFileAncestors != null) {
        for (DIPFile dipFileAncestor : dipFileAncestors) {
          ret.add(getBreadcrumbItem(dipFileAncestor));
        }
      }

      // DIP File
      ret.add(getBreadcrumbItem(dipFile));
    }

    return ret;
  }

  private static BreadcrumbItem getBreadcrumbItem(final IndexedDIP dip) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    // TODO get icon from config
    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-play-circle-o'></i>"));
    b.append(SafeHtmlUtils.fromString(dip.getTitle()));
    SafeHtml label = b.toSafeHtml();

    return new BreadcrumbItem(label, dip.getTitle(), new Command() {

      @Override
      public void execute() {
        HistoryUtils.openBrowse(dip);
      }
    });
  }

  private static BreadcrumbItem getBreadcrumbItem(final DIPFile dipFile) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    // TODO get icon from config
    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-play-circle'></i>"));
    b.append(SafeHtmlUtils.fromString(dipFile.getId()));
    SafeHtml label = b.toSafeHtml();

    return new BreadcrumbItem(label, dipFile.getId(), new Command() {

      @Override
      public void execute() {
        HistoryUtils.openBrowse(dipFile);
      }
    });
  }

  private static SafeHtml getBreadcrumbLabel(String label, String level) {
    SafeHtml elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(level, false);
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(label));
    return builder.toSafeHtml();
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
    return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), PreservationEvents.BROWSE_RESOLVER.getHistoryToken(),
      id);
  }

  private static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), id);
  }
}
