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
package org.roda.wui.management.editor.client;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.IndexedAIP;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import config.i18n.client.BrowseMessages;
import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class MoveChooseDestinationPanel extends WUIWindow {
  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final IndexedAIP source;

  // private final CollectionsTreeVerticalScrollPanel collectionsTree;

  private final WUIButton choose;

  private final WUIButton cancel;

  // private final LoadingPopup loading;

  /**
   * Listener for move events
   * 
   */
  public interface MoveListener {
    /**
     * On object move
     * 
     * @param oldParent
     *          PID of the parent object which the object is moving from
     * 
     * @param newParentPid
     *          PID of the parent object under which the object is moving to
     */
    public void onMove(String oldParent, String newParentPid);

    /**
     * On move cancellation
     */
    public void onCancel();
  }

  private List<MoveListener> listeners;

  /**
   * Create a new panel to choose the destination where to move the object
   * 
   * @param aip
   *          the simple description object to move
   */
  public MoveChooseDestinationPanel(IndexedAIP aip) {
    super(650, 550);
    this.source = aip;
    setTitle(constants.moveChooseDestinationTitle());
    // collectionsTree = new CollectionsTreeVerticalScrollPanel(false);
    // setWidget(collectionsTree);
    //
    // collectionsTree.setSelected(source.getId());

    listeners = new ArrayList<MoveListener>();

    choose = new WUIButton(constants.moveChooseDestinationChoose(), WUIButton.Left.ROUND,
      WUIButton.Right.ARROW_FORWARD);

    choose.setEnabled(false);

    // choose.addClickHandler(new ClickHandler() {
    //
    // public void onClick(ClickEvent event) {
    // final CollectionsTreeItem selected = collectionsTree.getSelected();
    // loading.show();
    // moveTo(selected.getSDO(), new AsyncCallback<CollectionsTreeItem>() {
    //
    // public void onFailure(Throwable caught) {
    // if (caught instanceof IllegalOperationException) {
    // Window.alert(messages.moveIllegalOperation(caught.getMessage()));
    // } else if (caught instanceof NoSuchRODAObjectException) {
    // Window.alert(messages.moveNoSuchObject(caught.getMessage()));
    // } else {
    // logger.error("Error while moving " + source.getId() + " to " +
    // selected.getPid());
    // }
    // loading.hide();
    // hide();
    // }
    //
    // public void onSuccess(CollectionsTreeItem treeItem) {
    // loading.hide();
    // hide();
    // }
    //
    // });
    //
    // }
    //
    // });

    cancel = new WUIButton(constants.moveChooseDestinationCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    cancel.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        hide();
        onCancel();
      }

    });

    addToBottom(choose);
    addToBottom(cancel);

    // collectionsTree.addClickListener(new ClickListener() {
    //
    // public void onClick(Widget sender) {
    // final CollectionsTreeItem selected = collectionsTree.getSelected();
    // choose.setEnabled(isMoveValid(source, selected.getSDO()));
    //
    // }
    //
    // });

    // loading = new LoadingPopup(collectionsTree);
    //
    // collectionsTree.addStyleName("wui-edit-move-collections");

  }

  protected boolean isMoveValid(IndexedAIP source, IndexedAIP target) {
    // FIXME this is not valid anymore (see if "target" level can have a
    // child with the "source" level). At the very best, if the move is
    // illegal, roda-core will notice it as it validates the changes to the
    // description level, so returning true here isn't that much problematic
    // return source.getLevel().compareTo(target.getLevel()) > 0;
    return true;
  }

  // protected void moveTo(final SimpleDescriptionObject newParent, final
  // AsyncCallback<CollectionsTreeItem> callback) {
  // BrowserService.Util.getInstance().getParent(source.getId(), new
  // AsyncCallback<String>() {
  //
  // public void onFailure(Throwable caught) {
  // callback.onFailure(caught);
  // }
  //
  // public void onSuccess(final String oldParentPID) {
  // EditorService.Util.getInstance().moveElement(source.getId(),
  // newParent.getId(),
  // new AsyncCallback<Void>() {
  //
  // public void onFailure(Throwable caught) {
  // callback.onFailure(caught);
  // }
  //
  // public void onSuccess(Void result) {
  // onMove(oldParentPID, newParent.getId());
  // callback.onSuccess(null);
  // }
  //
  // });
  //
  // }
  //
  // }

  /**
   * Add move listener
   * 
   * @param listener
   */
  public void addMoveListener(MoveListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove move listener
   * 
   * @param listener
   */
  public void removeMoveListener(MoveListener listener) {
    listeners.remove(listener);
  }

  protected void onMove(String oldParent, String newParent) {
    for (MoveListener listener : listeners) {
      listener.onMove(oldParent, newParent);
    }
  }

  protected void onCancel() {
    for (MoveListener listener : listeners) {
      listener.onCancel();
    }
  }
}
