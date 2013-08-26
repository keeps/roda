package pt.gov.dgarq.roda.disseminators.AIPDownload;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.disseminators.common.RepresentationHelper;
import pt.gov.dgarq.roda.disseminators.common.tools.ZipTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DisseminationInfo;

/**
 * Servlet implementation class for Servlet: AIPDownload
 *
 * @web.servlet name="AIPDownload" display-name="AIPDownload"
 * description="Download the AIP"
 *
 * @web.servlet-mapping url-pattern="/AIPDownload"
 *
 */
public class AIPDownload extends javax.servlet.http.HttpServlet implements
        javax.servlet.Servlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Set<String> ALWAYS_ZIP = new HashSet<String>(Arrays
            .asList(RodaClientFactory.getRodaProperties().getProperty(
            "roda.disseminators.aipdownload.alwayszip").split(" ")));
    private Logger logger = Logger.getLogger(AIPDownload.class);
    private RepresentationHelper representationHelper = null;

    /**
     * Create a new AIP download servlet
     */
    public AIPDownload() {
        logger.debug("Always zip: " + ALWAYS_ZIP);
    }

    private RepresentationHelper getRepresentationHelper() throws IOException {
        if (representationHelper == null) {
            representationHelper = new RepresentationHelper();
        }
        return representationHelper;
    }

    /*
     * (non-Java-doc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
     * HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /*
     * (non-Java-doc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
     * HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        int separatorIndex = pathInfo.indexOf('/', 2);
        separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo
                .length();
        String pid = pathInfo.substring(1, separatorIndex);
        String icon = request.getParameter("icon");

        logger.info("pid=" + pid);
        HttpSession session = request.getSession();
        try {
            RepresentationObject rep;
            try {
                rep = RodaClientFactory.getRodaClient(request.getSession())
                        .getBrowserService().getRepresentationObject(pid);
            } catch (NoSuchRODAObjectException e) {
                rep = null;
            }

            if (pid != null && icon != null && icon.equals("true")) {
                sendIcon(session, rep, response);
            } else if (pid != null) {
                sendRepresentation(request, rep, response);
            } else {
                logger.error("Request with no PID defined");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "parameter pid must be defined");
            }

        } catch (LoginException e) {
            logger.info("Login Failure", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
                    .getMessage());
        } catch (BrowserException e) {
            logger.error("Browser Exception", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                    .getMessage());
        } catch (RODAClientException e) {
            logger.error("RODA Client Exception", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                    .getMessage());
        } catch (RemoteException e) {
            RODAException parsed = RODAClient.parseRemoteException(e);
            if (parsed instanceof AuthorizationDeniedException) {
                logger.info("Authorization Denied", parsed);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, parsed
                        .getMessage());
            } else {
                logger.error("RODA Client Exception", parsed);
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, parsed
                        .getMessage());
            }
        }
    }

    private void sendIcon(HttpSession session, RepresentationObject rep,
            HttpServletResponse response) throws IOException {
        String basePath = "/pt/gov/dgarq/roda/disseminators/AIPDownload/icons/";
        InputStream defaultIcon = AIPDownload.class.getClassLoader()
                .getResourceAsStream(basePath + "default.png");

        // Set headers
        response.setContentType("image/png");
        response.setHeader("Pragma", "");
        response.setHeader("Cache-Control", "private");

        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.MONTH, 1);
        response.setHeader("Expires", c.getTime().toString());

        // Send representation icon
        if (rep == null) {
            FileHelper.copy(defaultIcon, response.getOutputStream());
        } else if (rep.getPartFiles().length > 0
                || ALWAYS_ZIP.contains(rep.getType())) {
            InputStream zipIcon = AIPDownload.class.getClassLoader()
                    .getResourceAsStream(basePath + "application_zip.png");
            FileHelper.copy(zipIcon, response.getOutputStream());
        } else {
            String mimetype = null;
            mimetype = rep.getRootFile().getMimetype();
            mimetype = mimetype.replace('/', '_');
            mimetype = mimetype.replace('.', '_');
            mimetype = mimetype.replace(' ', '_');
            mimetype = mimetype.replace(',', '_');
            InputStream icon = AIPDownload.class.getClassLoader()
                    .getResourceAsStream(basePath + mimetype + ".png");
            if (icon == null) {
                logger.debug("No icon found for mimetype " + mimetype);
                icon = defaultIcon;
            }
            FileHelper.copy(icon, response.getOutputStream());
        }

    }

    private void sendRepresentation(HttpServletRequest request,
            RepresentationObject rep, HttpServletResponse response)
            throws LoginException, HttpException, IOException,
            RODAClientException {
        if ((rep.getPartFiles() == null || rep.getPartFiles().length == 0)
                && !ALWAYS_ZIP.contains(rep.getType())) {
            logger.debug("Forwarding file to client");
            getRepresentationHelper().forwardMethod(
                    getRepresentationHelper().getRootMethod(request, rep),
                    response);
            RodaClientFactory.log(DisseminationInfo.DOWNLOAD_DISSEMINATOR_ID,
                    false, rep.getPid(), request);
        } else {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + rep.getLabel() + ".zip");
            try {
                ZipTools.sendZippedRepresentation(request, rep, response
                        .getOutputStream());

                RodaClientFactory.log(
                        DisseminationInfo.DOWNLOAD_DISSEMINATOR_ID, true, rep
                        .getPid(), request);
            } catch (IOException e) {
                logger.info("User canceled download");
            }
        }
    }
}
