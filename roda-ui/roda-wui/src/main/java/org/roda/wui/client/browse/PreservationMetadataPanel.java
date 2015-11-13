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
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.wui.client.browse.TimelineInfo.HotZone;
import org.roda.wui.client.browse.TimelineInfo.Phase;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LoadingPopup;
import org.roda.wui.common.client.widgets.MessagePopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.netthreads.gwt.simile.timeline.client.BandInfo;
import com.netthreads.gwt.simile.timeline.client.BandOptions;
import com.netthreads.gwt.simile.timeline.client.DateTime;
import com.netthreads.gwt.simile.timeline.client.EventSource;
import com.netthreads.gwt.simile.timeline.client.HotZoneBandOptions;
import com.netthreads.gwt.simile.timeline.client.ITimeLineRender;
import com.netthreads.gwt.simile.timeline.client.SpanHighlightDecorator;
import com.netthreads.gwt.simile.timeline.client.SpanHighlightDecoratorOptions;
import com.netthreads.gwt.simile.timeline.client.Theme;
import com.netthreads.gwt.simile.timeline.client.TimeLineWidget;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;
import config.i18n.client.CommonConstants;

/**
 * @author Luis Faria
 * 
 */
public class PreservationMetadataPanel extends Composite {

  private static final String TIME_UNIT_DAY = "TIME_UNIT_DAY";
  private static final String TIME_UNIT_MONTH = "TIME_UNIT_MONTH";
  private static final String TIME_UNIT_YEAR = "TIME_UNIT_YEAR";

  private static final String[] TIMELINE_ICONS = new String[] {"js/api/images/dark-green-circle.png",
    "js/api/images/dark-blue-circle.png", "js/api/images/dark-red-circle.png", "js/api/images/gray-circle.png"};

  private static final String[] TIMELINE_COLORS = new String[] {"green", "blue", "red", "gray"};

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static CommonConstants commonConstants = (CommonConstants) GWT.create(CommonConstants.class);

  private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  private final SimpleDescriptionObject sdo;

  private final DockPanel layout;
  private HorizontalPanel header;
  private HorizontalPanel representations;
  private ListBox timeUnit;
  private TimeLineWidget timeline;
  private PreservationTimeLineRender timelineRender;
  private LoadingPopup loading;

  private TimelineInfo timelineInfo;

  private boolean initialized;

  /**
   * Create a new preservation metadata panel
   * 
   * @param sdo
   */
  public PreservationMetadataPanel(SimpleDescriptionObject sdo) {
    this.sdo = sdo;
    layout = new DockPanel();
    initWidget(layout);
    timelineInfo = null;
    initialized = false;

  }

