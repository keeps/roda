package pt.gov.dgarq.roda.wui.common.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.util.ZipUtility;

/**
 * Servlet implementation class DIPDownloadDEMO
 *
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class DIPDownloadDEMO extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /**
     * Servlet PID parameter key
     */
    public static String PARAM_PIDS = "pids";
    /**
     * Servlet PREMIS type parameter value
     */
    public static String TYPE_PREMIS = "PREMIS";
    /**
     * Servlet EAD type parameter value
     */
    public static String TYPE_EAD = "EAD";
    private static final Logger logger = Logger
            .getLogger(DIPDownloadDEMO.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DIPDownloadDEMO() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pidStr = request.getParameter(PARAM_PIDS);
        String[] pids = pidStr.split("[|]");

        try {
            RODAClient rodaClient = RodaClientFactory.getRodaClient(request.getSession());
            getDIP(pids, request, response, rodaClient);
        } catch (LoginException e) {
            logger.error("Error getting RODA Client", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
                    .getMessage());

        } catch (RODAClientException e) {
            logger.error("Error getting RODA Client", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                    .getMessage());
        } catch (NoSuchRODAObjectException e) {
            response
                    .sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DownloaderException e) {
            logger.error("Error getting Downloader", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                    .getMessage());
        } catch (BrowserException e) {
            logger.error("Error getting Browser", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
                    .getMessage());
        }
    }

    private void getDIP(String[] pids, HttpServletRequest request, HttpServletResponse response, RODAClient rodaClient) throws IOException, BrowserException, NoSuchRODAObjectException, RODAClientException, LoginException, DownloaderException {
        String name = "DIP";
        File tempDir = TempDir.createUniqueTemporaryDirectory(name);
        for (String pid : pids) {
            File dipDir = new File(tempDir, pid.replace(':', '_'));
            dipDir.mkdir();
            getRepresentationFiles(pid, rodaClient, dipDir);
            getEad(pid, request, response, rodaClient, dipDir);
        }
        File premisZip = File.createTempFile(name, ".zip");
        ZipUtility.createZIPFile(premisZip, tempDir);
        response.setContentType("application/zip");
        response.setContentLength((int) premisZip.length());
        if (pids.length == 1) {
            response.setHeader("Content-disposition", "attachment; filename=" + pids[0].replace(':', '_') + ".dip.zip");
        } else {
            response.setHeader("Content-disposition", "attachment; filename=dip.zip");
        }
        IOUtils.copyLarge(new FileInputStream(premisZip), response.getOutputStream());
    }

    private void getEad(String pid, HttpServletRequest request,
            HttpServletResponse response, RODAClient rodaClient, File dipDir)
            throws IOException, LoginException, NoSuchRODAObjectException,
            DownloaderException {
        File eadFile = new File(dipDir, pid.replace(':', '_') + ".ead-c.xml");
        InputStream eadC = rodaClient.getDownloader().getFile(pid, "EAD-C");
        IOUtils.copy(eadC, new FileOutputStream(eadFile));
    }

    private void getRepresentationFiles(String pid, RODAClient rodaClient, File dipDir)
            throws IOException, BrowserException, NoSuchRODAObjectException,
            RODAClientException, LoginException, DownloaderException {

        File representationFilesDir = new File(dipDir, "representation_files");
        representationFilesDir.mkdir();

        // download normalized version
        RepresentationObject rep = rodaClient.getBrowserService().getDONormalizedRepresentation(pid);
        if (rep == null) {
            // normalzied verison doesnt exists lets download original version
            rep = rodaClient.getBrowserService().getDOOriginalRepresentation(pid);
        }

        // Copy root file
        File root = new File(representationFilesDir, rep.getRootFile().getId() + "_" + rep.getRootFile().getOriginalName());
        InputStream rootStream = rodaClient.getDownloader().get(rep.getRootFile().getAccessURL());
        IOUtils.copy(rootStream, new FileOutputStream(root));

        // Copy part files
        if (rep.getPartFiles() != null) {
            for (RepresentationFile part : rep.getPartFiles()) {
                File partFile = new File(representationFilesDir, part.getId() + "_" + part.getOriginalName());
                InputStream partStream = rodaClient.getDownloader().get(part.getAccessURL());
                IOUtils.copy(partStream, new FileOutputStream(partFile));
            }
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
