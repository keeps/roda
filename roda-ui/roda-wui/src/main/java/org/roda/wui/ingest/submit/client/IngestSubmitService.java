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
package org.roda.wui.ingest.submit.client;

import java.io.IOException;

import org.roda.core.common.AuthorizationDeniedException;
import org.roda.core.common.LoginException;
import org.roda.core.common.RODAClientException;
import org.roda.core.data.DescriptionObject;
import org.roda.wui.common.client.GenericException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface IngestSubmitService extends RemoteService {

  /**
   * Ingest submit service URI
   */
  public static final String SERVICE_URI = "ingestsubmitservice";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static IngestSubmitServiceAsync getInstance() {

      IngestSubmitServiceAsync instance = (IngestSubmitServiceAsync) GWT.create(IngestSubmitService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Submit SIPs as bytestreams
   * 
   * @param fileCodes
   *          the codes of the uploaded bytestream
   * 
   * @return true if all SIPs were successfully submitted, false otherwise
   * @throws LoginException
   * @throws GenericException
   * @throws RODAClientException
   * @throws AuthorizationDeniedException
   * @throws IOException
   */
  public boolean submitSIPs(String[] fileCodes) throws LoginException, RODAClientException, GenericException,
    AuthorizationDeniedException, IOException;

  /**
   * Create a new SIP
   * 
   * @param contentModel
   *          the SIP content model
   * 
   * @param metadata
   *          the SIP descriptive metadata
   * @param fileCodes
   *          the represention files' codes
   * @param parentPID
   *          the pid of the element which will be parent of the element created
   *          with this SIP
   * @return true if SIP successfully submited, false otherwise
   * @throws LoginException
   * @throws GenericException
   */
  public boolean createSIP(String contentModel, DescriptionObject metadata, String[] fileCodes, String parentPID)
    throws LoginException, GenericException;

}
