package org.roda.core.data.v2.disposal.rule;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class ChangeOrderRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -1604170151297177958L;

  private SelectedItemsRequest items;
  private OrderPositions position;
  private int newOrder;

  public ChangeOrderRequest() {
    position = OrderPositions.TOP;
  }

  public ChangeOrderRequest(SelectedItemsRequest items, OrderPositions position, int newOrder) {
    this.items = items;
    this.position = position;
    this.newOrder = newOrder;
  }

  public SelectedItemsRequest getItems() {
    return items;
  }

  public void setItems(SelectedItemsRequest items) {
    this.items = items;
  }

  public int getNewOrder() {
    return newOrder;
  }

  public void setNewOrder(int newOrder) {
    this.newOrder = newOrder;
  }

  public OrderPositions getPosition() {
    return position;
  }

  public void setPosition(OrderPositions position) {
    this.position = position;
  }
}
