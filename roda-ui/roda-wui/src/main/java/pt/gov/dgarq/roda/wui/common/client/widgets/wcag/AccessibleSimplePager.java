package pt.gov.dgarq.roda.wui.common.client.widgets.wcag;

import com.google.gwt.user.cellview.client.SimplePager;

public class AccessibleSimplePager extends SimplePager {

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, boolean showLastPageButton) {
    super(location, showFastForwardButton, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager() {
    super();
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location) {
    super(location);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

  public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows,
    boolean showLastPageButton) {
    super(location, showFastForwardButton, fastForwardRows, showLastPageButton);
    WCAGUtilities.getInstance().makeAccessible(this.getElement());
  }

}
