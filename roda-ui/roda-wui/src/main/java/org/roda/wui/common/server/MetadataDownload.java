package org.roda.wui.common.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class MetadataDownload
 */
public class MetadataDownload extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Servlet PID parameter key
   */
  public static String PARAM_PID = "pid";
  /**
   * Servlet type parameter key
   */
  public static String PARAM_TYPE = "type";

  /**
   * Servlet PREMIS type parameter value
   */
  public static String TYPE_PREMIS = "PREMIS";

  /**
   * Servlet EAD type parameter value
   */
  public static String TYPE_EAD = "EAD";

  private static final Logger logger = Logger.getLogger(MetadataDownload.class);

  public static String TYPE_OTHER_DESCRIPTIVE_METADATA = "OTHER";

  /**
   * @see HttpServlet#HttpServlet()
   */
  public MetadataDownload() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pid = request.getParameter(PARAM_PID);
    String type = request.getParameter(PARAM_TYPE);

    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // .getSession());
    //
    // if (type.equals(TYPE_PREMIS)) {
    // getPremis(pid, request, response, rodaClient);
    //
    // } else if (type.equals(TYPE_EAD)) {
    // getEad(pid, request, response, rodaClient);
    // }else{
    // response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    // "Wrong type '" + type + "'. Use '" + TYPE_EAD
    // + "' or '" + TYPE_PREMIS + "'");
    // }
    //
    // } catch (LoginException e) {
    // logger.error("Error getting RODA Client", e);
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
    // .getMessage());
    //
    // } catch (RODAClientException e) {
    // logger.error("Error getting RODA Client", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (NoSuchRODAObjectException e) {
    // response
    // .sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    // } catch (DownloaderException e) {
    // logger.error("Error getting Downloader", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (BrowserException e) {
    // logger.error("Error getting Browser", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }

  }

  // private void getEad(String pid, HttpServletRequest request,
  // HttpServletResponse response, RODAClient rodaClient)
  // throws IOException, LoginException, NoSuchRODAObjectException,
  // DownloaderException {
  // InputStream eadC = rodaClient.getDownloader().getFile(pid, "EAD-C");
  // response.setContentType("text/xml");
  // response.setHeader("Content-disposition", "attachment; filename="
  // + pid.replace(':', '_') + ".ead-c.xml");
  // IOUtils.copyLarge(eadC, response.getOutputStream());
  // response.getOutputStream().flush();
  // response.getOutputStream().close();
  //
  // }
  //
  // private void getPremis(String pid, HttpServletRequest request,
  // HttpServletResponse response, RODAClient rodaClient)
  // throws IOException, BrowserException, NoSuchRODAObjectException,
  // RODAClientException, LoginException, DownloaderException {
  //
  // RepresentationPreservationObject[] preservationObjects = rodaClient
  // .getBrowserService().getDOPreservationObjects(pid);
  //
  // if (preservationObjects == null) {
  // throw new DownloaderException("The object with PID \"" + pid
  // + "\" doesn't have preservation information...");
  // }
  //
  // File premisDir = TempDir.createUniqueTemporaryDirectory("premis");
  //
  // Set<String> agentPids = new HashSet<String>();
  //
  // for (RepresentationPreservationObject pObj : preservationObjects) {
  // File repDir = new File(premisDir, "representation_"
  // + pObj.getPid().replace(':', '_'));
  // repDir.mkdir();
  // copyDatastreams(pObj, repDir, rodaClient.getDownloader());
  //
  // EventPreservationObject[] repEvents = rodaClient
  // .getBrowserService().getPreservationEvents(pObj.getPid());
  // if (repEvents == null) {
  // repEvents = new EventPreservationObject[] {};
  // }
  //
  // for (EventPreservationObject repEvent : repEvents) {
  // // create event file
  // String eventPid = repEvent.getPid();
  // File eventFile = new File(repDir, "event_"
  // + eventPid.replace(':', '_') + ".premis.xml");
  // InputStream eventStream = rodaClient.getDownloader().getFile(
  // eventPid, "PREMIS");
  // IOUtils.copy(eventStream, new FileOutputStream(eventFile));
  //
  // // add agent to list
  // agentPids.add(repEvent.getAgentPID());
  // }
  // }
  //
  // for (String agentPid : agentPids) {
  // File agentFile = new File(premisDir, "agent_"
  // + agentPid.replace(':', '_') + ".premis.xml");
  // InputStream agentStream = rodaClient.getDownloader().getFile(
  // agentPid, "PREMIS");
  //
  // IOUtils.copy(agentStream, new FileOutputStream(agentFile));
  // }
  //
  // File premisZip = File.createTempFile("premis", ".zip");
  //
  // ZipUtility.createZIPFile(premisZip, premisDir);
  //
  // response.setContentType("application/zip");
  // response.setContentLength((int) premisZip.length());
  // response.setHeader("Content-disposition", "attachment; filename="
  // + pid.replace(':', '_') + ".premis.zip");
  // IOUtils.copyLarge(new FileInputStream(premisZip), response
  // .getOutputStream());
  //
  // }
  //
  // private void copyDatastreams(RepresentationPreservationObject obj,
  // File repDir, Downloader downloader)
  // throws NoSuchRODAObjectException, DownloaderException,
  // FileNotFoundException, IOException {
  //
  // // Copy representation
  // File repPremis = new File(repDir, "representation.premis.xml");
  // InputStream repPremisSteam = downloader.getFile(obj.getPid(), "PREMIS");
  // IOUtils.copy(repPremisSteam, new FileOutputStream(repPremis));
  //
  // // Copy root file
  // if (obj.getRootFile() != null) {
  // File root = new File(repDir, obj.getRootFile().getID()
  // + ".premis.xml");
  // InputStream rootStream = downloader.getFile(obj.getPid(), obj
  // .getRootFile().getID());
  // IOUtils.copy(rootStream, new FileOutputStream(root));
  // }
  //
  // // Copy part files
  // if (obj.getPartFiles() != null) {
  // for (RepresentationFilePreservationObject part : obj.getPartFiles()) {
  // File partFile = new File(repDir, part.getID() + ".premis.xml");
  // InputStream partStream = downloader.getFile(obj.getPid(), part
  // .getID());
  // IOUtils.copy(partStream, new FileOutputStream(partFile));
  // }
  // }
  //
  // }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

}
