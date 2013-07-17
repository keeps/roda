package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("gaservice")
public interface GAnalyticsService extends RemoteService {

	public String getGoogleAnalyticsAccount();

}
