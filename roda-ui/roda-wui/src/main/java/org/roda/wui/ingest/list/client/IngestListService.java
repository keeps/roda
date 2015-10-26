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

import org.roda.core.common.AuthorizationDeniedException;
import org.roda.core.common.RODAException;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SIPReport;
import org.roda.wui.common.client.GenericException;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface IngestListService extends RemoteService {

  /**
   * Ingest list service URI
   */
  public static final String SERVICE_URI = "ingestlist";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static IngestListServiceAsync getInstance() {

      IngestListServiceAsync instance = (IngestListServiceAsync) GWT.create(IngestListService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  public Long countSipReports(Filter filter) throws AuthorizationDeniedException, GenericException;

  public IndexResult<SIPReport> findSipReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException;

  public SIPReport retrieveSipReport(String sipReportId) throws AuthorizationDeniedException, GenericException;

  /**
   * Accept a SIP
   * 
   * @param sipId
   * @param message
   * @throws RODAException
   */
  public void acceptSIP(String sipId, String message) throws RODAException;

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
  public void rejectSIP(String sipId, String message, boolean notifyProducer) throws RODAException;

  /**
   * Set SIP list report info parameters
   * 
   * @param adapter
   * @param locale
   * @throws RODAException
   * @throws PrintReportException
   */
  public void setSIPListReportInfo(ContentAdapter adapter, String locale) throws PrintReportException;

  /**
   * Get accept message templates
   * 
   * @param localeString
   * @return
   */
  public Map<String, String> getAcceptMessageTemplates(String localeString);

  /**
   * Get reject message templates
   * 
   * @param localeString
   * @return
   */
  public Map<String, String> getRejectMessageTemplates(String localeString);

}
