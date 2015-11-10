/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.ingest.list.client;

import java.util.Map;

import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RODAException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SIPReport;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface IngestListServiceAsync {

  void countSipReports(Filter filter, AsyncCallback<Long> callback);

  void findSipReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<SIPReport>> callback);

  void retrieveSipReport(String sipReportId, AsyncCallback<SIPReport> callback);

  /**
   * Accept a SIP
   * 
   * @param sipId
   * @param message
   * @throws RODAException
   */
  public void acceptSIP(String sipId, String message, AsyncCallback<Void> callback);

  /**
   * Reject a SIP
   * 
   * @param sipId
   * @param message
   *          message detailing the cause of the rejection
   * @param notifyProducer
   *          where to notify the producer of the SIP by email about this
   *          rejection
   * @throws RODAException
   */
  public void rejectSIP(String sipId, String message, boolean notifyProducer, AsyncCallback<Void> callback);

  /**
   * Set SIP list report info parameters
   * 
   * @param adapter
   * @param locale
   * @throws RODAException
   * @throws PrintReportException
   */
  public void setSIPListReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback);

  /**
   * Get accept message templates
   * 
   * @param localeString
   * @return
   */
  public void getAcceptMessageTemplates(String localeString, AsyncCallback<Map<String, String>> callback);

  /**
   * Get reject message templates
   * 
   * @param localeString
   * @return
   */
  public void getRejectMessageTemplates(String localeString, AsyncCallback<Map<String, String>> callback);

}
