/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

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
