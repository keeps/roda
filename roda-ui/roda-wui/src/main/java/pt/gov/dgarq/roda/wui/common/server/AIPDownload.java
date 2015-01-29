package pt.gov.dgarq.roda.wui.common.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.util.ZipUtility;

/**
 * Servlet implementation class AIPDownload
 *
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class AIPDownload extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /**
     * Servlet PID parameter key
     */
    public static String PARAM_PID = "pid";
    /**
     * Servlet PREMIS type parameter value
     */
    public static String TYPE_PREMIS = "PREMIS";
    /**
     * Servlet EAD type parameter value
     */
    public static String TYPE_EAD = "EAD";
    private static final Logger logger = Logger.getLogger(AIPDownload.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AIPDownload() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pid = request.getParameter(PARAM_PID);
        String name = "AIP-" + pid.replace(':', '_');
        File tempDir = TempDir.createUniqueTemporaryDirectory(name);
        try {
            RODAClient rodaClient = RodaClientFactory.getRodaClient(request.getSession());
            getPremisAndRepresentations(pid, rodaClient, tempDir);
            response.setContentType("application/zip");            
            response.setHeader("Content-disposition", "attachment; filename=" + pid.replace(':', '_') + ".aip.zip");
            // Create ZIP output stream            
            ZipOutputStream zip = new ZipOutputStream(response.getOutputStream());
            // Add all files to zip recursivelly
            ZipUtility.addDirToArchive(zip, tempDir);
            // Send zip stream to client
            zip.close();
            //response.flushBuffer();
        } catch (LoginException e) {
            logger.error("Error getting RODA Client", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (RODAClientException e) {
            logger.error("Error getting RODA Client", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (NoSuchRODAObjectException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DownloaderException e) {
            logger.error("Error getting Downloader", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (BrowserException e) {
            logger.error("Error getting Browser", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            // Remove temp dir
            if ((tempDir != null) && (tempDir.exists())) {
                FileUtils.deleteDirectory(tempDir);
            }
        }

    }

    private void getEad(String pid, HttpServletRequest request, HttpServletResponse response, RODAClient rodaClient, File aipDir) throws IOException, LoginException, NoSuchRODAObjectException, DownloaderException {
        File eadFile = new File(aipDir, pid.replace(':', '_') + ".ead-c.xml");
        InputStream eadC = rodaClient.getDownloader().getFile(pid, "EAD-C");
        IOUtils.copy(eadC, new FileOutputStream(eadFile));
    }

    private void getPremisAndRepresentations(String pid, RODAClient rodaClient, File aipDir) throws IOException, BrowserException, NoSuchRODAObjectException, RODAClientException, LoginException, DownloaderException {

        RepresentationPreservationObject[] preservationObjects = rodaClient.getBrowserService().getDOPreservationObjects(pid);
        Set<String> agentPids = new HashSet<String>();
        
        if (preservationObjects == null) {
           throw new NoSuchRODAObjectException("The object with PID \"" + pid
           + "\" doesn't have preservation information...");
        }

        for (RepresentationPreservationObject pObj : preservationObjects) {
            File repDir = new File(aipDir, "representation_" + pObj.getPid().replace(':', '_'));
            repDir.mkdir();
            copyDatastreams(pObj, repDir, rodaClient.getDownloader());

            // Download representations
            String doPid = pObj.getRootFile().getContentLocationValue().split("/")[0];
            File representationFilesDir = new File(repDir, "representation_files");
            representationFilesDir.mkdir();
            // Original
            saveRepresentationObject(doPid, rodaClient, representationFilesDir);

            EventPreservationObject[] repEvents = rodaClient.getBrowserService().getPreservationEvents(pObj.getPid());
            if (repEvents == null) {
                repEvents = new EventPreservationObject[]{};
            }

            for (EventPreservationObject repEvent : repEvents) {
                // create event file
                String eventPid = repEvent.getPid();
                File eventFile = new File(repDir, "event_" + eventPid.replace(':', '_') + ".premis.xml");
                InputStream eventStream = rodaClient.getDownloader().getFile(eventPid, "PREMIS");
                IOUtils.copy(eventStream, new FileOutputStream(eventFile));

                // add agent to list
                agentPids.add(repEvent.getAgentPID());
            }
        }

        for (String agentPid : agentPids) {
            File agentFile = new File(aipDir, "agent_" + agentPid.replace(':', '_') + ".premis.xml");
            InputStream agentStream = rodaClient.getDownloader().getFile(agentPid, "PREMIS");

            IOUtils.copy(agentStream, new FileOutputStream(agentFile));
        }
    }

    private void copyDatastreams(RepresentationPreservationObject obj, File repDir, Downloader downloader) throws NoSuchRODAObjectException, DownloaderException, FileNotFoundException, IOException {

        // Copy representation
        File repPremis = new File(repDir, "representation.premis.xml");
        InputStream repPremisSteam = downloader.getFile(obj.getPid(), "PREMIS");
        IOUtils.copy(repPremisSteam, new FileOutputStream(repPremis));

        // Copy root file
        if (obj.getRootFile() != null) {
            File root = new File(repDir, obj.getRootFile().getID() + ".premis.xml");
            InputStream rootStream = downloader.getFile(obj.getPid(), obj.getRootFile().getID());
            IOUtils.copy(rootStream, new FileOutputStream(root));
        }

        // Copy part files
        if (obj.getPartFiles() != null) {
            for (RepresentationFilePreservationObject part : obj.getPartFiles()) {
                File partFile = new File(repDir, part.getID() + ".premis.xml");
                InputStream partStream = downloader.getFile(obj.getPid(), part.getID());
                IOUtils.copy(partStream, new FileOutputStream(partFile));
            }
        }

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private void saveRepresentationObject(String doPid, RODAClient rodaClient, File representationFilesDir) throws RemoteException, RODAClientException, NoSuchRODAObjectException, BrowserException, IOException, LoginException, DownloaderException {
        RepresentationObject rep = rodaClient.getBrowserService().getRepresentationObject(doPid);
        if (rep != null) {
            // Copy root file
            File root = new File(representationFilesDir, rep.getRootFile().getId() + "_" + rep.getRootFile().getOriginalName());
            InputStream rootStream = rodaClient.getDownloader().getFile(doPid, rep.getRootFile().getId());
            IOUtils.copy(rootStream, new FileOutputStream(root));

            // Copy part files
            if (rep.getPartFiles() != null) {
                for (RepresentationFile part : rep.getPartFiles()) {
                    File partFile = new File(representationFilesDir, part.getId() + "_" + part.getOriginalName());
                    InputStream partStream = rodaClient.getDownloader().getFile(doPid, part.getId());
                    IOUtils.copy(partStream, new FileOutputStream(partFile));
                }
            }
        }
    }
}
