package pt.gov.dgarq.roda.core.services;

import java.io.File;
import java.io.IOException;
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
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.ingest.SIPUploadTask;

/**
 * Servlet implementation class for Servlet: SIPUploadServlet
 * 
 */
public class SIPUploadServlet extends RODAServlet {
	private static final long serialVersionUID = 124550852289347231L;

	static final private Logger logger = Logger
			.getLogger(SIPUploadServlet.class);

	private HttpServletRequest currentRequest = null;

	/**
	 * @throws RODAServiceException
	 * @see HttpServlet#HttpServlet()
	 */
	public SIPUploadServlet() throws RODAServiceException {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Date start = new Date();

		this.currentRequest = request;

		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory(0, new File(
					System.getProperty("java.io.tmpdir")));

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			try {

				response.setContentType("text/plain");

				// Get information about the user doing the upload
				User user = getClientUser();

				// Parse the request
				List<FileItem> items = upload.parseRequest(request);

				for (FileItem fileItem : items) {

					if (fileItem.isFormField()) {
						// Bad!!! only files should be uploaded
						logger.warn(fileItem.getFieldName()
								+ " is a form field instead of a file. "
								+ "Ignoring this field.");
					} else {

						try {

							SIPUploadTask sipUploadTask = new SIPUploadTask(
									fileItem);
							SIPState insertedSIP = sipUploadTask.insertSIP(user
									.getName(), fileItem.getName());

							response.getWriter().write(insertedSIP.toString());

						} catch (IngestException e) {
							logger.error("Error inserting SIP - "
									+ e.getMessage(), e);
							response
									.sendError(
											HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
											"Error inserting SIP - "
													+ e.getMessage());
						}
					}
				}

				response.setStatus(HttpServletResponse.SC_CREATED);
				response.flushBuffer();

				long duration = new Date().getTime() - start.getTime();
				registerAction("SIPUploadServlet.POST", new String[] { "sips",
						"" + items },
						"User %username% called method SIPUploadServlet.POST("
								+ items + ")", duration);

			} catch (FileUploadException e) {
				// Bad!!! Upload failed.
				logger.error("Error parsing SIP upload request - "
						+ e.getMessage(), e);
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Error parsing SIP upload request - " + e.getMessage());
			}

		} else {
			// Bad!!! only files should be uploaded
			logger.error("SIP Upload request should contain only files");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"SIP Upload request should contain only files");
		}

	}

	@Override
	protected HttpServletRequest getCurrentRequest() {
		return this.currentRequest;
	}
}
