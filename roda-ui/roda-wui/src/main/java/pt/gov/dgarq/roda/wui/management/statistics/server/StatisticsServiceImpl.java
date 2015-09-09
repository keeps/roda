package pt.gov.dgarq.roda.wui.management.statistics.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.StatisticsListReportMessages;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.management.statistics.client.Segmentation;
import pt.gov.dgarq.roda.wui.management.statistics.client.StatisticFunction;
import pt.gov.dgarq.roda.wui.management.statistics.client.StatisticsService;

/**
 * Statistics Service Implementation
 * 
 * @author Luis Faria
 * 
 */
public class StatisticsServiceImpl extends RemoteServiceServlet implements StatisticsService {

  private static final long serialVersionUID = 5532120366373431284L;
  private static final Logger logger = Logger.getLogger(StatisticsServiceImpl.class);

  private final StatisticsHelper helper = new StatisticsHelper();

  public int getStatisticCount(Filter filter) throws RODAException {
    return getStatisticCount(filter, getThreadLocalRequest().getSession());
  }

  public int getStatisticCount(Filter filter, HttpSession session) throws RODAException {
    // StatisticsMonitor statistics =
    // RodaClientFactory.getRodaClient(session).getStatisticsMonitorService();
    int count;
    // try {
    // TODO move to new implementation
    // count = statistics.getStatisticDataCount(filter);
    count = 0;
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }

    return count;
  }

  public List<StatisticData> getStatisticList(ContentAdapter adapter) throws RODAException {
    return getStatisticList(adapter, getThreadLocalRequest().getSession());
  }

  public List<StatisticData> getStatisticList(ContentAdapter adapter, HttpSession session) throws RODAException {
    // StatisticsMonitor statistics =
    // RodaClientFactory.getRodaClient(session).getStatisticsMonitorService();

    StatisticData[] data;
    // try {
    // TODO move to new implementation
    // data = statistics.getStatisticData(adapter);
    data = null;
    if (data == null) {
      data = new StatisticData[] {};
    }
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return Arrays.asList(data);
  }

  private void addDateConstraints(ContentAdapter adapter, Date initialDate, Date finalDate) {
    // Put final date time at the last millisecond of the day
    finalDate = new Date(finalDate.getTime() + 86399999);

    adapter.getFilter().add(new DateRangeFilterParameter("timestamp", initialDate, finalDate));

    adapter.setSorter(new Sorter(new SortParameter[] {new SortParameter("timestamp", false)}));
  }

  public List<StatisticData> getStatisticList(ContentAdapter adapter, List<StatisticFunction> functions,
    Segmentation segmentation, Date initialDate, Date finalDate) throws RODAException {
    addDateConstraints(adapter, initialDate, finalDate);
    List<StatisticData> statisticList = getStatisticList(adapter);

    List<StatisticData> ret = helper.applyFunctions(statisticList, functions, segmentation, initialDate, finalDate);

    logger
      .debug("adapter: " + adapter + "\nfunctions: " + functions + "\nentries: " + statisticList + "\nlist: " + ret);

    return ret;
  }

  public List<List<StatisticData>> getStatisticStackedList(ContentAdapter adapter, List<StatisticFunction> functions,
    Segmentation segmentation, Date initialDate, Date finalDate) throws RODAException {
    addDateConstraints(adapter, initialDate, finalDate);
    List<StatisticData> entries = getStatisticList(adapter);
    List<List<StatisticData>> groups = helper.groupByType(entries);
    List<List<StatisticData>> ret = new ArrayList<List<StatisticData>>();

    for (List<StatisticData> group : groups) {
      ret.add(helper.applyFunctions(group, functions, segmentation, initialDate, finalDate));
    }

    logger.debug("adapter: " + adapter + "\nfunctions: " + functions + "\nentries: " + entries + "\ngroups: " + groups
      + "\nret: " + ret);

    return ret;
  }

  public void setStatisticListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException {
    final Locale locale = ServerTools.parseLocale(localeString);
    final StatisticsListReportMessages messages = new StatisticsListReportMessages(locale);
    // TODO move to new implementation
    // ReportDownload.getInstance().createPDFReport(getThreadLocalRequest().getSession(),
    // new ReportContentSource<StatisticData>() {
    //
    // public int getCount(HttpSession session, Filter filter) throws
    // Exception {
    // return getStatisticCount(filter, session);
    // }
    //
    // public StatisticData[] getElements(HttpSession session,
    // ContentAdapter adapter) throws Exception {
    // return getStatisticList(adapter, session).toArray(new StatisticData[]
    // {});
    // }
    //
    // public Map<String, String> getElementFields(HttpServletRequest req,
    // StatisticData data) {
    // return StatisticsServiceImpl.this.getElementFields(req, data,
    // messages);
    // }
    //
    // public String getElementId(StatisticData data) {
    // return String.format(messages.getString("statisticData.title"),
    // data.getType());
    //
    // }
    //
    // public String getReportTitle() {
    // return messages.getString("report.title");
    // }
    //
    // public String getFieldNameTranslation(String name) {
    // String translation;
    // try {
    // translation = messages.getString("statisticData.label." + name);
    // } catch (MissingResourceException e) {
    // translation = name;
    // }
    //
    // return translation;
    // }
    //
    // public String getFieldValueTranslation(String value) {
    // String translation;
    // try {
    // translation = messages.getString("statisticData.value." + value);
    // } catch (MissingResourceException e) {
    // translation = value;
    // }
    //
    // return translation;
    // }
    //
    // }, adapter);
  }

  protected Map<String, String> getElementFields(HttpServletRequest req, StatisticData data,
    StatisticsListReportMessages messages) {
    Map<String, String> ret = new LinkedHashMap<String, String>();
    ret.put(messages.getString("statisticData.label.timestamp"), DateParser.getIsoDate(data.getTimestamp()));
    ret.put(messages.getString("statisticData.label.value"), data.getValue());
    return ret;
  }

}
