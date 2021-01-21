package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalScheduleActions extends AbstractActionable<IndexedAIP> {
  public static final String NO_DISPOSAL_SCHEDULE_ID = null;

  private static final DisposalScheduleActions INSTANCE = new DisposalScheduleActions(NO_DISPOSAL_SCHEDULE_ID);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String disposalScheduleId;

  private DisposalScheduleActions(String disposalScheduleId) {
    this.disposalScheduleId = disposalScheduleId;
  }

  private static final Set<DisposalScheduleAction> POSSIBLE_ACTIONS_ON_DISPOSAL_SCHEDULE = new HashSet<>(
    Collections.singletonList(DisposalScheduleAction.DISASSOCIATE));

  public enum DisposalScheduleAction implements Action<IndexedAIP> {
    DISASSOCIATE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE);

    private List<String> methods;

    DisposalScheduleAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public Action<IndexedAIP>[] getActions() {
    return DisposalScheduleAction.values();
  }

  public static DisposalScheduleActions get() {
    return INSTANCE;
  }

  public static DisposalScheduleActions get(String disposalScheduleId) {
    return new DisposalScheduleActions(disposalScheduleId);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    if (object.getObject() != null || object.getObjects() != null) {
      return hasPermissions(action) && POSSIBLE_ACTIONS_ON_DISPOSAL_SCHEDULE.contains(action);
    } else {
      return false;
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (DisposalScheduleAction.DISASSOCIATE.equals(action)) {
      disassociate(objectToSelectedItems(aip, IndexedAIP.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> items, AsyncCallback<ActionImpact> callback) {
    if (DisposalScheduleAction.DISASSOCIATE.equals(action)) {
      disassociate(items, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void disassociate(SelectedItems<IndexedAIP> items, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, items, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.disassociateDisposalScheduleDialogTitle(),
          messages.disassociateDisposalScheduleDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                BrowserService.Util.getInstance().disassociateDisposalSchedule(items,
                  new ActionAsyncCallback<Job>(callback) {
                    @Override
                    public void onFailure(Throwable caught) {
                      callback.onFailure(caught);
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }

                    @Override
                    public void onSuccess(Job job) {
                      Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Timer timer = new Timer() {
                            @Override
                            public void run() {
                              doActionCallbackUpdated();
                            }
                          };

                          timer.schedule(RodaConstants.ACTION_TIMEOUT);
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                        }
                      });
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> disposalScheduleActionableBundle = new ActionableBundle<>();

    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.disassociateDisposalScheduleButton(), DisposalScheduleAction.DISASSOCIATE,
      ActionImpact.UPDATED, "fas fa-calendar");

    disposalScheduleActionableBundle.addGroup(managementGroup);
    return disposalScheduleActionableBundle;
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return null;
  }
}
