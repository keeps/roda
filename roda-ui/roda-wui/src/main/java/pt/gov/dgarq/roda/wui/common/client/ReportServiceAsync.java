package pt.gov.dgarq.roda.wui.common.client;

import pt.gov.dgarq.roda.core.data.Report;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Report service interface
 * 
 * @author Luis Faria
 * 
 */
public interface ReportServiceAsync {

	/**
	 * Get report
	 * 
	 * @param reportId
	 *            the report id
	 * @return
	 * @throws RODAException
	 */
	public void getReport(String reportId, AsyncCallback<Report> callback);

}
