/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
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
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalHoldActions extends AbstractActionable<IndexedAIP> {
  private static final DisposalHoldActions INSTANCE = new DisposalHoldActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalHold disposalHold;

  private static final Set<DisposalHoldAction> POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD = new HashSet<>(
    Arrays.asList(DisposalHoldAction.DISASSOCIATE));

  public enum DisposalHoldAction implements Action<IndexedAIP> {
    DISASSOCIATE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);

    private List<String> methods;

    DisposalHoldAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  public DisposalHoldActions() {
  }

  public DisposalHoldActions(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
  }

  public static DisposalHoldActions get() {
    return INSTANCE;
  }

  @Override
  public Action<IndexedAIP>[] getActions() {
    return DisposalHoldActions.DisposalHoldAction.values();
  }

  @Override
  public Action<IndexedAIP> actionForName(String name) {
    return DisposalHoldActions.DisposalHoldAction.valueOf(name);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, ActionableObject<IndexedAIP> object) {
    if (object.getObject() != null || object.getObjects() != null) {
      return hasPermissions(action) && POSSIBLE_ACTIONS_ON_DISPOSAL_HOLD.contains(action);
    } else {
      return false;
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (DisposalHoldAction.DISASSOCIATE.equals(action)) {
      disassociate(objectToSelectedItems(aip, IndexedAIP.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    if (DisposalHoldAction.DISASSOCIATE.equals(action)) {
      disassociate(aips, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void disassociate(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.disassociateDisposalHoldDialogTitle(),
          messages.disassociateDisposalHoldDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                BrowserService.Util.getInstance().liftDisposalHold(aips, disposalHold.getId(),
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
    ActionableBundle<IndexedAIP> disposalHoldActionableBundle = new ActionableBundle<>();

    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.disassociateDisposalHoldButton(), DisposalHoldAction.DISASSOCIATE,
      ActionImpact.UPDATED, "btn-unlock");

    disposalHoldActionableBundle.addGroup(managementGroup);
    return disposalHoldActionableBundle;
  }

}
