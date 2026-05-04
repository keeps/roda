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
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.disposal.confirmations.CreateDisposalConfirmation;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.client.disposal.hold.CreateDisposalHold;
import org.roda.wui.client.disposal.hold.EditDisposalHold;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.disposal.rule.CreateDisposalRule;
import org.roda.wui.client.disposal.rule.EditDisposalRule;
import org.roda.wui.client.disposal.rule.ShowDisposalRule;
import org.roda.wui.client.disposal.schedule.CreateDisposalSchedule;
import org.roda.wui.client.disposal.schedule.EditDisposalSchedule;
import org.roda.wui.client.management.NotificationRegister;
import org.roda.wui.client.management.ShowLogEntry;
import org.roda.wui.client.management.ShowNotification;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.disposal.DisposalDestroyedRecords;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.management.members.CreateGroup;
import org.roda.wui.client.management.members.CreateUser;
import org.roda.wui.client.management.members.EditGroup;
import org.roda.wui.client.management.members.EditUser;
import org.roda.wui.client.planning.agents.PreservationAgents;
import org.roda.wui.client.planning.RiskRegister;
import org.roda.wui.client.planning.agents.ShowPreservationAgent;
import org.roda.wui.client.planning.ShowRisk;
import org.roda.wui.client.management.members.MemberManagement;
import org.roda.wui.client.management.members.ShowMember;
import org.roda.wui.client.planning.RepresentationInformationNetwork;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
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

  public static List<BreadcrumbItem> getLogEntryBreadcrumbs(LogEntry logEntry) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.activityLogTitle()), messages.activityLogTitle(),
      UserLog.RESOLVER.getHistoryPath()));

    if (logEntry != null) {
      List<String> path = new ArrayList<>(ShowLogEntry.RESOLVER.getHistoryPath());
      path.add(logEntry.getUUID());
      String label = StringUtils.isNotBlank(logEntry.getId()) ? logEntry.getId() : logEntry.getUUID();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getNotificationBreadcrumbs(Notification notification) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.notificationsTitle()),
      messages.notificationsTitle(), NotificationRegister.RESOLVER.getHistoryPath()));

    if (notification != null) {
      List<String> path = new ArrayList<>(ShowNotification.RESOLVER.getHistoryPath());
      path.add(notification.getUUID());
      String label = StringUtils.isNotBlank(notification.getId()) ? notification.getId() : notification.getUUID();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getRepresentationInformationBreadCrumbs(RepresentationInformation ri) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.representationInformationTitle()),
      messages.representationInformationTitle(), RepresentationInformationNetwork.RESOLVER.getHistoryPath()));

    if (ri != null) {
      List<String> path = new ArrayList<>(ShowRepresentationInformation.RESOLVER.getHistoryPath());
      path.add(ri.getUUID());
      String label = ri.getName() != null ? ri.getName() : ri.getId();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getRiskBreadCrumbs(IndexedRisk risk) {
    List<BreadcrumbItem> ret = new ArrayList<>();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.riskRegisterTitle()),
      messages.riskRegisterTitle(), RiskRegister.RESOLVER.getHistoryPath()));

    if (risk != null) {
      List<String> path = new ArrayList<>(ShowRisk.RESOLVER.getHistoryPath());
      String label = StringUtils.isNotBlank(risk.getName()) ? risk.getName() : risk.getId();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
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

  public static List<BreadcrumbItem> getRODAMembersBreadcrumbs() {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.usersAndGroupsTitle()),
      messages.usersAndGroupsTitle(), MemberManagement.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getCreateUserBreadcrumbs() {
    List<BreadcrumbItem> ret = getRODAMembersBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.createUserTitle()), messages.createUserTitle(),
      CreateUser.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getCreateGroupBreadcrumbs() {
    List<BreadcrumbItem> ret = getRODAMembersBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.createGroupTitle()), messages.createGroupTitle(),
      CreateGroup.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getEditMemberBreadcrumbs(RODAMember member) {
    List<BreadcrumbItem> ret = getRODAMemberBreadcrumbs(member);

    if (member.isUser()) {
      List<String> path = new ArrayList<>(EditUser.RESOLVER.getHistoryPath());
      path.add(member.getId());

      ret.add(
        new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.editUserTitle()), messages.editUserTitle(), path));
    } else {
      List<String> path = new ArrayList<>(EditGroup.RESOLVER.getHistoryPath());
      path.add(member.getId());

      ret.add(
        new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.editGroupTitle()), messages.editGroupTitle(), path));
    }
    return ret;
  }

  public static List<BreadcrumbItem> getRODAMemberBreadcrumbs(RODAMember user) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.usersAndGroupsTitle()),
      messages.usersAndGroupsTitle(), MemberManagement.RESOLVER.getHistoryPath()));

    if (user != null) {
      List<String> path = new ArrayList<>(ShowMember.RESOLVER.getHistoryPath());
      path.add(user.getUUID());
      String label = user.getFullName();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getDisposalPolicyBreadcrumbs() {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.disposalPolicyTitle()),
      messages.disposalPolicyTitle(), DisposalPolicy.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getCreateDisposalScheduleBreadcrumbs() {
    List<BreadcrumbItem> ret = getDisposalPolicyBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.newDisposalScheduleTitle()),
      messages.newDisposalScheduleTitle(), CreateDisposalSchedule.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getEditDisposalScheduleBreadcrumbs(DisposalSchedule schedule) {
    List<BreadcrumbItem> ret = getDisposalScheduleBreadcrumbs(schedule);

    List<String> path = new ArrayList<>(EditDisposalSchedule.RESOLVER.getHistoryPath());
    path.add(schedule.getId());

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.editDisposalScheduleTitle()),
      messages.editDisposalScheduleTitle(), path));

    return ret;
  }

  public static List<BreadcrumbItem> getDisposalScheduleBreadcrumbs(DisposalSchedule schedule) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.disposalPolicyTitle()),
      messages.disposalPolicyTitle(), DisposalPolicy.RESOLVER.getHistoryPath()));

    if (schedule != null) {
      StringBuilder b = new StringBuilder();
      b.append("<i class='far fa-calendar'></i>");
      b.append("&nbsp;");
      b.append(schedule.getTitle());
      SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(b.toString());

      List<String> path = new ArrayList<>(ShowDisposalSchedule.RESOLVER.getHistoryPath());
      path.add(schedule.getId());
      String label = schedule.getTitle();
      ret.add(new BreadcrumbItem(safeHtml, label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getCreateDisposalHoldBreadcrumbs() {
    List<BreadcrumbItem> ret = getDisposalPolicyBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.newDisposalHoldTitle()),
      messages.newDisposalHoldTitle(), CreateDisposalHold.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getDisposalHoldBreadcrumbs(DisposalHold hold) {
    List<BreadcrumbItem> ret = getDisposalPolicyBreadcrumbs();

    if (hold != null) {
      StringBuilder b = new StringBuilder();
      b.append("<i class='fas fa-lock'></i>");
      b.append("&nbsp;");
      b.append(hold.getTitle());
      SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(b.toString());

      List<String> path = new ArrayList<>(ShowDisposalHold.RESOLVER.getHistoryPath());
      path.add(hold.getId());
      String label = hold.getTitle();
      ret.add(new BreadcrumbItem(safeHtml, label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getEditDisposalHoldBreadcrumbs(DisposalHold hold) {
    List<BreadcrumbItem> ret = getDisposalHoldBreadcrumbs(hold);

    List<String> path = new ArrayList<>(EditDisposalHold.RESOLVER.getHistoryPath());
    path.add(hold.getId());

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.editDisposalHoldTitle()),
      messages.editDisposalHoldTitle(), path));

    return ret;
  }

  public static List<BreadcrumbItem> getDisposalRuleBreadcrumbs(DisposalRule rule) {
    List<BreadcrumbItem> ret = getDisposalPolicyBreadcrumbs();

    if (rule != null) {
      StringBuilder b = new StringBuilder();
      b.append("<i class='fas fa-gavel'></i>");
      b.append("&nbsp;");
      b.append(rule.getTitle());
      SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(b.toString());

      List<String> path = new ArrayList<>(ShowDisposalRule.RESOLVER.getHistoryPath());
      path.add(rule.getId());
      String label = rule.getTitle();
      ret.add(new BreadcrumbItem(safeHtml, label, path));
    }

    return ret;
  }

  public static List<BreadcrumbItem> getEditDisposalRuleBreadcrumbs(DisposalRule rule) {
    List<BreadcrumbItem> ret = getDisposalRuleBreadcrumbs(rule);

    List<String> path = new ArrayList<>(EditDisposalRule.RESOLVER.getHistoryPath());
    path.add(rule.getId());

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.editDisposalRuleTitle()),
      messages.editDisposalRuleTitle(), path));

    return ret;
  }

  public static List<BreadcrumbItem> getCreateDisposalRuleBreadcrumbs() {
    List<BreadcrumbItem> ret = getDisposalPolicyBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.newDisposalRuleTitle()),
      messages.newDisposalRuleTitle(), CreateDisposalRule.RESOLVER.getHistoryPath()));

    return ret;
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

  public static List<BreadcrumbItem> getDisposalConfirmationBreadcrumbs() {
    List<BreadcrumbItem> ret = new ArrayList<>();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.disposalConfirmationsTitle()),
      messages.disposalConfirmationsTitle(), DisposalConfirmations.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getDisposalConfirmationBreadcrumbs(DisposalConfirmation confirmation) {
    List<BreadcrumbItem> ret = getDisposalConfirmationBreadcrumbs();

    List<String> path = new ArrayList<>(ShowDisposalConfirmation.RESOLVER.getHistoryPath());
    path.add(confirmation.getId());

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(confirmation.getTitle()), confirmation.getTitle(), path));

    return ret;
  }

  public static List<BreadcrumbItem> getCreateDisposalConfirmationBreadcrumbs() {
    List<BreadcrumbItem> ret = getDisposalConfirmationBreadcrumbs();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.createDisposalConfirmationTitle()),
      messages.createDisposalConfirmationTitle(), CreateDisposalConfirmation.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getPreservationAgentBreadcrumbs() {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(messages.preservationAgentsTitle()),
            messages.preservationAgentsTitle(), PreservationAgents.RESOLVER.getHistoryPath()));

    return ret;
  }

  public static List<BreadcrumbItem> getPreservationAgentBreadcrumbs(IndexedPreservationAgent agent) {
    List<BreadcrumbItem> ret = getPreservationAgentBreadcrumbs();

    if (agent != null) {
      List<String> path = new ArrayList<>(ShowPreservationAgent.RESOLVER.getHistoryPath());
      path.add(agent.getId());
      String label = agent.getName();
      ret.add(new BreadcrumbItem(SafeHtmlUtils.fromString(label), label, path));
    }

    return ret;
  }

  private static List<String> getViewItemEventsHistoryToken(String id) {
    return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), PreservationEvents.BROWSE_RESOLVER.getHistoryToken(),
      id);
  }

  private static List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), id);
  }
}
