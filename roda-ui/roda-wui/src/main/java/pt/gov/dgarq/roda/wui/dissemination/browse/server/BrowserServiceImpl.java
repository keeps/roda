package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.roda.api.controllers.Browser;
import org.roda.common.UserUtility;
import org.roda.model.ValidationException;
import org.roda.storage.Binary;
import org.roda.storage.DefaultBinary;
import org.roda.storage.StoragePath;
import org.roda.storage.StringContentPayload;
import org.w3c.util.DateParser;
import org.xml.sax.SAXParseException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.BrowserServiceMessages;
import pt.gov.dgarq.roda.common.I18nUtility;
import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.NotFoundException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DescriptiveMetadataEditBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DisseminationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.MetadataParseException;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.ParseError;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.PreservationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.RepresentationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo;

/**
 * Browser Service Implementation
 *
 * @author Luis Faria
 *
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

  private static final long serialVersionUID = 1L;
  static final String FONDLIST_PAGESIZE = "10";
  private static final Logger LOGGER = Logger.getLogger(BrowserServiceImpl.class);

  /**
   * Create a new BrowserService Implementation instance
   *
   */
  public BrowserServiceImpl() {

  }

  @Override
  public BrowseItemBundle getItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.getItemBundle(user, aipId, locale);
  }

  @Override
  public DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId, String descId)
    throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getDescriptiveMetadataEditBundle(user, aipId, descId);
  }

  @Override
  public IndexResult<SimpleDescriptionObject> findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString) throws RODAException {
    try {
      RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
      IndexResult<SimpleDescriptionObject> result = Browser.findDescriptiveMetadata(user, filter, sorter, sublist,
        facets);
      return I18nUtility.translate(result, SimpleDescriptionObject.class, localeString);
    } catch (RODAException e) {
      throw e;
    } catch (Throwable e) {
      LOGGER.error("Unexpected error", e);
      throw new GenericException(e.getMessage());
    }
  }

  public Long countDescriptiveMetadata(Filter filter) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.countDescriptiveMetadata(user, filter);
  }

  public SimpleDescriptionObject getSimpleDescriptionObject(String pid) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getSimpleDescriptionObject(user, pid);
  }

  public List<SimpleDescriptionObject> getAncestors(SimpleDescriptionObject sdo) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getAncestors(user, sdo);
  }

  @Override
  public SimpleDescriptionObject moveInHierarchy(String aipId, String parentId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.moveInHierarchy(user, aipId, parentId);
  }

  @Override
  public String createAIP(String parentId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.createAIP(user, parentId).getId();

  }

  @Override
  public void removeAIP(String aipId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeAIP(user, aipId);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, MetadataParseException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());

    String descriptiveMetadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();

    StoragePath storagePath = null;
    Map<String, Set<String>> metadata = new HashMap<>();
    StringContentPayload payload = new StringContentPayload(bundle.getXml());
    Long sizeInBytes = Long.valueOf(bundle.getXml().getBytes().length);
    boolean reference = false;
    Map<String, String> contentDigest = new HashMap<>();
    Binary descriptiveMetadataIdBinary = new DefaultBinary(storagePath, metadata, payload, sizeInBytes, reference,
      contentDigest);

    try {
      Browser.createDescriptiveMetadataFile(user, aipId, descriptiveMetadataId, descriptiveMetadataType,
        descriptiveMetadataIdBinary);
    } catch (ValidationException e) {
      throw convertValidationException(e);
    }
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, MetadataParseException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    String descriptiveMetadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();

    StoragePath storagePath = null;
    Map<String, Set<String>> metadata = new HashMap<>();
    StringContentPayload payload = new StringContentPayload(bundle.getXml());
    Long sizeInBytes = Long.valueOf(bundle.getXml().getBytes().length);
    boolean reference = false;
    Map<String, String> contentDigest = new HashMap<>();
    Binary descriptiveMetadataIdBinary = new DefaultBinary(storagePath, metadata, payload, sizeInBytes, reference,
      contentDigest);

    try {
      Browser.updateDescriptiveMetadataFile(user, aipId, descriptiveMetadataId, descriptiveMetadataType,
        descriptiveMetadataIdBinary);
    } catch (ValidationException e) {
      throw convertValidationException(e);
    }
  }

  private MetadataParseException convertValidationException(ValidationException e) {
    MetadataParseException ex = new MetadataParseException(e.getMessage());
    List<SAXParseException> errors = e.getErrors();
    List<ParseError> mappedList = new ArrayList<>();

    if (errors != null) {
      for (SAXParseException saxParseException : errors) {
        ParseError parseError = new ParseError();
        parseError.setMessage(saxParseException.getMessage());
        parseError.setLineNumber(saxParseException.getLineNumber());
        parseError.setColumnNumber(saxParseException.getColumnNumber());
        parseError.setPublicId(saxParseException.getPublicId());
        parseError.setSystemId(saxParseException.getSystemId());
        mappedList.add(parseError);
      }
    }
    ex.setErrors(mappedList);

    return ex;
  }

  public void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeDescriptiveMetadataFile(user, itemId, descriptiveMetadataId);
  }

  // public DescriptiveMetadata retrieveMetadataFile(String itemId, String
  // descriptiveMetadataId) throws RODAException {
  // RodaUser user = UserUtility.getUser(getThreadLocalRequest(),
  // RodaCoreFactory.getIndexService());
  // return Browser.retrieveMetadataFile(user, itemId, descriptiveMetadataId);
  // }

  public DescriptionObject getDescriptionObject(String pid) throws RODAException {
    // DescriptionObject ret;
    // try {
    // Browser browser =
    // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
    // .getBrowserService();
    // ret = browser.getDescriptionObject(pid);
    //
    // } catch (RemoteException e) {
    // LOGGER.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;
  }

  public List<RepresentationInfo> getRepresentationsInfo(String doPID) throws RODAException {
    // List<RepresentationInfo> ret = new Vector<RepresentationInfo>();
    // try {
    // Browser browserService =
    // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
    // .getBrowserService();
    // RepresentationObject[] representations =
    // browserService.getDORepresentations(doPID);
    // representations = representations != null ? representations : new
    // RepresentationObject[] {};
    // for (RepresentationObject rep : representations) {
    // // Set<String> st = new HashSet<String>(Arrays.asList(rep
    // // .getStatuses()));
    // // if (st.contains(RepresentationObject.STATUS_NORMALIZED)
    // // || st.contains(RepresentationObject.STATUS_ORIGINAL)) {
    // ret.add(new RepresentationInfo(rep, getDisseminations(rep),
    // rep.getSubType(),
    // rep.getPartFiles().length + 1, getSize(rep)));
    // // }
    // }
    //
    // Collections.sort(ret, new Comparator<RepresentationInfo>() {
    // public int compare(RepresentationInfo arg0, RepresentationInfo arg1)
    // {
    // int score0;
    // int score1;
    // score0 = (arg0.isNormalized() ? 1 : 0) + (arg0.isOriginal() ? 2 : 0);
    // score1 = (arg1.isNormalized() ? 1 : 0) + (arg1.isOriginal() ? 2 : 0);
    //
    // return score1 - score0;
    // }
    // });
    //
    // } catch (RemoteException e) {
    // LOGGER.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }

    return null;
  }

  // private String[] parseDisseminatorList(String disseminatorList) {
  // String[] ret;
  // if (disseminatorList == null) {
  // ret = new String[] {};
  // } else {
  // ret = disseminatorList.split(", ");
  // }
  // return ret;
  // }

  protected List<DisseminationInfo> getDisseminations(RepresentationObject rep) {
    // List<DisseminationInfo> ret;
    // if (rep != null) {
    // LOGGER.info("REP content-model: " + rep.getContentModel());
    //
    // Properties properties = RodaClientFactory.getRodaProperties();
    // String repType = rep.getType();
    // String repSubType = rep.getSubType();
    //
    // List<String> disseminators = new Vector<String>();
    //
    // String[] typeDisseminators =
    // parseDisseminatorList(properties.getProperty("content-model." +
    // repType));
    // disseminators.addAll(Arrays.asList(typeDisseminators));
    //
    // if (repSubType != null) {
    // String[] subTypeDisseminators = parseDisseminatorList(
    // properties.getProperty("content-model." + repType + "." +
    // repSubType));
    // disseminators.addAll(Arrays.asList(subTypeDisseminators));
    // }
    //
    // ret = new ArrayList<DisseminationInfo>(disseminators.size());
    // for (String disseminator : disseminators) {
    // DisseminationInfo info = new DisseminationInfo();
    // info.setId(disseminator);
    // info.setUrl(properties.getProperty("disseminator." + disseminator +
    // ".url"));
    // info.setWindowName(properties.getProperty("disseminator." +
    // disseminator + ".window.name"));
    // info.setWindowFeatures(properties.getProperty("disseminator." +
    // disseminator + ".window.features"));
    // info.setIconURL(properties.getProperty("disseminator." + disseminator
    // + ".icon"));
    // ret.add(info);
    // }
    //
    // } else {
    // ret = new ArrayList<DisseminationInfo>();
    // }
    return null;
  }

  // private long getSize(RepresentationObject rep) {
  // long size = 0;
  //
  // size += rep.getRootFile().getSize();
  //
  // for (RepresentationFile file : rep.getPartFiles()) {
  // size += file.getSize();
  // }
  // return size;
  // }

  public List<RepresentationPreservationObject> getDOPreservationObjects(String doPID) throws RODAException {
    // List<RepresentationPreservationObject> ret;
    // try {
    // Browser browserService =
    // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
    // .getBrowserService();
    // RepresentationPreservationObject[] rpos =
    // browserService.getDOPreservationObjects(doPID);
    // if (rpos != null) {
    // ret = Arrays.asList(rpos);
    // } else {
    // ret = new ArrayList<RepresentationPreservationObject>();
    // }
    // } catch (RemoteException e) {
    // LOGGER.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;

  }

  public List<PreservationInfo> getPreservationsInfo(String doPID) throws RODAException {
    // List<PreservationInfo> ret = new ArrayList<PreservationInfo>();
    // Browser browserService =
    // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
    // .getBrowserService();
    // for (RepresentationPreservationObject rpo :
    // getDOPreservationObjects(doPID)) {
    // try {
    // boolean normalized;
    // boolean original;
    // if (rpo.getRepresentationObjectPID() != null) {
    // try {
    //
    // SimpleRepresentationObject sro = browserService
    // .getSimpleRepresentationObject(rpo.getRepresentationObjectPID());
    // Set<String> status = new
    // HashSet<String>(Arrays.asList(sro.getStatuses()));
    // normalized = status.contains(RepresentationObject.STATUS_NORMALIZED);
    // original = status.contains(RepresentationObject.STATUS_ORIGINAL);
    // } catch (NoSuchObjectException e) {
    // normalized = false;
    // original = false;
    // }
    // } else {
    // normalized = false;
    // original = false;
    // }
    //
    // PreservationInfo info = new PreservationInfo(rpo, normalized,
    // original);
    // ret.add(info);
    //
    // } catch (RemoteException e) {
    // LOGGER.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    //
    // }

    return null;
  }

  public TimelineInfo getPreservationTimeline(List<String> rpoPIDs, List<String> icons, List<String> colors,
    String localeString) throws RODAException {
    // final Locale locale = ServerTools.parseLocale(localeString);
    // TimelineInfo timelineInfo = new TimelineInfo();
    // timelineInfo.setHotZones(new ArrayList<HotZone>());
    // timelineInfo.setPhases(new ArrayList<Phase>());
    //
    // Browser browserService =
    // RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
    // .getBrowserService();
    //
    // String eventXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    // eventXML += "<data date-time-format=\"iso8601\">\n";
    // for (int i = 0; i < rpoPIDs.size(); i++) {
    // String rpoPID = rpoPIDs.get(i);
    // String icon = icons.get(i);
    // String color = colors.get(i);
    // try {
    //
    // LOGGER.debug("Getting PREMIS Events of " + rpoPID);
    // EventPreservationObject[] pEvents =
    // browserService.getPreservationEvents(rpoPID);
    // if (pEvents == null) {
    // pEvents = new EventPreservationObject[] {};
    // }
    // LOGGER.debug("Got " + pEvents.length + " PREMIS Events of " +
    // rpoPID);
    //
    // eventXML += createTimelineXML(pEvents, icon, color, locale);
    //
    // } catch (RemoteException e) {
    // LOGGER.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    // }
    //
    // eventXML += "</data>";
    //
    // timelineInfo.setEventsXML(eventXML);
    // timelineInfo.setDate(DateParser.getIsoDate(new Date()));
    //
    // LOGGER.debug("Timeline events XML: " + eventXML);
    // LOGGER.debug("Timeline date: " + timelineInfo.getDate());

    return null;

  }

  protected String createTimelineXML(EventPreservationObject[] pEvents, String icon, String color, Locale locale) {

    BrowserServiceMessages browserServiceMessages = new BrowserServiceMessages(locale);

    String eventXML = "";
    for (EventPreservationObject pEvent : pEvents) {
      String title = escapeXML(pEvent.getEventType());

      String content = "<p style=\"text-align:left;\">";
      if (!StringUtils.isBlank(pEvent.getEventDetail())) {
        content += "<strong>" + browserServiceMessages.getString("description") + ": </strong>"
          + pEvent.getEventDetail() + "<br/>";
      }
      if (!StringUtils.isBlank(pEvent.getOutcome())) {
        content += "<strong>" + browserServiceMessages.getString("result") + ": </strong>" + pEvent.getOutcome()
          + "<br/>";
      }
      if (!StringUtils.isBlank(pEvent.getOutcomeDetailNote())) {
        content += "<strong>" + pEvent.getOutcomeDetailNote() + ": </strong>";
      }
      if (!StringUtils.isBlank(pEvent.getOutcomeDetailExtension())) {
        content += truncate(pEvent.getOutcomeDetailExtension(), 200);
      }
      content += "</p>";

      content = escapeXML(content);

      eventXML += "<event";
      eventXML += " start=\"" + DateParser.getIsoDate(pEvent.getDatetime()) + "\"";
      // eventXML += " end=\"" + DateParser.getIsoDate(end) + "\"";
      eventXML += " isDuration=\"false\"";
      eventXML += " title=\"" + title + "\"";
      eventXML += " icon=\"" + icon + "\"";
      eventXML += " color=\"" + color + "\"";
      eventXML += ">\n";
      eventXML += content + "\n";
      eventXML += "</event>\n";
    }

    return eventXML;
  }

  protected String truncate(String message, int size) {
    String ret;
    if (message.length() > size) {
      ret = message.substring(0, size - 5) + "(...)";
    } else {
      ret = message;
    }
    return ret;
  }

  protected String escapeXML(String xml) {
    return StringEscapeUtils.escapeXml(xml);
  }

}
