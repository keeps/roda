/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client;

import org.roda.core.data.v2.jobs.Report;

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
   *          the report id
   * @return
   * @throws RODAException
   */
  public void getReport(String reportId, AsyncCallback<Report> callback);

}