  /**
   * Initialize preservation metadata panel
   */
  public void init() {
    if (!initialized) {
      initialized = true;

      loading = new LoadingPopup(this);
      loading.show();

      header = new HorizontalPanel();
      representations = new HorizontalPanel();
      timeUnit = new ListBox();
      timeUnit.setVisibleItemCount(1);
      timeUnit.addItem(constants.timeUnitDay(), TIME_UNIT_DAY);
      timeUnit.addItem(constants.timeUnitMonth(), TIME_UNIT_MONTH);
      timeUnit.addItem(constants.timeUnitYear(), TIME_UNIT_YEAR);
      timeUnit.setSelectedIndex(timeUnit.getItemCount() - 1);

      timeUnit.addChangeListener(new ChangeListener() {

        public void onChange(Widget sender) {
          String unit = timeUnit.getValue(timeUnit.getSelectedIndex());
          if (unit.equals(TIME_UNIT_DAY)) {
            timelineRender.setTimeUnit(DateTime.DAY(), DateTime.MONTH());
          } else if (unit.equals(TIME_UNIT_MONTH)) {
            timelineRender.setTimeUnit(DateTime.MONTH(), DateTime.YEAR());
          } else /* unit == TIME_UNIT_YEAR */ {
            timelineRender.setTimeUnit(DateTime.YEAR(), DateTime.DECADE());
          }
          update();
        }

      });

      BrowserService.Util.getInstance().getPreservationsInfo(sdo.getId(), new AsyncCallback<List<PreservationInfo>>() {

        public void onFailure(Throwable caught) {
          logger.error("Error getting representations info", caught);
          loading.hide();

        }

        public void onSuccess(List<PreservationInfo> infos) {
          int index = 0;
          List<String> icons = new ArrayList<String>();
          List<String> colors = new ArrayList<String>();

          for (PreservationInfo info : infos) {
            String icon = TIMELINE_ICONS[index % TIMELINE_ICONS.length];
            String color = TIMELINE_COLORS[index % TIMELINE_COLORS.length];

            HorizontalPanel repLayout = new HorizontalPanel();
            Image repIcon = new Image(GWT.getModuleBaseURL() + icon);
            Label repLabel = new Label();

            if (info.isOriginal()) {
              repLabel.setText(messages.preservationRepOriginal(info.getLabel()));
            } else if (info.isNormalized()) {
              repLabel.setText(messages.preservationRepNormalized(info.getLabel()));
            } else {
              repLabel.setText(messages.preservationRepAlternative(info.getLabel()));
            }

            repLabel.setTitle(messages.preservationRepTooltip(info.getNumberOfFiles(), info.getSizeOfFiles()));

            repLayout.add(repIcon);
            repLayout.add(repLabel);

            representations.add(repLayout);

            repLayout.addStyleName("preservation-rep-layout");
            repIcon.addStyleName("preservation-rep-icon");
            repLabel.addStyleName("preservation-rep-label");

            icons.add(icon);
            colors.add(color);
            index++;
          }
          loading.hide();
          initTimeline(infos, icons, colors);

        }

      });

      header.add(representations);
      header.add(timeUnit);
      layout.add(header, DockPanel.NORTH);

      header.setCellWidth(representations, "100%");
      layout.addStyleName("wui-metadata-preservation");
      header.addStyleName("wui-metadata-preservation-header");
      representations.addStyleName("wui-metadata-preservation-header-reps");
      timeUnit.addStyleName("wui-metadata-preservation-header-timeUnit");
    } else {
      if (timeline != null) {
        timeline.layout();
      }
    }
  }

  private void initTimeline(List<PreservationInfo> infos, List<String> icons, List<String> colors) {
    loading.show();
    List<String> rpoPIDs = new ArrayList<String>();
    for (PreservationInfo info : infos) {
      rpoPIDs.add(info.getRepresentationPreservationObjectPID());
    }
    BrowserService.Util.getInstance().getPreservationTimeline(rpoPIDs, icons, colors, commonConstants.locale(),
      new AsyncCallback<TimelineInfo>() {

        public void onFailure(Throwable caught) {
          logger.error("Error loading timeline info", caught);
          loading.hide();
        }

        public void onSuccess(TimelineInfo timelineInfo) {
          PreservationMetadataPanel.this.timelineInfo = timelineInfo;
          try {
            timelineRender = new PreservationTimeLineRender(timelineInfo, DateTime.YEAR(), DateTime.YEAR());
            update();
          } catch (Throwable e) {
            MessagePopup.showError(e.getClass().getName() + ": " + e.getMessage());
            logger.error("Error getting preservation timeline", e);
          } finally {
            loading.hide();
          }
        }

      });
  }

  protected void update() {
    if (timeline != null) {
      layout.remove(timeline);
    }
    timeline = new TimeLineWidget("99%", "330px", timelineRender);
    layout.add(timeline, DockPanel.CENTER);
    timeline.getEventSource().loadXMLText(timelineInfo.getEventsXML());
    timeline.addStyleName("wui-metadata-preservation-timeline");

  }

  private class PreservationTimeLineRender implements ITimeLineRender {

    private TimelineInfo timeLineInfo;

    private int primaryTimeUnit;

    private int pagerTimeUnit;

    /**
     * Create a new preservation time line renderer
     * 
     * @param timeLineInfo
     * @param primaryTimeUnit
     * @param pagerTimeUnit
     */
    public PreservationTimeLineRender(TimelineInfo timeLineInfo, int primaryTimeUnit, int pagerTimeUnit) {
      this.timeLineInfo = timeLineInfo;
      this.primaryTimeUnit = primaryTimeUnit;
      this.pagerTimeUnit = pagerTimeUnit;
    }

