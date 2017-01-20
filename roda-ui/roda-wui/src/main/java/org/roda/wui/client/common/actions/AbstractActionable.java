package org.roda.wui.client.common.actions;

import java.util.Arrays;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractActionable<T extends IsIndexed> implements Actionable<T> {

  protected static AsyncCallback<Void> createDefaultAsyncCallback() {
    return new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    };
  }

  protected SelectedItemsList<T> objectToSelectedItems(T object) {
    return new SelectedItemsList<T>(Arrays.asList(object.getUUID()), object.getClass().getName());
  }

  @Override
  public void act(Actionable.Action<T> action, T object) {
    act(action, object, createDefaultAsyncCallback());
  }

  @Override
  public boolean canAct(Actionable.Action<T> action, T object) {
    return canAct(action, objectToSelectedItems(object));
  }

  @Override
  public void act(Actionable.Action<T> action, T object, AsyncCallback<Void> callback) {
    act(action, objectToSelectedItems(object), callback);
  }

  @Override
  public void act(Actionable.Action<T> action, SelectedItems<T> objects) {
    act(action, objects, createDefaultAsyncCallback());
  }

  public FlowPanel createLayout() {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("actions-layout");
    return layout;
  }

  public void addTitle(FlowPanel layout, String text) {
    Label title = new Label(text);
    title.addStyleName("actions-layout-title");
    layout.add(title);
  }

  public void addButton(FlowPanel layout, final String text, final Actionable.Action<T> action, final T object,
    final AsyncCallback<Void> callback, final String... extraCssClasses) {

    if (canAct(action, object)) {

      // Construct
      Button button = new Button(text);
      button.setTitle(text);

      // CSS
      button.addStyleName("actions-layout-button");
      button.addStyleName("btn");
      button.addStyleName("btn-block");

      for (String extraCssClass : extraCssClasses) {
        button.addStyleName(extraCssClass);
      }

      // Action
      button.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          act(action, object, callback);
        }
      });

      layout.add(button);
    }
  }

  public void addButton(FlowPanel layout, final String text, final Actionable.Action<T> action,
    final SelectedItems<T> objects, final AsyncCallback<Void> callback, final String... extraCssClasses) {

    if (canAct(action, objects)) {

      // Construct
      Button button = new Button(text);
      button.setTitle(text);

      // CSS
      button.addStyleName("actions-layout-button");
      button.addStyleName("btn");
      button.addStyleName("btn-block");

      for (String extraCssClass : extraCssClasses) {
        button.addStyleName(extraCssClass);
      }

      // Action
      button.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          act(action, objects, callback);
        }
      });

      layout.add(button);
    }
  }

  @Override
  public Widget createActionsLayout(T object, AsyncCallback<Void> callback) {
    return createActionsLayout(objectToSelectedItems(object), callback);
  }

  public Widget createActionsLayout(T object) {
    return createActionsLayout(object, createDefaultAsyncCallback());
  }

}
