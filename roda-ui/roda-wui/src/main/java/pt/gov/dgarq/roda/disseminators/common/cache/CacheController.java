package pt.gov.dgarq.roda.disseminators.common.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * Cache controlling common code
 * 
 * @author Luis Faria
 */
public abstract class CacheController {

	private static final Logger logger = Logger
			.getLogger(CacheController.class);

	private final String disseminatorName;
	private final String disseminatorURL;
	private final Set<String> waitingForResources;
	private final Map<String, Exception> errorCreatingResources;

	/**
	 * Create a new cache controller
	 * 
	 * @param disseminatorName
	 *            the name of the disseminator, to use internally in cache
	 * @param disseminatorURL
	 *            the URL used after servlet context to identify the
	 *            disseminator
	 */
	public CacheController(String disseminatorName, String disseminatorURL) {
		this.disseminatorName = disseminatorName;
		this.disseminatorURL = disseminatorURL;

		waitingForResources = new HashSet<String>();
		errorCreatingResources = new HashMap<String, Exception>();
	}

	/**
	 * Create needed resources in cache
	 * 
	 * @param rep
	 *            the representation to disseminate
	 * @param cacheURL
	 *            the cache URL, to use to externally access files in cache
	 * @param cacheFile
	 *            the cache directory, where resources must be created
	 */
	protected abstract void createResources(HttpServletRequest request,
			RepresentationObject rep, String cacheURL, File cacheFile)
			throws Exception;

	/**
	 * After creating resources, if needed send a response to user
	 * 
	 * @param rep
	 *            the representation to disseminate
	 * @param cacheURL
	 *            the cache URL, to use to externally access files in cache
	 * @param response
	 */
	protected abstract void sendResponse(HttpServletRequest request,
			RepresentationObject rep, String cacheURL,
			HttpServletResponse response) throws Exception;

	/**
	 * Get a dissemination of a representation
	 * 
	 * @param pid
	 *            the representation PID
	 * @param request
	 *            the servlet request
	 * @param response
	 *            the servlet response
	 * @throws IOException
	 * @throws RODAClientException
	 * @throws NoSuchRODAObjectException
	 * @throws LoginException
	 * @throws BrowserException
	 */
	public void get(String pid, final HttpServletRequest request,
			HttpServletResponse response) throws IOException, BrowserException,
			LoginException, NoSuchRODAObjectException, RODAClientException {
		String servletUrl = RodaClientFactory.getServletUrl(request);
		final String cacheURL = Cache.getCacheUrl(request);
		if (waitingForResources.contains(pid)) {
			response.sendRedirect(servletUrl + "/pt.gov.dgarq.roda.disseminators.common.loading.Loading/Loading.html#" + servletUrl
					+ "/" + disseminatorURL + "/" + pid + "/");
		} else {
			final RepresentationObject rep = RodaClientFactory.getRodaClient(
					request.getSession()).getBrowserService()
					.getRepresentationObject(pid);
			if (!Cache.isCached(pid, disseminatorName)
					&& !errorCreatingResources.containsKey(pid)) {
				Thread createResourcesThread = new Thread() {
					public void run() {
						waitingForResources.add(rep.getPid());
						File cacheFile = Cache.getCacheFile(rep.getPid(),
								disseminatorName);
						try {
							createResources(request, rep, cacheURL, cacheFile);
						} catch (Exception e) {
							logger.error("Error creating resources", e);
							errorCreatingResources.put(rep.getPid(), e);
							cacheFile.delete();
						}
						waitingForResources.remove(rep.getPid());
					}
				};
				createResourcesThread.start();
				response.sendRedirect(servletUrl + "/pt.gov.dgarq.roda.disseminators.common.loading.Loading/Loading.html#"
						+ servletUrl + "/" + disseminatorURL + "/" + pid + "/");
				RodaClientFactory.log(disseminatorName, false, pid, request);

			} else if (errorCreatingResources.containsKey(pid)) {
				Exception e = errorCreatingResources.get(pid);
				// TODO create a different error for different exception classes
				// e.g. LoginException
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"An error occurred while generating resources. Exception "
								+ e.getClass().getName() + ": "
								+ e.getMessage());
				errorCreatingResources.remove(pid);
			} else {
				try {
					sendResponse(request, rep, cacheURL, response);
					RodaClientFactory.log(disseminatorName, true, pid, request);
				} catch (Exception e) {
					logger.error("Error sending response", e);
					response.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
									.getMessage());
				}
			}
		}
	}
}
