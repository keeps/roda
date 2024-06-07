package org.roda.core.data.v2.disposal.confirmation;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationCreateRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -3852472973580068030L;

  private String title;
  private SelectedItems<IndexedAIP> selectedItems;
  private DisposalConfirmationForm form;

  public DisposalConfirmationCreateRequest() {
    // empty constructor
  }

  public DisposalConfirmationCreateRequest(String title, SelectedItems<IndexedAIP> selectedItems,
                                           DisposalConfirmationForm form) {
    this.title = title;
    this.selectedItems = selectedItems;
    this.form = form;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public SelectedItems<IndexedAIP> getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItems<IndexedAIP> selectedItems) {
    this.selectedItems = selectedItems;
  }

  public DisposalConfirmationForm getForm() {
    return form;
  }

  public void setForm(DisposalConfirmationForm form) {
    this.form = form;
  }
}
