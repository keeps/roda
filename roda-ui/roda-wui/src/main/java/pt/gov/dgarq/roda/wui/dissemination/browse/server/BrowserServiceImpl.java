package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DisseminationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.PreservationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.RepresentationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo.HotZone;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo.Phase;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.BrowserServiceMessages;

/**
 * Browser Service Implementation
 * 
 * @author Luis Faria
 * 
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements
		BrowserService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final String FONDLIST_PAGESIZE = "10";
	static final private Logger logger = Logger
			.getLogger(BrowserServiceImpl.class);

	/**
	 * Create a new BrowserService Implementation instance
	 * 
	 */
	public BrowserServiceImpl() {

	}

	protected Filter addFondsRestrictions(Filter filter) {
		if (filter == null) {
			filter = new Filter();
		}
		filter.add(SimpleDescriptionObject.FONDS_FILTER.getParameters());
		return filter;
	}

	public Integer getCollectionsCount(Filter filter) throws RODAException {
		int ret;
		Browser browser = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getBrowserService();
		try {
			ret = browser
					.getSimpleDescriptionObjectCount(addFondsRestrictions(filter));
		} catch (RemoteException e) {
			logger.debug("Error getting collections count", e);
			throw RODAClient.parseRemoteException(e);
		}

		return new Integer(ret);
	}

	public SimpleDescriptionObject[] getCollections(ContentAdapter adapter)
			throws RODAException {

		SimpleDescriptionObject[] fonds;
		// Get all fonds
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			adapter.setFilter(addFondsRestrictions(adapter.getFilter()));
			fonds = browser.getSimpleDescriptionObjects(adapter);
		} catch (RemoteException e) {
			logger.debug("Error getting collections", e);
			throw RODAClient.parseRemoteException(e);
		} catch (BrowserException e) {
			logger.debug("Error getting collections", e);
			throw e;
		}

		return fonds;
	}

	public Integer getSubElementsCount(String pid, Filter filter)
			throws RODAException {
		int ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			if (filter == null) {
				filter = new Filter();
			}
			filter.add(new SimpleFilterParameter("parentPID", pid));
			ret = browser.getSimpleDescriptionObjectCount(filter);
		} catch (RemoteException e) {
			logger.debug("Error getting sub elements count", e);
			throw RODAClient.parseRemoteException(e);
		}
		return new Integer(ret);
	}

	/**
	 * Get <code>count</code> the children of current node starting at
	 * <code>index</code>. A node explicits its children in the relationship
	 * metadata (RDF) by the roda:parent-of relationship.
	 * 
	 * @param pid
	 *            parent pid value
	 * @param firstitem
	 *            first item to show
	 * @param itemsPerPage
	 *            number of items per page.
	 * @throws RODAException
	 */
	public SimpleDescriptionObject[] getSubElements(String pid,
			ContentAdapter adapter) throws RODAException {
		SimpleDescriptionObject[] ret;

		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			if (adapter.getFilter() == null) {
				adapter.setFilter(new Filter());
			}
			adapter.getFilter()
					.add(new SimpleFilterParameter("parentPID", pid));
			ret = browser.getSimpleDescriptionObjects(adapter);

		} catch (RemoteException e) {
			logger.debug("Error getting sub elements if " + pid, e);
			throw RODAClient.parseRemoteException(e);
		} catch (BrowserException e) {
			logger.debug("Error getting sub elements of " + pid, e);
			throw e;
		}

		return ret;
	}

	public RODAObject getRODAObject(String pid) throws RODAException {
		RODAObject ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ret = browser.getRODAObject(pid);

		} catch (RemoteException e) {
			logger.debug("Error getting RODA object", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public SimpleDescriptionObject getSimpleDescriptionObject(String pid)
			throws RODAException {
		SimpleDescriptionObject ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ret = browser.getSimpleDescriptionObject(pid);

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public DescriptionObject getDescriptionObject(String pid)
			throws RODAException {
		DescriptionObject ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ret = browser.getDescriptionObject(pid);

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public String getParent(String pid) throws RODAException {
		String ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			SimpleDescriptionObject sdo = browser
					.getSimpleDescriptionObject(pid);
			ret = sdo.getParentPID();
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public String[] getAncestors(String pid) throws RODAException {
		String[] ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ret = browser.getDOAncestorPIDs(pid);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public Integer getCollectionIndex(String collectionPID, Filter filter,
			Sorter sorter) throws RODAException {
		int ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ContentAdapter cAdapter = new ContentAdapter(
					addFondsRestrictions(filter), sorter, null);
			ret = browser.getSimpleDescriptionObjectIndex(collectionPID,
					cAdapter);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return new Integer(ret);
	}

	public Integer getItemIndex(String parentPID, String childPID,
			Filter filter, Sorter sorter) throws RODAException {
		int ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			if (filter == null) {
				filter = new Filter();
			}
			filter.add(new SimpleFilterParameter("parentPID", parentPID));
			ContentAdapter cAdapter = new ContentAdapter(filter, sorter, null);
			ret = browser.getSimpleDescriptionObjectIndex(childPID, cAdapter);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return new Integer(ret);
	}

	public SimpleDescriptionObject[] getSubElements(String pid,
			String focusOnChild, int count, Filter filter, Sorter sorter)
			throws RODAException {
		int index = getItemIndex(pid, focusOnChild, filter, sorter);
		ContentAdapter cAdapter = new ContentAdapter(filter, sorter,
				new Sublist(index, count));

		return getSubElements(pid, cAdapter);
	}

	public List<RepresentationInfo> getRepresentationsInfo(String doPID)
			throws RODAException {
		List<RepresentationInfo> ret = new Vector<RepresentationInfo>();
		try {
			Browser browserService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			RepresentationObject[] representations = browserService
					.getDORepresentations(doPID);
			representations = representations != null ? representations
					: new RepresentationObject[] {};
			for (RepresentationObject rep : representations) {
//				Set<String> st = new HashSet<String>(Arrays.asList(rep
//						.getStatuses()));
//				if (st.contains(RepresentationObject.STATUS_NORMALIZED)
//						|| st.contains(RepresentationObject.STATUS_ORIGINAL)) {
					ret.add(new RepresentationInfo(rep, getDisseminations(rep),
							rep.getSubType(), rep.getPartFiles().length + 1,
							getSize(rep)));
//				}
			}

			Collections.sort(ret, new Comparator<RepresentationInfo>() {

				public int compare(RepresentationInfo arg0,
						RepresentationInfo arg1) {
					int score0;
					int score1;
					score0 = (arg0.isNormalized() ? 1 : 0)
							+ (arg0.isOriginal() ? 2 : 0);
					score1 = (arg1.isNormalized() ? 1 : 0)
							+ (arg1.isOriginal() ? 2 : 0);

					return score1 - score0;
				}

			});

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		return ret;
	}

	private String[] parseDisseminatorList(String disseminatorList) {
		String[] ret;
		if (disseminatorList == null) {
			ret = new String[] {};
		} else {
			ret = disseminatorList.split(", ");
		}
		return ret;
	}

	protected List<DisseminationInfo> getDisseminations(RepresentationObject rep) {
		List<DisseminationInfo> ret;
		if (rep != null) {
			logger.info("REP content-model: " + rep.getContentModel());

			Properties properties = RodaClientFactory.getRodaProperties();
			String repType = rep.getType();
			String repSubType = rep.getSubType();

			List<String> disseminators = new Vector<String>();

			String[] typeDisseminators = parseDisseminatorList(properties
					.getProperty("content-model." + repType));
			disseminators.addAll(Arrays.asList(typeDisseminators));

			if (repSubType != null) {
				String[] subTypeDisseminators = parseDisseminatorList(properties
						.getProperty("content-model." + repType + "."
								+ repSubType));
				disseminators.addAll(Arrays.asList(subTypeDisseminators));
			}

			ret = new ArrayList<DisseminationInfo>(disseminators.size());
			for (String disseminator : disseminators) {
				DisseminationInfo info = new DisseminationInfo();
				info.setId(disseminator);
				info.setUrl(properties.getProperty("disseminator."
						+ disseminator + ".url"));
				info.setWindowName(properties.getProperty("disseminator."
						+ disseminator + ".window.name"));
				info.setWindowFeatures(properties.getProperty("disseminator."
						+ disseminator + ".window.features"));
				info.setIconURL(properties.getProperty("disseminator."
						+ disseminator + ".icon"));
				ret.add(info);
			}

		} else {
			ret = new ArrayList<DisseminationInfo>();
		}
		return ret;
	}

	private long getSize(RepresentationObject rep) {
		long size = 0;

		size += rep.getRootFile().getSize();

		for (RepresentationFile file : rep.getPartFiles()) {
			size += file.getSize();
		}
		return size;
	}

	public List<RepresentationPreservationObject> getDOPreservationObjects(
			String doPID) throws RODAException {
		List<RepresentationPreservationObject> ret;
		try {
			Browser browserService = RodaClientFactory.getRodaClient(
					this.getThreadLocalRequest().getSession())
					.getBrowserService();
			RepresentationPreservationObject[] rpos = browserService
					.getDOPreservationObjects(doPID);
			if (rpos != null) {
				ret = Arrays.asList(rpos);
			} else {
				ret = new ArrayList<RepresentationPreservationObject>();
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;

	}

	public List<PreservationInfo> getPreservationsInfo(String doPID)
			throws RODAException {
		List<PreservationInfo> ret = new ArrayList<PreservationInfo>();
		Browser browserService = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getBrowserService();
		for (RepresentationPreservationObject rpo : getDOPreservationObjects(doPID)) {
			try {
				boolean normalized;
				boolean original;
				if (rpo.getRepresentationObjectPID() != null) {
					try {

						SimpleRepresentationObject sro = browserService
								.getSimpleRepresentationObject(rpo
										.getRepresentationObjectPID());
						Set<String> status = new HashSet<String>(Arrays
								.asList(sro.getStatuses()));
						normalized = status
								.contains(RepresentationObject.STATUS_NORMALIZED);
						original = status
								.contains(RepresentationObject.STATUS_ORIGINAL);
					} catch (NoSuchObjectException e) {
						normalized = false;
						original = false;
					}
				} else {
					normalized = false;
					original = false;
				}

				PreservationInfo info = new PreservationInfo(rpo, normalized,
						original);
				ret.add(info);

			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}

		}

		return ret;
	}

	// private static final DateFormat dateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd hh:ss");
	private static final DateFormat timelineDateFormat = new SimpleDateFormat(
			"MMM dd yyyy HH:mm:ss 'GMT'");

	public TimelineInfo getPreservationTimeline(List<String> rpoPIDs,
			List<String> icons, List<String> colors, String localeString)
			throws RODAException {
		final Locale locale = ServerTools.parseLocale(localeString);
		TimelineInfo timelineInfo = new TimelineInfo();
		timelineInfo.setHotZones(new ArrayList<HotZone>());
		timelineInfo.setPhases(new ArrayList<Phase>());

		Browser browserService = RodaClientFactory.getRodaClient(
				this.getThreadLocalRequest().getSession()).getBrowserService();

		String eventXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		eventXML += "<data date-time-format=\"iso8601\">\n";
		for (int i = 0; i < rpoPIDs.size(); i++) {
			String rpoPID = rpoPIDs.get(i);
			String icon = icons.get(i);
			String color = colors.get(i);
			try {

				logger.debug("Getting PREMIS Events of " + rpoPID);
				EventPreservationObject[] pEvents = browserService
						.getPreservationEvents(rpoPID);
				if (pEvents == null) {
					pEvents = new EventPreservationObject[] {};
				}
				logger.debug("Got " + pEvents.length + " PREMIS Events of "
						+ rpoPID);

				eventXML += createTimelineXML(pEvents, icon, color, locale);

			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}
		}

		eventXML += "</data>";

		timelineInfo.setEventsXML(eventXML);
		timelineInfo.setDate(timelineDateFormat.format(new Date()));

		logger.debug("Timeline events XML: " + eventXML);
		logger.debug("Timeline date: " + timelineInfo.getDate());

		return timelineInfo;

	}

	protected String createTimelineXML(EventPreservationObject[] pEvents,
			String icon, String color, Locale locale) {

		BrowserServiceMessages browserServiceMessages = new BrowserServiceMessages(
				locale);

		String eventXML = "";
		for (EventPreservationObject pEvent : pEvents) {
			String title = escapeXML(pEvent.getEventType());

			String content = "<p style=\"text-align:left;\">";
			if (!StringUtils.isBlank(pEvent.getEventDetail())) {
				content += "<strong>"
						+ browserServiceMessages.getString("description")
						+ ": </strong>" + pEvent.getEventDetail() + "<br/>";
			}
			if (!StringUtils.isBlank(pEvent.getOutcome())) {
				content += "<strong>"
						+ browserServiceMessages.getString("result")
						+ ": </strong>" + pEvent.getOutcome() + "<br/>";
			}
			if (!StringUtils.isBlank(pEvent.getOutcomeDetailNote())) {
				content += "<strong>" + pEvent.getOutcomeDetailNote()
						+ ": </strong>";
			}
			if (!StringUtils.isBlank(pEvent.getOutcomeDetailExtension())) {
				content += truncate(pEvent.getOutcomeDetailExtension(), 200);
			}
			content += "</p>";

			content = escapeXML(content);

			eventXML += "<event";
			eventXML += " start=\""
					+ DateParser.getIsoDate(pEvent.getDatetime()) + "\"";
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
