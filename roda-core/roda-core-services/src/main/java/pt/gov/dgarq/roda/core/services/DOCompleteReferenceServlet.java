package pt.gov.dgarq.roda.core.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.BrowserHelper;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;

/**
 * This servlet serves complete references for Description Objects.
 */
public class DOCompleteReferenceServlet extends RODAServlet {
	private static final long serialVersionUID = 4766070377638424295L;

	private static final Logger logger = Logger
			.getLogger(DOCompleteReferenceServlet.class);

	private HttpServletRequest currentRequest = null;

	private String fedoraURL = null;
	private String fedoraGSearchURL = null;
	private String adminUsername = null;
	private String adminPassword = null;

	private BrowserHelper browserHelper = null;

	/**
	 * @throws RODAServiceException
	 * @see HttpServlet#HttpServlet()
	 */
	public DOCompleteReferenceServlet() throws RODAServiceException {
		super();

		fedoraURL = getConfiguration().getString("fedoraURL");
		fedoraGSearchURL = getConfiguration().getString("fedoraGSearchURL");

		adminUsername = getConfiguration().getString("adminUsername");
		adminPassword = getConfiguration().getString("adminPassword");

		logger.debug(getClass().getSimpleName() + " initialised ok");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		this.currentRequest = request;

		if (StringUtils.isBlank(request.getPathInfo())) {

			logger.debug("Empty HTTP GET. Sending BAD_REQUEST");

			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			response.flushBuffer();

		} else {

			// Remove first '/' from pathInfo
			String pathInfo = request.getPathInfo().substring(1);

			String[] pidAndDatastreamID = pathInfo.split("/");
			String pid = pidAndDatastreamID[0];

			if (pidAndDatastreamID.length == 1) {

				logger.debug("HTTP GET " + pathInfo);

				try {

					if (this.browserHelper == null) {
						User adminUser = new Login().getAuthenticatedUser(
								adminUsername, adminPassword);

						FedoraClientUtility fedoraClient = new FedoraClientUtility(
								fedoraURL, fedoraGSearchURL, adminUser,
								adminPassword);
						this.browserHelper = new BrowserHelper(fedoraClient,
								getConfiguration());
					}

					String completeReference = this.browserHelper
							.getDOCompleteReference(pid);

					logger.debug("Getting SimpleDescriptionObject with PID "
							+ pid);

					response.setContentType("text/xml");
					response.getWriter().write(
							"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
					response.getWriter().write(
							"<completeReference>" + completeReference
									+ "</completeReference>\n");
					response.flushBuffer();

				} catch (FedoraClientException e) {
					throw new ServletException(
							"Exception creating Fedora client - "
									+ e.getMessage(), e);
				} catch (BrowserException e) {
					throw new ServletException(
							"Exception getting complete reference - "
									+ e.getMessage(), e);
				} catch (NoSuchRODAObjectException e) {
					throw new ServletException(
							"Exception getting complete reference - "
									+ e.getMessage(), e);
				} catch (LoginException e) {
					throw new ServletException(
							"Exception getting complete reference - "
									+ e.getMessage(), e);
				} catch (RODAServiceException e) {
					throw new ServletException(
							"Exception getting complete reference - "
									+ e.getMessage(), e);
				}

			}

		}

	}

	@Override
	protected HttpServletRequest getCurrentRequest() {
		return this.currentRequest;
	}

}