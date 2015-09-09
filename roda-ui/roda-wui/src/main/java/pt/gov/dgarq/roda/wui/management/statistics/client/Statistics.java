/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.management.client.Management;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class Statistics implements HistoryResolver {

  private static Statistics instance = null;

  /**
   * Get statistics instance
   * 
   * @return the singleton
   */
  public static Statistics getInstance() {
    if (instance == null) {
      instance = new Statistics();
    }
    return instance;
  }

  // private ClientLogger logger = new ClientLogger(getClass().getName());
  // private StatisticsConstants constants = (StatisticsConstants) GWT
  // .create(StatisticsConstants.class);

  private boolean initialized;
  private TabPanel layout;
  private List<StatisticTab> statisticTabs;

  /**
   * Create a new statistics
   */
  public Statistics() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      statisticTabs = new ArrayList<StatisticTab>();
      layout = new TabPanel();

      statisticTabs.add(new RepositoryStatistics());
      statisticTabs.add(new IngestStatistics());
      // statisticTabs.add(new EventStatistics());
      statisticTabs.add(new ProducersStatistics());
      statisticTabs.add(new UserStatistics());
      statisticTabs.add(new AccessStatistics());
      statisticTabs.add(new ActionsStatistics());
      statisticTabs.add(new SystemStatistics());

      for (StatisticTab tab : statisticTabs) {
        layout.add(tab, tab.getTabText());
      }

      layout.addTabListener(new TabListener() {

        public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
          return true;
        }

        public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
          statisticTabs.get(tabIndex).init();
        }

      });

      layout.selectTab(0);
      layout.setAnimationEnabled(true);

      layout.addStyleName("wui-statistics");
    }
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().checkRole(this, callback);
  }

  public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.length == 0) {
      init();
      callback.onSuccess(layout);
    } else {
      History.newItem(getHistoryPath());
      callback.onSuccess(null);
    }
  }

  public String getHistoryPath() {
    return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
  }

  public String getHistoryToken() {
    return "statistics";
  }
}
