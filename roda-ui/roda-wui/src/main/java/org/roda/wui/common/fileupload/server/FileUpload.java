/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.fileupload.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.roda.core.util.TempDir;
import org.roda.wui.common.fileupload.client.FileUploadPanel;

/**
 * File upload servlet
 * 
 * @author Luis Faria
 * 
 */
public class FileUpload extends HttpServlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = -5990339251413849287L;

  private static final String UPLOADED_FILES_ATTRIBUTE = "FileUpload_File";

  /**
   * The name of the session attribute that holds current upload progress
   */
  public static final String UPLOADED_PROGRESS_ATTRIBUTE = "FileUpload_Progress";

  private static Logger logger = Logger.getLogger(FileUpload.class);

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/plain");

    try {
      List<FileItem> fileItems = getFileItems(request);
      if (fileItems.size() == 0) {
        logger.error("While uploading file, no file item found");
        response.getWriter().write(FileUploadPanel.UPLOAD_FAILURE + " " + "While uploading file, no file item found");
      } else {
        String hashcodes = "";
        for (Iterator<FileItem> i = fileItems.iterator(); i.hasNext();) {
          FileItem item = i.next();
          hashcodes += item.hashCode() + (i.hasNext() ? " " : "");
        }
        response.getWriter().write(FileUploadPanel.UPLOAD_SUCCESS + " " + hashcodes);
      }

    } catch (FileUploadException e) {
      response.getWriter().write(FileUploadPanel.UPLOAD_FAILURE + " " + e.getMessage());
      logger.info("Error uploading file (or user cancelled)", e);

    }
  }

  @SuppressWarnings("unchecked")
  private List<FileItem> getFileItems(final HttpServletRequest request) throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory(10240, TempDir.getTemporaryDirectory());
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> ret = new Vector<FileItem>();

    upload.setProgressListener(new ProgressListener() {

      public void update(long pBytesRead, long pContentLength, int pItems) {
        logger.debug("Progress: " + pBytesRead + ", " + pContentLength + ", " + pItems);
        double progress;
        if (pContentLength > 0) {
          progress = pBytesRead / (double) pContentLength;
        } else {
          progress = -1;
        }

        request.getSession().setAttribute(UPLOADED_PROGRESS_ATTRIBUTE, progress);
      }

    });

    List<FileItem> items = upload.parseRequest(request);
    for (FileItem item : items) {
      if (!item.isFormField() && FileUploadPanel.FILE_UPLOAD_NAME.equals(item.getFieldName())) {
        addFileItem(request.getSession(), item);
        ret.add(item);
      }
    }
    return ret;

  }

  /**
   * Get the list of the uploaded files on this session
   * 
   * @param session
   *          the request session
   * @return a list with the uploaded files
   */
  @SuppressWarnings("unchecked")
  public static List<FileItem> getUploadedFiles(HttpSession session) {
    List<FileItem> files = (List<FileItem>) session.getAttribute(UPLOADED_FILES_ATTRIBUTE);
    if (files == null) {
      files = new Vector<FileItem>();
      session.setAttribute(UPLOADED_FILES_ATTRIBUTE, files);
    }
    return files;
  }

  /**
   * Add a file item to the uploaded files session list
   * 
   * @param session
   *          the request session
   * @param item
   *          the file item to add
   */
  public static void addFileItem(HttpSession session, FileItem item) {
    List<FileItem> uploadedFiles = getUploadedFiles(session);
    uploadedFiles.add(item);
    session.setAttribute(UPLOADED_FILES_ATTRIBUTE, uploadedFiles);
  }

  /**
   * Remove a file item from the uploaded files session list
   * 
   * @param session
   *          the session request
   * @param item
   *          the file item to remove
   */
  public static void removeFileItem(HttpSession session, FileItem item) {
    List<FileItem> uploadedFiles = getUploadedFiles(session);
    uploadedFiles.remove(item);
    session.setAttribute(UPLOADED_FILES_ATTRIBUTE, uploadedFiles);
  }

  /**
   * Lookup a file in session
   * 
   * @param session
   *          the user session where to lookup
   * @param fileCode
   *          the code of the file
   * @return the file item or null if code not found
   */
  public static FileItem lookupFileItem(HttpSession session, String fileCode) {
    List<FileItem> uploadedFiles = getUploadedFiles(session);
    FileItem ret = null;
    for (FileItem item : uploadedFiles) {
      if (fileCode.equals(item.hashCode() + "")) {
        ret = item;
      }
    }
    return ret;
  }

  /**
   * Lookup a group of file codes
   * 
   * @param session
   *          the user session where to lookup files
   * @param fileCodes
   *          the codes of the files
   * @return the FileItems if ALL files found or null otherwise
   */
  public static FileItem[] lookupFileItems(HttpSession session, String[] fileCodes) {
    boolean allFilesFound = true;
    FileItem[] items = new FileItem[fileCodes.length];
    for (int i = 0; i < fileCodes.length; i++) {
      logger.info("looking up file code: " + fileCodes[i]);
      items[i] = FileUpload.lookupFileItem(session, fileCodes[i]);
      if (items[i] != null) {
        FileUpload.removeFileItem(session, items[i]);
      } else {
        allFilesFound = false;
      }
    }
    return allFilesFound ? items : null;
  }

}
