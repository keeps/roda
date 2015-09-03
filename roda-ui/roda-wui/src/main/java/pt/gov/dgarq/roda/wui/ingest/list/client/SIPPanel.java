package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.SuccessListener;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.UserInfoPanel;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.ingest.list.client.SelectDescriptionObjectWindow.SelectDescriptionObjectListener;
import pt.gov.dgarq.roda.wui.management.user.client.UserManagementService;

/**
 * 
 * @author Luis Faria
 * 
 */
public class SIPPanel extends ElementPanel<SIPState> {

  private static IngestListConstants constants = (IngestListConstants) GWT.create(IngestListConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final HorizontalPanel layout;

  private final Label filename;

  private final Label startdate;

  private final Label state;

  private final Label percentage;

  private final Label producer;

  private PopupPanel producerInfoPopup;

  private boolean showProducerInfoPopup;

  private Timer scheduleProducerInfoPopupShow;

  private Timer scheduleProducerInfoPopupHide;

  private static final int PRODUCER_INFO_POPUP_SHOW_DELAY_MS = 1000;

  private static final int PRODUCER_INFO_POPUP_HIDE_DELAY_MS = 500;

  private final HorizontalPanel toolbar;

  private final Image report;

  private final Image view;

  private IngestReportWindow reportWindow;

  // private ViewWindow viewWindow;

  /**
   * Create a new SIP Panel
   * 
   * @param sip
   */
  public SIPPanel(SIPState sip) {
    super(sip);

    layout = new HorizontalPanel();
    filename = new Label();
    startdate = new Label();
    state = new Label();
    percentage = new Label();
    producer = new Label();

    toolbar = new HorizontalPanel();
    report = commonImageBundle.report().createImage();
    view = commonImageBundle.info().createImage();

    reportWindow = null;
    producerInfoPopup = new PopupPanel();
    showProducerInfoPopup = false;

    scheduleProducerInfoPopupShow = new Timer() {
      public void run() {
        producer.addStyleName("wui-ingest-list-producer-loading");
        initProducerInfoPopup(new AsyncCallback<PopupPanel>() {

          public void onFailure(Throwable caught) {
            logger.warn("Producer popup cannot be shown", caught);
          }

          public void onSuccess(PopupPanel result) {
            producer.removeStyleName("wui-ingest-list-producer-loading");
            if (showProducerInfoPopup) {
              producerInfoPopup.setPopupPositionAndShow(new PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                  producerInfoPopup.setPopupPosition(producer.getAbsoluteLeft(),
                    producer.getAbsoluteTop() - offsetHeight);
                }

              });
            }

          }

        });
      }
    };

    scheduleProducerInfoPopupHide = new Timer() {

      public void run() {
        if (!showProducerInfoPopup) {
          producerInfoPopup.hide();
        }
      }

    };

