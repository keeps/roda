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
package org.roda.wui.common.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public interface HistoryResolver {

  /**
   * Get the history token that distinguishes this history resolver on the
   * current scope
   * 
   * @return
   */
  public String getHistoryToken();

  /**
   * Get the complete history path of this history resolver. This can be used to
   * call this resolver with History.newItem(resolver.getHistoryPath);
   * 
   * @return
   */
  public List<String> getHistoryPath();

  /**
   * Check the current user permissions of accessing this resolver
   * 
   * @param callback
   *          Callback to handle the result, Boolean.TRUE if user has
   *          permissions, Boolean.FALSE otherwise
   */
  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback);

  /**
   * Resolve the history path on the scope of this resolver
   * 
   * @param historyTokens
   *          the history path
   * @param callback
   *          Callback to handle the result Widget or the exception
   *          BadHistoryTokenException or AuthorizationDeniedException
   */
  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback);

}
