package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
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
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.roda.common.RodaConstants;
import org.roda.index.IndexActionException;
import org.roda.index.IndexService;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;
import org.w3c.util.DateParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.BrowserServiceMessages;
import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DisseminationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.PreservationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.RepresentationInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo.HotZone;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.TimelineInfo.Phase;

/**
 * Browser Service Implementation
 *
 * @author Luis Faria
 *
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static final String FONDLIST_PAGESIZE = "10";
	static final private Logger logger = Logger.getLogger(BrowserServiceImpl.class);

	private Path basePath;
	private Path indexPath;
	private StorageService storage;
	private ModelService model;
	private IndexService index;

	/**
	 * Create a new BrowserService Implementation instance
	 *
	 */
	public BrowserServiceImpl() {
		try {
			basePath = Files.createTempDirectory("modelTests");

			indexPath = Files.createTempDirectory("indexTests");
			storage = new FileStorageService(basePath);
			model = new ModelService(storage);

			// Configure Solr
			URL solrConfigURL = getClass().getResource("/index/solr.xml");
			Path solrConfigPath = Paths.get(solrConfigURL.toURI());
			Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
			Path aipSchema = indexPath.resolve("aip");
			Files.createDirectories(aipSchema);
			Files.createFile(aipSchema.resolve("core.properties"));

			Path solrHome = Paths.get(getClass().getResource("/index/").toURI());
			System.setProperty("solr.data.dir", indexPath.toString());
			System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
			System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
			System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());

			// start embedded solr
			final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

			index = new IndexService(solr, model);

			// Copy test corpora

			String RODA_HOME;
			if (System.getProperty("roda.home") != null) {
				RODA_HOME = System.getProperty("roda.home");
			} else if (System.getenv("RODA_HOME") != null) {
				RODA_HOME = System.getenv("RODA_HOME");
			} else {
				RODA_HOME = null;
			}

			Path corporaPath = Paths.get(RODA_HOME, "data2");
			StorageService corporaService = new FileStorageService(corporaPath);
			model.createAIP("AIP_1", corporaService, DefaultStoragePath.parse("AIP", "AIP_1"));
			model.createAIP("AIP_2", corporaService, DefaultStoragePath.parse("AIP", "AIP_2"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Filter addFondsRestrictions(Filter filter) {
		if (filter == null) {
			filter = new Filter();
		}
		filter.add(SimpleDescriptionObject.FONDS_FILTER.getParameters());
		return filter;
	}

	protected Filter addParentRestrictions(Filter filter, String parentId) {
		if (filter == null) {
			filter = new Filter();
		}
		filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, parentId));
		return filter;
	}

	public Integer getCollectionsCount(Filter filter) throws RODAException {
		Long count;
		try {
			count = index.countDescriptiveMetadata(addFondsRestrictions(filter));
			logger.info(String.format("getCollectionsCount(%1$s)=%2$s", filter, count));
		} catch (IndexActionException e) {
			logger.error("Error counting collections", e);
			throw new GenericException("Error counting collections " + e.getMessage());
		}
		return count.intValue();
	}

	public SimpleDescriptionObject[] getCollections(Filter filter, Sorter sorter, Sublist sublist)
			throws RODAException {
		IndexResult<SimpleDescriptionObject> sdos;
		try {
			sdos = index.findDescriptiveMetadata(addFondsRestrictions(filter), sorter, sublist);
			logger.info(String.format("getCollections(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, sdos));
		} catch (IndexActionException e) {
			logger.debug("Error getting collections", e);
			throw new GenericException("Error getting collections " + e.getMessage());
		}

		// TODO change return type to index result
		return sdos.getResults().toArray(new SimpleDescriptionObject[] {});
	}

	public IndexResult<SimpleDescriptionObject> findCollections(Filter filter, Sorter sorter, Sublist sublist)
			throws RODAException {
		IndexResult<SimpleDescriptionObject> sdos;
		try {
			sdos = index.findDescriptiveMetadata(addFondsRestrictions(filter), sorter, sublist);
			logger.info(String.format("findCollections(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, sdos));
		} catch (IndexActionException e) {
			logger.debug("Error getting collections", e);
			throw new GenericException("Error getting collections " + e.getMessage());
		}

		return sdos;
	}

	public Integer getSubElementsCount(String pid, Filter filter) throws RODAException {
		Long count;
		try {
			count = index.countDescriptiveMetadata(addParentRestrictions(filter, pid));
		} catch (IndexActionException e) {
			logger.debug("Error getting sub-elements count", e);
			throw new GenericException("Error getting sub-elements count " + e.getMessage());
		}

		return new Integer(count.intValue());
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
	public SimpleDescriptionObject[] getSubElements(String pid, Filter filter, Sorter sorter, Sublist sublist)
			throws RODAException {
		IndexResult<SimpleDescriptionObject> sdos;
		try {
			sdos = index.findDescriptiveMetadata(addParentRestrictions(filter, pid), sorter, sublist);
		} catch (IndexActionException e) {
			logger.debug("Error getting collections", e);
			throw new GenericException("Error getting collections " + e.getMessage());
		}

		// TODO change return type to index result
		return sdos.getResults().toArray(new SimpleDescriptionObject[] {});
	}

	// public RODAObject getRODAObject(String pid) throws RODAException {
	// RODAObject ret;
	// try {
	// Browser browser =
	// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
	// .getBrowserService();
	// ret = browser.getRODAObject(pid);
	//
	// } catch (RemoteException e) {
	// logger.debug("Error getting RODA object", e);
	// throw RODAClient.parseRemoteException(e);
	// }
	// return ret;
	// }

	public SimpleDescriptionObject getSimpleDescriptionObject(String pid) throws RODAException {
		try {
			return index.retrieveDescriptiveMetadata(pid);
		} catch (IndexActionException e) {
			logger.error("Error getting SDO", e);
			throw new GenericException("Error getting SDO: " + e.getMessage());
		}
	}

	public DescriptionObject getDescriptionObject(String pid) throws RODAException {
		DescriptionObject ret;
		try {
			Browser browser = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getBrowserService();
			ret = browser.getDescriptionObject(pid);

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return ret;
	}

	public String getParent(String pid) throws RODAException {
		try {
			return index.getParent(pid);
		} catch (IndexActionException e) {
			logger.error("Error getting parent", e);
			throw new GenericException("Error getting parent: " + e.getMessage());
		}
	}

	public String[] getAncestors(String pid) throws RODAException {
		try {
			return index.getAncestors(pid).toArray(new String[] {});
		} catch (IndexActionException e) {
			logger.error("Error getting parent", e);
			throw new GenericException("Error getting parent: " + e.getMessage());
		}
	}

	public List<RepresentationInfo> getRepresentationsInfo(String doPID) throws RODAException {
		List<RepresentationInfo> ret = new Vector<RepresentationInfo>();
		try {
			Browser browserService = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getBrowserService();
			RepresentationObject[] representations = browserService.getDORepresentations(doPID);
			representations = representations != null ? representations : new RepresentationObject[] {};
			for (RepresentationObject rep : representations) {
				// Set<String> st = new HashSet<String>(Arrays.asList(rep
				// .getStatuses()));
				// if (st.contains(RepresentationObject.STATUS_NORMALIZED)
				// || st.contains(RepresentationObject.STATUS_ORIGINAL)) {
				ret.add(new RepresentationInfo(rep, getDisseminations(rep), rep.getSubType(),
						rep.getPartFiles().length + 1, getSize(rep)));
				// }
			}

			Collections.sort(ret, new Comparator<RepresentationInfo>() {
				public int compare(RepresentationInfo arg0, RepresentationInfo arg1) {
					int score0;
					int score1;
					score0 = (arg0.isNormalized() ? 1 : 0) + (arg0.isOriginal() ? 2 : 0);
					score1 = (arg1.isNormalized() ? 1 : 0) + (arg1.isOriginal() ? 2 : 0);

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

			String[] typeDisseminators = parseDisseminatorList(properties.getProperty("content-model." + repType));
			disseminators.addAll(Arrays.asList(typeDisseminators));

			if (repSubType != null) {
				String[] subTypeDisseminators = parseDisseminatorList(
						properties.getProperty("content-model." + repType + "." + repSubType));
				disseminators.addAll(Arrays.asList(subTypeDisseminators));
			}

			ret = new ArrayList<DisseminationInfo>(disseminators.size());
			for (String disseminator : disseminators) {
				DisseminationInfo info = new DisseminationInfo();
				info.setId(disseminator);
				info.setUrl(properties.getProperty("disseminator." + disseminator + ".url"));
				info.setWindowName(properties.getProperty("disseminator." + disseminator + ".window.name"));
				info.setWindowFeatures(properties.getProperty("disseminator." + disseminator + ".window.features"));
				info.setIconURL(properties.getProperty("disseminator." + disseminator + ".icon"));
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

	public List<RepresentationPreservationObject> getDOPreservationObjects(String doPID) throws RODAException {
		List<RepresentationPreservationObject> ret;
		try {
			Browser browserService = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getBrowserService();
			RepresentationPreservationObject[] rpos = browserService.getDOPreservationObjects(doPID);
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

	public List<PreservationInfo> getPreservationsInfo(String doPID) throws RODAException {
		List<PreservationInfo> ret = new ArrayList<PreservationInfo>();
		Browser browserService = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
				.getBrowserService();
		for (RepresentationPreservationObject rpo : getDOPreservationObjects(doPID)) {
			try {
				boolean normalized;
				boolean original;
				if (rpo.getRepresentationObjectPID() != null) {
					try {

						SimpleRepresentationObject sro = browserService
								.getSimpleRepresentationObject(rpo.getRepresentationObjectPID());
						Set<String> status = new HashSet<String>(Arrays.asList(sro.getStatuses()));
						normalized = status.contains(RepresentationObject.STATUS_NORMALIZED);
						original = status.contains(RepresentationObject.STATUS_ORIGINAL);
					} catch (NoSuchObjectException e) {
						normalized = false;
						original = false;
					}
				} else {
					normalized = false;
					original = false;
				}

				PreservationInfo info = new PreservationInfo(rpo, normalized, original);
				ret.add(info);

			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}

		}

		return ret;
	}

	public TimelineInfo getPreservationTimeline(List<String> rpoPIDs, List<String> icons, List<String> colors,
			String localeString) throws RODAException {
		final Locale locale = ServerTools.parseLocale(localeString);
		TimelineInfo timelineInfo = new TimelineInfo();
		timelineInfo.setHotZones(new ArrayList<HotZone>());
		timelineInfo.setPhases(new ArrayList<Phase>());

		Browser browserService = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
				.getBrowserService();

		String eventXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		eventXML += "<data date-time-format=\"iso8601\">\n";
		for (int i = 0; i < rpoPIDs.size(); i++) {
			String rpoPID = rpoPIDs.get(i);
			String icon = icons.get(i);
			String color = colors.get(i);
			try {

				logger.debug("Getting PREMIS Events of " + rpoPID);
				EventPreservationObject[] pEvents = browserService.getPreservationEvents(rpoPID);
				if (pEvents == null) {
					pEvents = new EventPreservationObject[] {};
				}
				logger.debug("Got " + pEvents.length + " PREMIS Events of " + rpoPID);

				eventXML += createTimelineXML(pEvents, icon, color, locale);

			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}
		}

		eventXML += "</data>";

		timelineInfo.setEventsXML(eventXML);
		timelineInfo.setDate(DateParser.getIsoDate(new Date()));

		logger.debug("Timeline events XML: " + eventXML);
		logger.debug("Timeline date: " + timelineInfo.getDate());

		return timelineInfo;

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