    report.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        setReportVisible(true);
      }
    });
    view.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        view();
      }
    });

    producer.addMouseListener(new MouseListener() {

      public void onMouseDown(Widget sender, int x, int y) {
      }

      public void onMouseEnter(Widget sender) {
        showProducerInfoPopup = true;
        scheduleProducerInfoPopupHide.cancel();
        scheduleProducerInfoPopupShow.schedule(PRODUCER_INFO_POPUP_SHOW_DELAY_MS);
      }

      public void onMouseLeave(Widget sender) {
        showProducerInfoPopup = false;
        scheduleProducerInfoPopupShow.cancel();
        scheduleProducerInfoPopupHide.schedule(PRODUCER_INFO_POPUP_HIDE_DELAY_MS);

      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
      }

    });

    layout.add(filename);
    layout.add(startdate);
    layout.add(state);
    layout.add(percentage);
    layout.add(producer);
    layout.add(toolbar);

    update(sip);

    this.setStylePrimaryName("wui-ingest-sip");
    setSelected(false);
    this.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        setSelected(!isSelected());
      }

    });

    layout.setCellWidth(toolbar, "100%");
    layout.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
    layout.addStyleName("sip-layout");
    filename.addStyleName("sip-filename");
    startdate.addStyleName("sip-date");
    state.addStyleName("sip-state");
    percentage.addStyleName("sip-percentage");
    producer.addStyleName("sip-producer");
    toolbar.addStyleName("sip-toolbar");
    report.addStyleName("sip-toolbar-report");
    view.addStyleName("sip-toolbar-view");

    this.setWidget(layout);
  }

  @Override
  protected void update(SIPState sip) {
    filename.setText(sip.getOriginalFilename());
    filename.setTitle(sip.getOriginalFilename());
    startdate.setText(Tools.formatDateTime(sip.getStateTransitions()[0].getDatetime()));
    try {
      state.setText(constants.getString("state_" + sip.getState()));
    } catch (MissingResourceException e) {
      state.setText(sip.getState());
    }

    percentage.setText(sip.getCompletePercentage() + "%");
    producer.setText(sip.getUsername());

    toolbar.add(report);
    if (sip.getIngestedPID() != null) {
      toolbar.add(view);
    }
  }

  /**
   * Set report panel visible
   * 
   * @param visible
   */
  public void setReportVisible(boolean visible) {
    if (visible) {
      if (reportWindow == null) {
        reportWindow = new IngestReportWindow(get());
      }
      reportWindow.show();
    } else if (reportWindow != null) {
      reportWindow.hide();
    }
  }

  /**
   * Open view window
   */
  public void view() {
    String pid = get().getIngestedPID();
    if (pid != null) {
      BrowserService.Util.getInstance().getSimpleDescriptionObject(pid, new AsyncCallback<SimpleDescriptionObject>() {

        public void onFailure(Throwable caught) {
          logger.error("Error viewing SIP ingested pid", caught);

        }

        public void onSuccess(SimpleDescriptionObject sdo) {
          if (sdo.getSubElementsCount() == 0) {
            // view(sdo);
          } else {
            SelectDescriptionObjectWindow selectWindow = new SelectDescriptionObjectWindow(sdo);
            selectWindow.addSelectDescriptionObjectListener(new SelectDescriptionObjectListener() {

              public void onSelect(SimpleDescriptionObject sdo) {
                // view(sdo);
              }
            });
            selectWindow.show();
          }

        }

      });

    }
  }

  // private void view(SimpleDescriptionObject sdo) {
  // viewWindow = new ViewWindow(sdo.getId(), new
  // AsyncCallback<DescriptionObject>() {
  //
  // public void onFailure(Throwable caught) {
  // if (caught instanceof NoSuchRODAObjectException) {
  // Window.alert(constants.objectNotInRepository());
  // } else {
  // logger.error("Error creating view window", caught);
  // }
  //
  // }
  //
  // public void onSuccess(DescriptionObject obj) {
  // viewWindow.show();
  // }
  //
  // });
  // }

  private void initProducerInfoPopup(final AsyncCallback<PopupPanel> callback) {
    if (producerInfoPopup.getWidget() == null) {
      UserManagementService.Util.getInstance().getUser(get().getUsername(), new AsyncCallback<User>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(User user) {
          UserInfoPanel userInfo = new UserInfoPanel(user);
          FocusPanel focus = new FocusPanel(userInfo.getWidget());
          producerInfoPopup.setWidget(focus);
          focus.addMouseListener(new MouseListener() {

            public void onMouseDown(Widget sender, int x, int y) {
            }

            public void onMouseEnter(Widget sender) {
              showProducerInfoPopup = true;
              scheduleProducerInfoPopupHide.cancel();
            }

            public void onMouseLeave(Widget sender) {
              showProducerInfoPopup = false;
              scheduleProducerInfoPopupHide.schedule(PRODUCER_INFO_POPUP_HIDE_DELAY_MS);
            }

            public void onMouseMove(Widget sender, int x, int y) {

            }

            public void onMouseUp(Widget sender, int x, int y) {

            }

          });
          focus.addStyleName("wui-ingest-list-producer-popup");
          callback.onSuccess(producerInfoPopup);
        }

      });
    } else {
      callback.onSuccess(producerInfoPopup);
    }
  }

  /**
   * Accept / publish SIP
   * 
   * @param callback
   */
  public void accept(final AsyncCallback<?> callback) {
    AcceptMessageWindow acceptMessageWindow = new AcceptMessageWindow(get());
    acceptMessageWindow.addSuccessListener(new SuccessListener() {

      public void onCancel() {
        callback.onSuccess(null);
      }

      public void onSuccess() {
        callback.onSuccess(null);
      }

    });
    acceptMessageWindow.show();
  }

  /**
   * Reject SIP
   * 
   * @param published
   * @param callback
   */
  public void reject(final AsyncCallback<?> callback) {
    RejectMessageWindow rejectMessageWindow = new RejectMessageWindow(get());
    rejectMessageWindow.addSuccessListener(new SuccessListener() {

      public void onCancel() {
        callback.onSuccess(null);
      }

      public void onSuccess() {
        callback.onSuccess(null);
      }

    });
    rejectMessageWindow.show();
  }

  @Override
  public void set(SIPState updatedSIP) {
    try {
      if (get().hasChanges(updatedSIP)) {
        super.set(updatedSIP);
        update(updatedSIP);
        if (reportWindow != null) {
          reportWindow.update(get());
        }
      }
    } catch (IllegalOperationException e) {
      logger.error("Tried to update SIP panel with different sip id");
    }
  }

}
