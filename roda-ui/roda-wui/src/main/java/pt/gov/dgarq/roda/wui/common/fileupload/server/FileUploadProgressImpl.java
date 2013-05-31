package pt.gov.dgarq.roda.wui.common.fileupload.server;

import pt.gov.dgarq.roda.wui.common.fileupload.client.FileUploadProgress;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * 
 * @author Luis Faria
 * 
 */
public class FileUploadProgressImpl extends RemoteServiceServlet implements
		FileUploadProgress {

	private static final long serialVersionUID = 1L;

	public double getProgress() {
		double ret = -1;
		Object attribute = getThreadLocalRequest().getSession().getAttribute(
				FileUpload.UPLOADED_PROGRESS_ATTRIBUTE);
		if (attribute != null) {
			ret = (Double) attribute;
		}

		return ret;
	}

}
