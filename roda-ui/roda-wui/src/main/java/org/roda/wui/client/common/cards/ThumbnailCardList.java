/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.cards;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.HTML;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.wui.client.common.cards.utils.CardBuilder;
import org.roda.wui.client.common.labels.Header;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ThumbnailCardList<T extends IsIndexed> extends Composite {
  public static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ThumbnailCardList.MyUiBinder uiBinder = GWT.create(ThumbnailCardList.MyUiBinder.class);

  @UiField
  Header title;

  @UiField
  FlowPanel cardsPanel;

  @UiField
  FlowPanel pagination;

  @UiField
  AccessibleFocusPanel prev;

  @UiField
  AccessibleFocusPanel next;

  @UiField
  HTML pageInfo;

  private static final ClientLogger LOGGER = new ClientLogger(ThumbnailCardList.class.getName());

  protected final ArrayList<ThumbnailCard> cards;
  private final FindRequest baseRequest;
  private final Class<T> objectClass;
  private IndexResult<T> results;
  private CardBuilder<T> cardBuilder;

  // pagination
  private int pageSize = 1;
  private int pageIndex = 0;
  private long total = -1;
  private boolean paged = false;

  protected ThumbnailCardList() {
    // UI Elements
    initUI();
    this.objectClass = null;
    this.results = null;
    this.baseRequest = null;
    this.cards = new ArrayList<>();
  }

  public ThumbnailCardList(String title, String icon, Class<T> objectClass, FindRequest findRequest,
    CardBuilder<T> cardBuilder) {
    // UI Elements
    initUI();
    setHeaderAndIcon(title, icon);

    // Data
    this.objectClass = objectClass;
    this.results = new IndexResult<>();
    this.baseRequest = findRequest;
    this.cardBuilder = cardBuilder;
    this.cards = new ArrayList<>();

  }

  public ThumbnailCardList(String title, String icon, Class<T> objectClass, Filter resultsFilter,
    CardBuilder<T> cardBuilder, Sorter sorter) {
    // UI Elements
    initUI();
    setHeaderAndIcon(title, icon);

    // Data
    this.objectClass = objectClass;
    this.results = new IndexResult<>();
    this.baseRequest = FindRequest.getBuilder(resultsFilter, true).withSorter(sorter).build();
    this.cardBuilder = cardBuilder;
    this.cards = new ArrayList<>();

  }

  private void initUI() {
    initWidget(uiBinder.createAndBindUi(this));
    this.pagination.setVisible(false);

    this.prev.addClickHandler(e -> goToPage(this.pageIndex - 1));
    this.next.addClickHandler(e -> goToPage(this.pageIndex + 1));
  }

  public void refresh() {
    clearCards();
    updateResultsAndRebuildCards();
  }

  private void clearCards() {
    this.cardsPanel.clear();
  }

  private void setHeaderAndIcon(String title, String icon) {
    this.title.setHeaderStyleName("noMargin");
    this.title.setHeaderText(title);
    this.title.setLevel(5);
    this.title.setIcon(icon);
  }

  private void updateResultsAndRebuildCards() {
    this.cards.clear();
    this.cardsPanel.clear();
    Services services = new Services("Retrieve AIP representations or dissemination", "get");
    services.rodaEntityRestService(s -> s.find(buildFindRequest(), LocaleInfo.getCurrentLocale().getLocaleName()),
      objectClass).whenComplete((requestResults, error) -> {
        if (error == null) {
          this.results = requestResults;
          for (T object : this.results.getResults()) {
            ThumbnailCard card = cardBuilder.constructCard(messages, object);
            if (this.cards.isEmpty()) {
              card.expand();
            }
            this.cards.add(card);
            this.cardsPanel.add(card);
          }
        } else {
          this.results = new IndexResult<>();
          LOGGER.error("Error retrieving objects for thumbnail card list: ", error);
        }
      });
  }

  // pagination
  private FindRequest buildFindRequest() {
    FindRequest.FindRequestBuilder fr = FindRequest
      .getBuilder(this.baseRequest.getFilter(), this.baseRequest.isOnlyActive())
      .withSorter(this.baseRequest.getSorter());
    if (this.paged)
      fr.withSublist(new Sublist(this.pageIndex * this.pageSize, this.pageSize));
    else if (this.baseRequest.getSublist() != null) {
      fr.withSublist(this.baseRequest.getSublist());
    }
    return fr.build();
  }

  private void goToPage(int newIndex) {
    if (!this.paged)
      return;
    int maxPage = (int) Math.max(0, (long) Math.ceil((double) total / this.pageSize) - 1);
    this.pageIndex = Math.max(0, Math.min(newIndex, maxPage));
    updatePaginationState();
    refresh();
  }

  private void updatePaginationState() {
    if (!this.paged)
      return;

    int totalPages = (int) Math.max(1, Math.ceil((double) this.total / this.pageSize));
    this.pageInfo.setHTML("<span>" + (this.pageIndex + 1) + "</span> / <span>" + totalPages + "</span>");

    boolean showPrev = this.pageIndex > 0;
    boolean showNext = this.pageIndex < totalPages - 1;
    setHidden(this.prev, !showPrev);
    setHidden(this.next, !showNext);
  }

  public void withPagination(int pageSize, long total) {
    this.pageSize = Math.max(1, pageSize);
    this.total = total;
    this.pageIndex = 0;
    if (total <= this.pageSize) {
      this.paged = false;
      this.pagination.setVisible(false);
      this.pageInfo.setHTML("");
      setHidden(prev, true);
      setHidden(next, true);
      return;
    }
    this.paged = true;
    this.pagination.setVisible(true);

    updatePaginationState();
  }

  private void setHidden(Widget w, boolean hidden) {
    if (hidden) {
      w.addStyleName("isHidden");
    } else
      w.removeStyleName("isHidden");
  }

  interface MyUiBinder extends UiBinder<Widget, ThumbnailCardList<? extends IsIndexed>> {
  }
}
