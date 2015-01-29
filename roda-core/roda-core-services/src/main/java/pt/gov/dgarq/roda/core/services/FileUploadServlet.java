package pt.gov.dgarq.roda.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * Servlet implementation class for Servlet: FileUploadServlet
 */
public class FileUploadServlet extends RODAServlet {
  private static final long serialVersionUID = -9044858508538982735L;

  static final private Logger logger = Logger.getLogger(FileUploadServlet.class);

  private String fedoraURL = "http://localhost:8080/fedora";
  private String fedoraGSearchURL = "http://localhost:8080/fedoragsearch";
  private FedoraClientUtility fedoraClientUtility = null;

  private HttpServletRequest currentRequest = null;

  /**
   * 
   * @throws RODAServiceException
   * 
   * @see HttpServlet#HttpServlet()
   */
  public FileUploadServlet() throws RODAServiceException {
    super();

    this.fedoraURL = getConfiguration().getString("fedoraURL");
    this.fedoraGSearchURL = getConfiguration().getString("fedoraGSearchURL");
  }

  /**
   * 
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    this.currentRequest = request;
    this.currentRequest.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

    // Get information about the user doing the upload
    CASUserPrincipal clientUser = getClientUser();

    if (clientUser == null) {

      // Bad!!! no information about the user.
      logger.error("Not enought information about client user. Responding with HTTP UNAUTHORIZED");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not enought information about client user.");

    } else {

      try {
        this.fedoraClientUtility = new FedoraClientUtility(fedoraURL, fedoraGSearchURL, clientUser, getCasUtility());

        processRequest(request, response);

      } catch (FedoraClientException e) {

        logger.error("Error creating Fedora client - " + e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error creating Fedora client - " + e.getMessage());

      }

    }

  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Date start = new Date();

    // Check that we have a file upload request
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);

    if (!isMultipart) {

      // Bad!!! only files should be uploaded
      logger.error("Representation file upload request should be multipart");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Representation file upload request should be multipart");

    } else {

      // Create a factory for disk-based file items
      FileItemFactory factory = new DiskFileItemFactory(0, TempDir.getTemporaryDirectory());

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      String roPID = null;

      try {

        // Parse the request
        List<FileItem> items = upload.parseRequest(request);

        String fileID = null;
        String fileOriginalName = null;
        String fileMimetype = null;
        String puid = null;
        long fileSize = 0;
        FileItem fileFileItem = null;

        for (FileItem fileItem : items) {

          if (fileItem.isFormField()) {

            if ("id".equals(fileItem.getFieldName())) {

              if (fileID != null) {
                logger.warn(String.format("Parameter 'id' already set to '%1$s'. Ignoring new value '%2$s'", fileID,
                  fileItem.getString()));
              } else {
                fileID = fileItem.getString();
              }

            } else if ("name".equals(fileItem.getFieldName())) {

              if (fileOriginalName != null) {
                logger.warn(String.format("Parameter 'name' already set to '%1$s'. Ignoring new value '%2$s'", fileID,
                  fileItem.getString()));
              } else {
                fileOriginalName = fileItem.getString();
              }

            } else if ("mimetype".equals(fileItem.getFieldName())) {

              if (fileMimetype != null) {
                logger.warn(String.format("Parameter 'mimetype' already set to '%1$s'. Ignoring new value '%2$s'",
                  fileID, fileItem.getString()));
              } else {
                fileMimetype = fileItem.getString();
              }
            } else if ("puid".equals(fileItem.getFieldName())) {

              if (puid != null) {
                logger.warn(String.format("Parameter 'puid' already set to '%1$s'. Ignoring new value '%2$s'",
                  fileID, fileItem.getString()));
              } else {
                puid = fileItem.getString();
              }

            } else if ("size".equals(fileItem.getFieldName())) {

              if (fileSize > 0) {
                logger.warn(String.format("Parameter 'size' already set to '%1$s'. Ignoring new value '%2$s'", fileID,
                  fileItem.getString()));
              } else {
                fileSize = Long.parseLong(fileItem.getString());
              }

            } else if ("pid".equals(fileItem.getFieldName())) {

              if (roPID != null) {
                logger.warn(String.format("Parameter 'pid' already set to '%1$s'. Ignoring new value '%2$s'", roPID,
                  fileItem.getString()));
              } else {
                roPID = fileItem.getString();
              }

            } else {
              logger.warn("Unsupported parameter " + fileItem.getFieldName() + ". Ignored.");
            }

          } else {

            if ("file".equals(fileItem.getFieldName())) {

              if (fileFileItem != null) {

                logger.warn(String.format("Parameter 'file' already set to '%1$s'. Ignoring new value '%2$s'",
                  fileOriginalName, fileItem.getName()));

              } else {
                // fileOriginalName = fileItem.getName();
                // fileMimetype = fileItem.getContentType();
                // fileSize = fileItem.getSize();
                fileFileItem = fileItem;
              }

            } else {
              logger.warn("Unsupported parameter " + fileItem.getFieldName() + ". Ignored.");
            }

          }
        }

        uploadToFedora(roPID, fileID, fileOriginalName, fileMimetype, puid, fileSize, fileFileItem);

        response.setContentType("text/plain");

        response.getWriter().write(
          String.format("roPID=%1$s, fileID=%2$s, fileOriginalName=%3$s, fileMimetype=%4$s, fileSize=%5$d", roPID,
            fileID, fileOriginalName, fileMimetype, fileSize));

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.flushBuffer();

        long duration = new Date().getTime() - start.getTime();

        registerAction("FileUploadServlet.POST", new String[] {"id", fileID, "originalName", fileOriginalName,
          "mimetype", fileMimetype, "size", "" + fileSize, "pid", roPID},
          "User %username% called method FileUploadServlet.POST(" + fileID + ", " + fileOriginalName + ", "
            + fileMimetype + ", " + fileSize + ", " + roPID + ")", duration);

        logger.debug("FileUploadServlet.POST pid: " + roPID);
        logger.debug("FileUploadServlet.POST id: " + fileID);
        logger.debug("FileUploadServlet.POST name: " + fileOriginalName);
        logger.debug("FileUploadServlet.POST mimetype: " + fileMimetype);
        logger.debug("FileUploadServlet.POST size: " + fileSize);

      } catch (FileUploadException e) {

        // Bad!!! Upload failed.
        logger.error("Error parsing file upload request - " + e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error parsing file upload request - " + e.getMessage());

      } catch (FedoraClientException e) {

        // Bad!!! Error uploading file to Fedora

        logger.debug("Error uploading file to Fedora - " + e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error uploading file to Fedora - " + e.getMessage());

      } catch (IllegalOperationException e) {

        // Representation is already being preserved.

        logger.debug("Operation is not permitted - " + e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Operation is not permitted - " + e.getMessage());

      } catch (NoSuchRODAObjectException e) {
        // Representation does not exist.

        logger.debug("Representation " + roPID + "doesn't exist - " + e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Representation " + roPID + "doesn't exist - " + e.getMessage());
      }
    }
  }

  private void uploadToFedora(String roPID, String fileID, String fileOriginalName, String fileMimetype, String puid,
    long fileSize, FileItem fileItem) throws FedoraClientException, NoSuchRODAObjectException,
    IllegalOperationException {

    RODAObject preservationObject = this.fedoraClientUtility.getFedoraRISearch().getROPreservationObject(roPID);

    if (preservationObject != null) {

      throw new IllegalOperationException("Representation " + roPID
        + " is already being preserved. No more files can be added");

    } else {

      String temporaryURL = this.fedoraClientUtility.temporaryUpload(new RepresentationFilePartSource(fileOriginalName,
        fileSize, fileItem));

      try {

        logger.info("Adding datastream (roPID:" + roPID + ";fileID:" + fileID + ";fileOriginalName:" + fileOriginalName
          + ";fileMimetype:" + fileMimetype + ";puid:" + puid + ";size:" + fileSize + ")");

        this.fedoraClientUtility.getAPIM().addDatastream(roPID, fileID, new String[] {}, fileOriginalName, true,
          fileMimetype, puid, temporaryURL, "M", "A", null, null, "Added by RODA Core");

      } catch (RemoteException e) {
        String errorMessage = String.format("Error adding new datastream %1$s to object %2$s - %3$s", fileID, roPID,
          e.getMessage());
        logger.debug(errorMessage, e);
        throw new FedoraClientException(errorMessage, e);
      }

    }

  }

  @Override
  protected HttpServletRequest getCurrentRequest() {
    return this.currentRequest;
  }

  class RepresentationFilePartSource implements PartSource {

    private String fileName = null;
    private long fileSize = 0;
    private FileItem fileItem = null;

    /**
     * @param fileName
     * @param fileSize
     * @param fileItem
     */
    public RepresentationFilePartSource(String fileName, long fileSize, FileItem fileItem) {
      this.fileName = fileName;
      this.fileSize = fileSize;
      this.fileItem = fileItem;
    }

    /**
     * @see PartSource#createInputStream()
     */
    public InputStream createInputStream() throws IOException {
      return fileItem.getInputStream();
    }

    /**
     * @see PartSource#getFileName()
     */
    public String getFileName() {
      return fileName;
    }

    /**
     * @see PartSource#getLength()
     */
    public long getLength() {
      return fileSize;
    }
  }
}