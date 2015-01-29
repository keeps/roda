package pt.gov.dgarq.roda.core.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import fedora.client.HttpInputStream;
import fedora.server.types.gen.DatastreamDef;

/**
 * This servlet serves files from RODA objects.
 */
public class FileAccessServlet extends RODAServlet {
	private static final long serialVersionUID = 8250998813262549879L;

	private static final Logger logger = Logger
			.getLogger(FileAccessServlet.class);

	private HttpServletRequest currentRequest = null;

	private String fedoraURL = null;
	private String fedoraGSearchURL = null;

	/**
	 * @throws RODAServiceException
	 * @see HttpServlet#HttpServlet()
	 */
	public FileAccessServlet() throws RODAServiceException {
		super();

		fedoraURL = getConfiguration().getString("fedoraURL"); //$NON-NLS-1$
		fedoraGSearchURL = getConfiguration().getString("fedoraGSearchURL"); //$NON-NLS-1$
		logger.debug(getClass().getSimpleName() + " initialised ok"); //$NON-NLS-1$
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Date start = new Date();

		this.currentRequest = request;
		response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

		if (StringUtils.isBlank(request.getPathInfo())) {

			logger.debug("Empty HTTP GET. Sending BAD_REQUEST"); //$NON-NLS-1$

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, request
					.getPathInfo());
			response.flushBuffer();

		} else {

			// Remove first '/' from pathInfo
			String pathInfo = request.getPathInfo().substring(1);

			String[] pidAndDatastreamID = pathInfo.split("/"); //$NON-NLS-1$

			if (pidAndDatastreamID.length == 2) {

				String pid = pidAndDatastreamID[0];
				String dsID = pidAndDatastreamID[1];

				logger.debug("HTTP GET " + pathInfo); //$NON-NLS-1$

				CASUserPrincipal clientUser = getClientUser();

				if (clientUser == null) {
					throw new ServletException(
							"User credentials are not available."); //$NON-NLS-1$
				} else {

					FedoraClientUtility fedoraClient;
					try {
						fedoraClient = new FedoraClientUtility(fedoraURL,fedoraGSearchURL, clientUser, getCasUtility());

					} catch (FedoraClientException e) {
						throw new ServletException(
								"Exception creating Fedora client - " //$NON-NLS-1$
										+ e.getMessage(), e);
					}

					// Look for the requested datastream
					DatastreamDef datastreamDef = null;

					DatastreamDef[] datastreamDefs = fedoraClient.getAPIA()
							.listDatastreams(pid, null);
					for (DatastreamDef dsDef : datastreamDefs) {

						if (dsDef.getID().equals(dsID)) {

							datastreamDef = dsDef;
							break;
						}
					}

					if (datastreamDef == null) {

						logger.debug("The requested datastream " + dsID //$NON-NLS-1$
								+ " doesn't exist. Sending SC_NOT_FOUND"); //$NON-NLS-1$

						response.sendError(HttpServletResponse.SC_NOT_FOUND,
								pathInfo);
						response.flushBuffer();

					} else {

						// Datastream exists

						// By default the filename of a datastream is the ID
						String dsFilename;

						if (StringUtils.isBlank(datastreamDef.getLabel())) {
							dsFilename = dsID;
						} else {
							dsFilename = datastreamDef.getLabel();
						}

						String datastreamURI = fedoraClient.getDatastreamURI(
								pid, dsID);

						logger.debug("Requesting Fedora datastream with URI " //$NON-NLS-1$
								+ datastreamURI);

						HttpInputStream httpInputStream = fedoraClient.get(
								datastreamURI, true);

						response.setContentType(httpInputStream
								.getContentType());

						// Set the content disposition
						response.setHeader("Content-disposition", "filename=\"" //$NON-NLS-1$ //$NON-NLS-2$
								+ dsFilename + "\""); //$NON-NLS-1$

						// Add filename encoded as UTF-8 in parameter named FILENAME
						// This is usefull to RODA-WUI, that cannot decode the filename
						// correctly.
						response.setHeader("FILENAME", URLEncoder.encode( //$NON-NLS-1$
								dsFilename, "UTF-8")); //$NON-NLS-1$

						logger.debug("Setting Content-disposition=" //$NON-NLS-1$
								+ "filename=\"" + dsFilename + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						try {

							response.setContentLength(httpInputStream
									.getContentLength());

						} catch (Throwable t) {

							logger.warn("Exception getting content-length - " //$NON-NLS-1$
									+ t.getMessage() + " - IGNORING"); //$NON-NLS-1$

							// response.setContentLength(0);
						}

						logger.info("Serving datastream " + pid + "/" + dsID); //$NON-NLS-1$ //$NON-NLS-2$

						IOUtils.copyLarge(httpInputStream, response
								.getOutputStream());

						response.flushBuffer();

						long duration = new Date().getTime() - start.getTime();
						registerAction("FileAccessServlet.GET", new String[] { //$NON-NLS-1$
								"pid", pid, "id", dsID }, //$NON-NLS-1$ //$NON-NLS-2$
								"User %username% called method FileAccessServlet.GET(" //$NON-NLS-1$
										+ pid + ", " + dsID + ")", duration); //$NON-NLS-1$ //$NON-NLS-2$

						logger.info("Served datastream " + pid + "/" + dsID); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

			} else {

				logger.debug("HTTP GET " + pathInfo + ". Sending BAD_REQUEST"); //$NON-NLS-1$ //$NON-NLS-2$

				response
						.sendError(HttpServletResponse.SC_BAD_REQUEST, pathInfo);
				response.flushBuffer();
			}

		}

	}

	@Override
	protected HttpServletRequest getCurrentRequest() {
		return this.currentRequest;
	}

}