    /**
     * Get time line info
     * 
     * @return the time line info
     */
    public TimelineInfo getTimeLineInfo() {
      return timeLineInfo;
    }

    /**
     * Set time line info
     * 
     * @param timeLineInfo
     */
    public void setTimeLineInfo(TimelineInfo timeLineInfo) {
      this.timeLineInfo = timeLineInfo;
    }

    /**
     * Get primary time unit
     * 
     * @return the primary time unit
     */
    public int getPrimaryTimeUnit() {
      return primaryTimeUnit;
    }

    /**
     * Get pager time unit
     * 
     * @return the pager time unit
     */
    public int getPagerTimeUnit() {
      return pagerTimeUnit;
    }

    /**
     * Set time unit
     * 
     * @param primaryTimeUnit
     * @param pagerTimeUnit
     * 
     */
    public void setTimeUnit(int primaryTimeUnit, int pagerTimeUnit) {
      this.primaryTimeUnit = primaryTimeUnit;
      this.pagerTimeUnit = pagerTimeUnit;
    }

    @SuppressWarnings("unchecked")
    public void render(TimeLineWidget widget) {
      List<JavaScriptObject> bandInfos = widget.getBandInfos();
      List<HotZoneBandOptions> bandHotZones = widget.getBandHotZones();
      List<JavaScriptObject> bandDecorators = widget.getBandDecorators();
      EventSource eventSource = widget.getEventSource();

      Theme theme = widget.getTheme();
      theme.setEventLabelWidth(200);

      for (HotZone hotZone : timeLineInfo.getHotZones()) {
        HotZoneBandOptions hotZoneOpts = HotZoneBandOptions.create();
        hotZoneOpts.setStart(hotZone.getStart());
        hotZoneOpts.setEnd(hotZone.getEnd());
        hotZoneOpts.setMagnify(hotZone.getMagnification());
        hotZoneOpts.setUnit(primaryTimeUnit);
        hotZoneOpts.setMultiple(hotZone.getMultiple());
        bandHotZones.add(hotZoneOpts);

        // TODO create hot zones for pager
      }

      for (Phase phase : timeLineInfo.getPhases()) {
        SpanHighlightDecoratorOptions phaseOpts = SpanHighlightDecoratorOptions.create();
        phaseOpts.setStartDate(phase.getStart());
        phaseOpts.setEndDate(phase.getEnd());
        // TODO toggle color
        phaseOpts.setColor("FFC080");
        phaseOpts.setOpacity(50);
        phaseOpts.setStartLabel(phase.getLabel());
        phaseOpts.setEndLabel(phase.getLabel());
        phaseOpts.setTheme(theme);
        SpanHighlightDecorator phaseDecorator = SpanHighlightDecorator.create(phaseOpts);
        bandDecorators.add(phaseDecorator);

      }

      // Bands

      // Primary band
      BandOptions primaryOpts = BandOptions.create();
      primaryOpts.setWidth("80%");
      primaryOpts.setIntervalUnit(primaryTimeUnit);
      primaryOpts.setIntervalPixels(100);
      primaryOpts.setShowEventText(true);
      primaryOpts.setTheme(theme);
      primaryOpts.setEventSource(eventSource);
      primaryOpts.setDate(timeLineInfo.getDate());
      primaryOpts.setZones(bandHotZones);
      primaryOpts.setTimeZone(0);

      BandInfo primary = BandInfo.createHotZone(primaryOpts);
      primary.setDecorators(bandDecorators);
      bandInfos.add(primary);

      // Pager band
      BandOptions pagerOpts = BandOptions.create();
      pagerOpts.setWidth("20%");
      pagerOpts.setTrackHeight(0.5f);
      pagerOpts.setTrackGap(0.2f);
      pagerOpts.setIntervalUnit(pagerTimeUnit);
      pagerOpts.setIntervalPixels(250);
      pagerOpts.setShowEventText(false);
      pagerOpts.setTheme(theme);
      pagerOpts.setEventSource(eventSource);
      pagerOpts.setDate(timeLineInfo.getDate());

      BandInfo pager = BandInfo.create(pagerOpts);
      pager.setDecorators(bandDecorators);
      bandInfos.add(pager);

      pager.setSyncWith(0);
      pager.setHighlight(true);

    }

  }

}
