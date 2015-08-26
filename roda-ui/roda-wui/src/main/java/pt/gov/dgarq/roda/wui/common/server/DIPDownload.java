package pt.gov.dgarq.roda.wui.common.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.util.TempDir;

/**
 * Servlet implementation class DIPDownload
 * 
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class DIPDownload extends HttpServlet {

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
	private static final Logger logger = Logger.getLogger(DIPDownload.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DIPDownload() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String pidStr = request.getParameter(PARAM_PIDS);
		String[] pids = pidStr.split("[|]");
		String name = "DIP";
		File tempDir = TempDir.createUniqueTemporaryDirectory(name);
		// try {
		// response.setContentType("application/zip");
		// if (pids.length == 1) {
		// response.setHeader("Content-disposition",
		// "attachment; filename=" + pids[0].replace(':', '_')
		// + ".dip.zip");
		// } else {
		// response.setHeader("Content-disposition",
		// "attachment; filename=dip.zip");
		// }
		// RODAClient rodaClient = RodaClientFactory.getRodaClient(request
		// .getSession());
		// for (String pid : pids) {
		// File dipDir = new File(tempDir, pid.replace(':', '_'));
		// dipDir.mkdir();
		// getRepresentationFiles(pid, rodaClient, dipDir);
		// getEad(pid, request, response, rodaClient, dipDir);
		// }
		// // Create ZIP output stream
		// ZipOutputStream zip = new ZipOutputStream(
		// response.getOutputStream());
		// // Add all files to zip recursivelly
		// ZipUtility.addDirToArchive(zip, tempDir);
		// // Send zip stream to client
		// zip.close();
		// //response.flushBuffer();
		// } catch (LoginException e) {
		// logger.error("Error getting RODA Client", e);
		// response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
		// e.getMessage());
		//
		// } catch (RODAClientException e) {
		// logger.error("Error getting RODA Client", e);
		// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		// e.getMessage());
		// } catch (NoSuchRODAObjectException e) {
		// response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		// } catch (DownloaderException e) {
		// logger.error("Error getting Downloader", e);
		// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		// e.getMessage());
		// } catch (BrowserException e) {
		// logger.error("Error getting Browser", e);
		// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		// e.getMessage());
		// } finally {
		// // Remove temp dir
		// if ((tempDir != null) && (tempDir.exists())) {
		// FileUtils.deleteDirectory(tempDir);
		// }
		// }
	}

	// private void getEad(String pid, HttpServletRequest request,
	// HttpServletResponse response, RODAClient rodaClient, File dipDir)
	// throws IOException, LoginException, NoSuchRODAObjectException,
	// DownloaderException {
	// File eadFile = new File(dipDir, pid.replace(':', '_') + ".ead-c.xml");
	// InputStream eadC = rodaClient.getDownloader().getFile(pid, "EAD-C");
	// IOUtils.copy(eadC, new FileOutputStream(eadFile));
	// }
	//
	// private void getRepresentationFiles(String pid, RODAClient rodaClient,
	// File dipDir) throws IOException, BrowserException,
	// NoSuchRODAObjectException, RODAClientException, LoginException,
	// DownloaderException {
	//
	// File representationFilesDir = new File(dipDir, "representation_files");
	// representationFilesDir.mkdir();
	//
	// // download normalized version
	// RepresentationObject rep = rodaClient.getBrowserService()
	// .getDONormalizedRepresentation(pid);
	// if (rep == null) {
	// // normalzied verison doesnt exists lets download original version
	// rep = rodaClient.getBrowserService().getDOOriginalRepresentation(
	// pid);
	// }
	//
	// // Copy root file
	// File root = new File(representationFilesDir, rep.getRootFile().getId()
	// + "_" + rep.getRootFile().getOriginalName());
	// InputStream rootStream = rodaClient.getDownloader().get(
	// rep.getRootFile().getAccessURL());
	// IOUtils.copy(rootStream, new FileOutputStream(root));
	//
	// // Copy part files
	// if (rep.getPartFiles() != null) {
	// for (RepresentationFile part : rep.getPartFiles()) {
	// File partFile = new File(representationFilesDir, part.getId()
	// + "_" + part.getOriginalName());
	// InputStream partStream = rodaClient.getDownloader().get(
	// part.getAccessURL());
	// IOUtils.copy(partStream, new FileOutputStream(partFile));
	// }
	// }
	// }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
