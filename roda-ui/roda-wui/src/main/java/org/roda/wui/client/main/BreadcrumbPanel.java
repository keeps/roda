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
package org.roda.wui.client.main;

import java.util.LinkedList;
import java.util.List;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.LoginStatusListener;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class BreadcrumbPanel extends FlowPanel {

  private List<BreadcrumbItem> currentpath;
  private final LinkedList<Breadcrumb> breadcrumbs;

  /**
   * Create a new Breadcrumb panel
   * 
   * @param contentPanel
   *          the content panel this breadcrumb panel will manage
   */
  public BreadcrumbPanel() {
    super();
    this.breadcrumbs = new LinkedList<>();
    this.currentpath = null;

    UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {
      @Override
      public void onLoginStatusChanged(User user) {
        updatePath(currentpath);
      }
    });

    addStyleName("wui-breadcrumbPanel");
  }

  protected Widget createSeparator() {
    final HTML separator = new HTML("&nbsp;/&nbsp;");
    separator.setStyleName("breadcrumb-separator");
    return separator;
  }

  /**
   * Update the breadcrumb panel
   * 
   * @param path
   *          the new history path
   * 
   */
  public void updatePath(List<BreadcrumbItem> path) {

    breadcrumbs.clear();
    for (final BreadcrumbItem item : path) {
      Breadcrumb breadcrumb = new Breadcrumb(item);
      breadcrumb.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          item.getCommand().execute();
        }
      });
      breadcrumbs.add(breadcrumb);
    }
    updateLayout();

    currentpath = path;
  }

  @Override
  public void clear() {
    breadcrumbs.clear();
    updateLayout();
  }

  protected void updateLayout() {
    super.clear();
    for (int i = 0; i < breadcrumbs.size(); i++) {
      if (i > 0) {
        add(createSeparator());
      }
      Breadcrumb breadcrumb = breadcrumbs.get(i);
      breadcrumb.setLast(i == breadcrumbs.size() - 1);
      add(breadcrumb);
    }

  }

  protected class Breadcrumb extends HTML {

    private BreadcrumbItem item;

    private boolean enabled;

    private boolean last;

    /**
     * Create a new breadcrumb
     * 
     * @param path
     *          the history path that this breadcrumb points to
     */
    public Breadcrumb(final BreadcrumbItem item) {
      super();
      setHTML(item.getLabel());
      setTitle(item.getTitle());

      this.item = item;
      enabled = true;
      last = true;

      addStyleName("breadcrumb");
    }

    /**
     * Is this breadcrumb enabled
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Set if this breadcrumb is enabled. Overrides the click event
     * 
     * @param enabled
     * 
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Is this the last breadcrumb in list (current active breadcrumb)
     * 
     * @return true if this breadcrumb is the last one. The last breadcrumb is
     *         disabled and has a different style.
     */
    public boolean isLast() {
      return last;
    }

    /**
     * Get the history path defined by this breadcrumb
     * 
     * @return the breadcrumb history path
     */
    public BreadcrumbItem getItem() {
      return item;
    }

    /**
     * Set if this breadcrumb is the last one. The last breadcrumb will be
     * disabled and has a different style.
     * 
     * @param last
     */
    public void setLast(boolean last) {
      this.last = last;
      if (last) {
        this.addStyleName("breadcrumb-last");
      } else {
        this.removeStyleName("breadcrumb-last");
      }
    }

    @Override
    public void onBrowserEvent(final Event event) {
      if (enabled) {
        super.onBrowserEvent(event);
      }
    }

    protected String getTargetHistoryToken(String[] path) {
      return StringUtils.join(path, ".");
    }

  }
}
