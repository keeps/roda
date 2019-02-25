/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.portal.BrowseAIPPortal;
import org.roda.wui.client.browse.BrowseDIP;
import org.roda.wui.client.browse.BrowseFile;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ShowPreservationEvent;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.ingest.process.ShowJobReport;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.client.management.EditGroup;
import org.roda.wui.client.management.EditUser;
import org.roda.wui.client.management.ShowLogEntry;
import org.roda.wui.client.management.ShowNotification;
import org.roda.wui.client.planning.ShowPreservationAgent;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.client.planning.ShowRisk;
import org.roda.wui.client.planning.ShowRiskIncidence;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Useful methods
 * 
 * @author Luis Faria
 */
public class HistoryUtils {
  public static final String HISTORY_SEP = "/";
  public static final String HISTORY_SEP_REGEX = "/";
  public static final String HISTORY_SEP_ESCAPE = "%2F";
  public static final String HISTORY_PERMISSION_SEP = ".";

  private static boolean USING_PORTAL_UI = false;

  public static void initEndpoint(boolean usingPortalUI) {
    HistoryUtils.USING_PORTAL_UI = usingPortalUI;
  }

  private HistoryUtils() {
    // do nothing
  }

  public static <T> List<T> tail(List<T> list) {
    return ListUtils.tail(list);
  }

  public static <T> List<T> removeLast(List<T> list) {
    return list.subList(0, list.size() - 1);

  }

  /**
   * Split history string to history path using HISTORY_SEP as the separator
   * 
   * @param history
   * @return the history path
   */
  public static List<String> splitHistory(String history) {
    List<String> historyPath;
    if (history.indexOf(HISTORY_SEP) == -1) {
      historyPath = Arrays.asList(history);
    } else {
      historyPath = Arrays.asList(history.split(HISTORY_SEP_REGEX));
    }
    return historyPath;
  }

  public static List<String> decodeList(List<String> splitted) {
    List<String> tokens = new ArrayList<>();
    for (String item : splitted) {
      tokens.add(URL.decodeQueryString(item));
    }
    return tokens;
  }

  public static List<String> getCurrentHistoryPath() {
    String hash = Window.Location.getHash();
    if (hash.length() > 0) {
      hash = hash.substring(1);
    }
    return decodeList(Arrays.asList(hash.split(HISTORY_SEP_REGEX)));
  }

  public static String createHistoryToken(List<String> tokens) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (String token : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(HISTORY_SEP);
      }

