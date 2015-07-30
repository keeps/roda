package pt.gov.dgarq.roda.wui.ingest.list.server;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.w3c.util.DateParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.IngestListAcceptMessages;
import config.i18n.server.IngestListRejectMessages;
import config.i18n.server.IngestListReportMessages;
import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SIPStateTransition;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.services.AcceptSIP;
import pt.gov.dgarq.roda.core.services.IngestMonitor;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;
import pt.gov.dgarq.roda.wui.common.client.tools.PIDTranslator;
import pt.gov.dgarq.roda.wui.common.server.ReportContentSource;
import pt.gov.dgarq.roda.wui.common.server.ReportDownload;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.common.server.VelocityMail;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestListService;

/**
 * Ingest list service implementation
 * 
 * @author Luis Faria
 * 
 */
public class IngestListServiceImpl extends RemoteServiceServlet implements IngestListService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(IngestListServiceImpl.class);

	private IngestMonitor ingestMonitor;
	private AcceptSIP acceptSIP;

	public IngestListServiceImpl() {
		super();

		try {
			this.ingestMonitor = new IngestMonitor();
			this.acceptSIP = new AcceptSIP();
		} catch (IngestRegistryException e) {
			logger.error("Error initializing ingest manager", e);
		} catch (RODAServiceException e) {
			logger.error("Error initializing ingest manager", e);
		}
	}

	public SIPState getSipState(String sipId) throws RODAException {
		SIPState ret = null;
		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("id", sipId));
		Sublist sublist = new Sublist(0, 1);
		SIPState[] states;
		states = ingestMonitor.getSIPs(new ContentAdapter(filter, null, sublist));
		if (states != null && states.length > 0) {
			ret = states[0];
		}

		return ret;
	}

	public int getSIPCount(Filter filter) throws RODAException {
		return getSIPCount(getThreadLocalRequest().getSession(), filter);
	}

	private int getSIPCount(HttpSession session, Filter filter) throws RODAException {
		int count;
		count = ingestMonitor.getSIPsCount(filter);
		return count;
	}

	private SIPState[] getSIPs(ContentAdapter adapter) throws RODAException {
		SIPState[] list;
		list = ingestMonitor.getSIPs(adapter);
		if (list == null) {
			list = new SIPState[] {};
		}

		return list;
	}

	public IndexResult<SIPState> getSIPs(Filter filter, Sorter sorter, Sublist sublist) throws RODAException {
		ContentAdapter adapter = new ContentAdapter(filter, sorter, sublist);

		SIPState[] list = ingestMonitor.getSIPs(adapter);
		IndexResult<SIPState> ret = new IndexResult<SIPState>();
		ret.setResults(Arrays.asList(list));
		ret.setOffset(sublist.getFirstElementIndex());
		ret.setLimit(sublist.getMaximumElementCount());
		ret.setTotalCount(getSIPCount(filter));

		logger.info("getting sips " + ret);
		System.out.println("!!! " + ret);
		return ret;
	}

	public void acceptSIP(String sipId, String message) throws RODAException {
		acceptSIP.acceptSIP(sipId, true, message);
	}

	public void rejectSIP(String sipId, String message, boolean notifyProducer) throws RODAException {
		SIPState sip = acceptSIP.acceptSIP(sipId, false, message);
		if (notifyProducer) {
			sendNotifyProducerEmail(sip, message);
		}
	}

	private boolean sendNotifyProducerEmail(SIPState sip, String message) throws RODAException {
		boolean success = false;

		String email = null;

		try {
			User producer = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserBrowserService().getUser(sip.getUsername());

			email = producer.getEmail();

			Map<String, String> contextMap = new HashMap<String, String>();
			contextMap.put("sipId", sip.getId());
			contextMap.put("sipOriginalFilename", sip.getOriginalFilename());
			contextMap.put("sipDateTime", DateParser.getIsoDate(sip.getDatetime()));
			contextMap.put("producerName", producer.getName());
			contextMap.put("producerFullName", StringEscapeUtils.escapeHtml(producer.getFullName()));
			contextMap.put("producerEmail", producer.getEmail());
			contextMap.put("message", StringEscapeUtils.escapeHtml(message));

			VelocityMail vmail = VelocityMail.getDefaultInstance();
			InternetAddress address = new InternetAddress(email);
			vmail.send("notifyproducer", address, new VelocityContext(contextMap));
			success = true;
		} catch (AddressException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error notifying producer email '" + email + "' about sip " + sip, caught);
			throw new GenericException(caught.getMessage());
		} catch (UserManagementException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error notifying producer email '" + email + "' about sip " + sip, caught);
		} catch (RemoteException e) {
			throw RODAClient.parseRemoteException(e);
		} catch (Exception e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error notifying producer email '" + email + "' about sip " + sip, caught);
			throw new GenericException(caught.getMessage());
		}

		return success;
	}

	public void setSIPPublished(String sipId, boolean published, String message) throws RODAException {
		try {
			RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession()).getAcceptSIPService()
					.acceptSIP(sipId, published, message);
		} catch (RemoteException e) {
			logger.debug("Error setting SIP published", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

	public void setSIPListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException {
		final Locale locale = ServerTools.parseLocale(localeString);
		final IngestListReportMessages messages = new IngestListReportMessages(locale);

		ReportDownload.getInstance().createPDFReport(getThreadLocalRequest().getSession(),
				new ReportContentSource<SIPState>() {

					public int getCount(HttpSession session, Filter filter) throws Exception {
						return getSIPCount(session, filter);
					}

					public SIPState[] getElements(HttpSession session, ContentAdapter adapter) throws Exception {
						return getSIPs(adapter);
					}

					public Map<String, String> getElementFields(HttpServletRequest req, SIPState sip) {
						return IngestListServiceImpl.this.getElementFields(req, sip, messages);
					}

					public String getElementId(SIPState sip) {
						return String.format(messages.getString("sip.title"), sip.getId());

					}

					public String getReportTitle() {
						return messages.getString("report.title");
					}

					public String getFieldNameTranslation(String name) {
						String translation;
						try {
							translation = messages.getString("sip.label." + name);
						} catch (MissingResourceException e) {
							translation = name;
						}

						return translation;
					}

					public String getFieldValueTranslation(String value) {
						String translation;
						try {
							translation = messages.getString("sip.value." + value);
						} catch (MissingResourceException e) {
							translation = value;
						}

						return translation;
					}

				}, adapter);
	}

	protected Map<String, String> getElementFields(HttpServletRequest req, SIPState sip,
			IngestListReportMessages messages) {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		ret.put(messages.getString("sip.label.originalFilename"), sip.getOriginalFilename());
		Date date = sip.getStateTransitions()[0].getDatetime();
		ret.put(messages.getString("sip.label.startDate"), DateParser.getIsoDate(date));
		String state;
		try {
			state = messages.getString("sip.value." + sip.getState());
		} catch (MissingResourceException e) {
			state = sip.getState();
		}

		ret.put(messages.getString("sip.label.state"), state);
		if (sip.getState().equals("QUARANTINE")) {
			SIPStateTransition[] transitions = sip.getStateTransitions();

			ret.put(messages.getString("sip.label.failureReason"),
					transitions[transitions.length - 1].getDescription());
		}

		ret.put(messages.getString("sip.label.percentage"),
				String.format(messages.getString("sip.value.percentage"), sip.getCompletePercentage()));

		if (sip.getIngestedPID() != null) {
			ret.put(messages.getString("sip.label.pid"), sip.getIngestedPID());
			ret.put(messages.getString("sip.label.link"), RodaClientFactory.getServletUrl(req)
					+ "/#dissemination.browse." + PIDTranslator.translatePID(sip.getIngestedPID()));
		}
		ret.put(messages.getString("sip.label.producer"), sip.getUsername());
		return ret;
	}

	public Map<String, String> getAcceptMessageTemplates(String localeString) {
		final Locale locale = ServerTools.parseLocale(localeString);
		final IngestListAcceptMessages messages = new IngestListAcceptMessages(locale);
		return messages.getMessages();
	}

	public Map<String, String> getRejectMessageTemplates(String localeString) {
		final Locale locale = ServerTools.parseLocale(localeString);
		final IngestListRejectMessages messages = new IngestListRejectMessages(locale);
		return messages.getMessages();
	}

}
