package pt.gov.dgarq.roda.wui.common.fileupload.client;

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