      String encodedToken = URL.encodeQueryString(token);
      builder.append(encodedToken);
    }
    return builder.toString();
  }

  public static void newHistory(List<String> path) {
    String hash = createHistoryToken(path);
    Window.Location.assign("#" + hash);
  }

  public static void replaceHistory(List<String> path) {
    String hash = createHistoryToken(path);
    Window.Location.replace("#" + hash);
  }

  public static void newHistory(HistoryResolver resolver) {
    newHistory(resolver.getHistoryPath());
  }

  public static void newHistory(HistoryResolver resolver, String... extrapath) {
    List<String> path = ListUtils.concat(resolver.getHistoryPath(), extrapath);
    newHistory(path);
  }

  public static void newHistory(HistoryResolver resolver, List<String> extrapath) {
    List<String> path = ListUtils.concat(resolver.getHistoryPath(), extrapath);
    newHistory(path);
  }

  public static String createHistoryHashLink(List<String> path) {
    String hash = createHistoryToken(path);
    return "#" + hash;
  }

  public static String createHistoryHashLink(HistoryResolver resolver, String... extrapath) {
    return createHistoryHashLink(getHistory(resolver, extrapath));
  }

  public static String createHistoryHashLink(HistoryResolver resolver, List<String> extrapath) {
    return createHistoryHashLink(getHistory(resolver, extrapath));
  }

  public static List<String> getHistoryBrowse(String aipId) {
    return getHistory(BrowseTop.RESOLVER, aipId);
  }

  public static List<String> getHistoryBrowseDIP(String dipId) {
    return getHistory(BrowseDIP.RESOLVER, dipId);
  }

  public static List<String> getHistoryBrowseDIPFile(String dipId, String dipFileUUID) {
    return getHistory(BrowseDIP.RESOLVER, dipId, dipFileUUID);
  }

  public static List<String> getHistoryBrowse(DIPFile dipFile) {
    return getHistoryBrowseDIPFile(dipFile.getDipId(), dipFile.getUUID());
  }

  public static void openBrowse(IndexedDIP dip) {
    HistoryUtils.newHistory(getHistoryBrowseDIP(dip.getId()));
  }

  public static void openBrowse(DIPFile dipFile) {
    HistoryUtils.newHistory(getHistoryBrowseDIPFile(dipFile.getDipId(), dipFile.getUUID()));
  }

  public static void openBrowseDIP(String dipId) {
    HistoryUtils.newHistory(getHistoryBrowseDIP(dipId));
  }

  public static void openBrowse(String aipId) {
    HistoryUtils.newHistory(getHistoryBrowse(aipId));
  }

  public static void openBrowse(IndexedAIP aip) {
    openBrowse(aip.getId());
  }

  public static List<String> getHistoryBrowse(String aipId, String representationId) {
    return getHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
  }

  public static List<String> getHistoryBrowse(IndexedRepresentation representation) {
    return getHistoryBrowse(representation.getAipId(), representation.getId());
  }

  public static void openBrowse(String aipId, String representationId) {
    HistoryUtils.newHistory(getHistoryBrowse(aipId, representationId));
  }

  public static void openBrowse(IndexedRepresentation representation) {
    openBrowse(representation.getAipId(), representation.getId());
  }

  public static List<String> getHistoryBrowse(String aipId, String representationId, List<String> filePath,
    String fileId) {
    List<String> tokens = new ArrayList<>();
    tokens.add(aipId);
    tokens.add(representationId);
    tokens.addAll(filePath);
    tokens.add(fileId);
    return getHistory(BrowseFile.RESOLVER, tokens);
  }

  public static List<String> getHistoryBrowse(IndexedFile file) {
    return getHistoryBrowse(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public static void openBrowse(String aipId, String representationId, List<String> filePath, String fileId) {
    HistoryUtils.newHistory(getHistoryBrowse(aipId, representationId, filePath, fileId));
  }

  public static void openBrowse(IndexedFile file) {
    openBrowse(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public static List<String> getHistoryUpload(IndexedFile folder) {
    List<String> tokens = new ArrayList<>();
    tokens.add(folder.getAipId());
    tokens.add(folder.getRepresentationId());
    tokens.addAll(folder.getPath());
    tokens.add(folder.getId());
    return getHistory(TransferUpload.BROWSE_RESOLVER, tokens);
  }

  public static void openUpload(String aipId, String representationId) {
    HistoryUtils.newHistory(getHistoryUpload(aipId, representationId));
  }

  public static void openUpload(IndexedRepresentation representation) {
    HistoryUtils.newHistory(getHistoryUpload(representation));
  }

  public static List<String> getHistoryUpload(IndexedRepresentation representation) {
    return getHistory(TransferUpload.BROWSE_RESOLVER, representation.getAipId(), representation.getId());
  }

  public static List<String> getHistoryUpload(String aipId, String representationId) {
    return getHistory(TransferUpload.BROWSE_RESOLVER, aipId, representationId);
  }

  public static void openUpload(IndexedFile folder) {
    if (folder.isDirectory()) {
      HistoryUtils.newHistory(getHistoryUpload(folder));
    }
  }

  public static List<String> getHistory(List<String> resolverPath, String... extraPath) {
    return ListUtils.concat(resolverPath, extraPath);
  }

  public static List<String> getHistory(List<String> resolverPath, List<String> extraPath) {
    return ListUtils.concat(resolverPath, extraPath);
  }

  public static List<String> getHistory(HistoryResolver resolver, String... extrapath) {
    return getHistory(resolver.getHistoryPath(), extrapath);
  }

  public static List<String> getHistory(HistoryResolver resolver, List<String> extrapath) {
    return getHistory(resolver.getHistoryPath(), extrapath);
  }

  public static String getSearchHistoryByRepresentationInformationFilter(List<String> filters, String searchType) {
    List<String> history = new ArrayList<>();
    history.addAll(Search.RESOLVER.getHistoryPath());
    history.add("@" + searchType);
    history.add(RodaConstants.OPERATOR_OR);

    for (String filter : filters) {
      String[] splittedFilter = filter
        .split(RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR);
      history.add(splittedFilter[1]);
      history.add(splittedFilter[2]);
    }

    return createHistoryHashLink(history);
  }

  public static void resolve(final String objectClass, final String objectUUID) {
    resolve(objectClass, objectUUID, false);
  }

  public static <T extends IsIndexed> void resolve(final String objectClass, final String objectUUID,
    final boolean replace) {
    BrowserService.Util.getInstance().retrieveFromModel(objectClass, objectUUID, new AsyncCallback<T>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(T object) {
        resolve(object, replace);
      }
    });
  }

  public static <T extends IsIndexed> void resolve(T object) {
    resolve(object, false);
  }

  public static <T extends IsIndexed> void resolve(T object, boolean replace) {
    List<String> path = null;

    if (object instanceof IndexedAIP) {
      IndexedAIP aip = (IndexedAIP) object;
      if (USING_PORTAL_UI) {
        path = getHistory(BrowseAIPPortal.RESOLVER, aip.getId());
      } else {
        path = HistoryUtils.getHistoryBrowse(aip.getId());
      }
    } else if (object instanceof IndexedRepresentation) {
      IndexedRepresentation representation = (IndexedRepresentation) object;
      path = HistoryUtils.getHistoryBrowse(representation);
    } else if (object instanceof IndexedFile) {
      IndexedFile file = (IndexedFile) object;
      path = HistoryUtils.getHistoryBrowse(file);
    } else if (object instanceof IndexedDIP) {
      IndexedDIP dip = (IndexedDIP) object;
      path = HistoryUtils.getHistoryBrowseDIP(dip.getId());
    } else if (object instanceof DIPFile) {
      DIPFile dipFile = (DIPFile) object;
      path = HistoryUtils.getHistoryBrowseDIPFile(dipFile.getDipId(), dipFile.getUUID());
    } else if (object instanceof TransferredResource) {
      TransferredResource resource = (TransferredResource) object;
      path = HistoryUtils.getHistory(IngestTransfer.RESOLVER.getHistoryPath(), resource.getUUID());
    } else if (object instanceof IndexedRisk) {
      IndexedRisk risk = (IndexedRisk) object;
      path = HistoryUtils.getHistory(ShowRisk.RESOLVER.getHistoryPath(), risk.getUUID());
    } else if (object instanceof RepresentationInformation) {
      RepresentationInformation ri = (RepresentationInformation) object;
      path = HistoryUtils.getHistory(ShowRepresentationInformation.RESOLVER.getHistoryPath(), ri.getUUID());
    } else if (object instanceof Notification) {
      Notification notification = (Notification) object;
      path = HistoryUtils.getHistory(ShowNotification.RESOLVER.getHistoryPath(), notification.getUUID());
    } else if (object instanceof RiskIncidence) {
      RiskIncidence incidence = (RiskIncidence) object;
      path = HistoryUtils.getHistory(ShowRiskIncidence.RESOLVER.getHistoryPath(), incidence.getUUID());
    } else if (object instanceof Job) {
      Job job = (Job) object;
      path = HistoryUtils.getHistory(ShowJob.RESOLVER.getHistoryPath(), job.getUUID());
    } else if (object instanceof IndexedReport) {
      IndexedReport report = (IndexedReport) object;
      path = HistoryUtils.getHistory(ShowJobReport.RESOLVER.getHistoryPath(), report.getUUID());
    } else if (object instanceof RODAMember) {
      RODAMember member = (RODAMember) object;
      HistoryUtils.newHistory(member.isUser() ? EditUser.RESOLVER : EditGroup.RESOLVER, member.getId());
    } else if (object instanceof IndexedPreservationAgent) {
      IndexedPreservationAgent agent = (IndexedPreservationAgent) object;
      HistoryUtils.newHistory(ShowPreservationAgent.RESOLVER, agent.getId());
    } else if (object instanceof IndexedPreservationEvent) {
      IndexedPreservationEvent preservationEvent = (IndexedPreservationEvent) object;

      String eventId = preservationEvent.getId();
      String aipUUID = preservationEvent.getAipID();
      String representationUUID = preservationEvent.getRepresentationUUID();
      String fileUUID = preservationEvent.getFileUUID();

      if (StringUtils.isNotBlank(fileUUID)) {
        path = HistoryUtils.getHistory(ShowPreservationEvent.RESOLVER.getHistoryPath(), aipUUID, representationUUID,
          fileUUID, eventId);
      } else if (StringUtils.isNotBlank(representationUUID)) {
        path = HistoryUtils.getHistory(ShowPreservationEvent.RESOLVER.getHistoryPath(), aipUUID, representationUUID,
          eventId);
      } else if (StringUtils.isNotBlank(aipUUID)) {
        path = HistoryUtils.getHistory(ShowPreservationEvent.RESOLVER.getHistoryPath(), aipUUID, eventId);
      } else {
        path = HistoryUtils.getHistory(ShowPreservationEvent.RESOLVER.getHistoryPath(), eventId);
      }
    } else if (object instanceof LogEntry) {
      LogEntry logEntry = (LogEntry) object;
      HistoryUtils.newHistory(ShowLogEntry.RESOLVER, logEntry.getUUID());
    } else {
      Toast.showError("Resolve of class not supported: " + object.getClass().getName());
    }

    if (path != null) {
      if (replace) {
        HistoryUtils.replaceHistory(path);
      } else {
        HistoryUtils.newHistory(path);
      }
    }
  }

  public static final HistoryResolver UUID_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        String objectClass = historyTokens.get(0);
        String objectUUID = historyTokens.get(1);
        HistoryUtils.resolve(objectClass, objectUUID, true);
      }
      callback.onSuccess(null);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(true);
    }

    @Override
    public String getHistoryToken() {
      return "uuid";
    }

    @Override
    public List<String> getHistoryPath() {
      return new ArrayList<>();
    }
  };

  public static List<String> getHistoryUuidResolver(String objectClass, String objectUUID) {
    return Arrays.asList(UUID_RESOLVER.getHistoryToken(), objectClass, objectUUID);
  }

}
