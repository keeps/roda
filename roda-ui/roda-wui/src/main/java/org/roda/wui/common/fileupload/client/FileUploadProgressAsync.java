/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.fileupload.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileUploadProgressAsync {

  /**
   * Get the upload progress
   * 
   * @return The progress percentage, from 0 to 1, or -1 if unknown or not
   *         applicable
   */
  public void getProgress(AsyncCallback<Double> callback);

}
