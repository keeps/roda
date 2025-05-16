package org.roda.wui.client.common.cards;

import java.util.ArrayList;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
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
  FlowPanel buttons;

  private static final ClientLogger LOGGER = new ClientLogger(ThumbnailCardList.class.getName());

  protected final ArrayList<ThumbnailCard> cards;
  private final FindRequest findRequest;
  private final Class<T> objectClass;
  private IndexResult<T> results;
  private Filter resultsFilter;
  private CardBuilder<T> cardBuilder;

  protected ThumbnailCardList() {
    // UI Elements
    initWidget(uiBinder.createAndBindUi(this));
    this.objectClass = null;
    this.results = null;
    this.resultsFilter = null;
    this.findRequest = null;
    this.cards = new ArrayList<>();
  }

  public ThumbnailCardList(String title, String icon, Class<T> objectClass, FindRequest findRequest,
    CardBuilder<T> cardBuilder) {
    // UI Elements
    initWidget(uiBinder.createAndBindUi(this));
    this.title.setHeaderStyleName("noMargin");
    this.title.setHeaderText(title);
    this.title.setLevel(5);
    this.title.setIcon(icon);

    // Data
    this.objectClass = objectClass;
    this.results = new IndexResult<>();
    this.findRequest = findRequest;
    this.cardBuilder = cardBuilder;
    this.cards = new ArrayList<>();

    // Initialize
    refresh();
  }

  public ThumbnailCardList(String title, String icon, Class<T> objectClass, Filter resultsFilter,
    CardBuilder<T> cardBuilder) {
    // UI Elements
    initWidget(uiBinder.createAndBindUi(this));
    this.title.setHeaderStyleName("noMargin");
    this.title.setHeaderText(title);
    this.title.setLevel(5);
    this.title.setIcon(icon);

    // Data
    this.objectClass = objectClass;
    this.results = new IndexResult<>();
    if (resultsFilter != null) {
      this.resultsFilter = resultsFilter;
    }
    this.findRequest = FindRequest.getBuilder(resultsFilter, true).build();
    this.cardBuilder = cardBuilder;
    this.cards = new ArrayList<>();

    // Initialize
    refresh();
  }

  public void refresh() {
    clearCards();
    updateResultsAndRebuildCards();
  }

  private void clearCards() {
    this.cardsPanel.clear();
  }

  private void updateResultsAndRebuildCards() {
    this.cards.clear();
    this.cardsPanel.clear();
    Services services = new Services("Retrieve AIP representations or dissemination", "get");
    services.rodaEntityRestService(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()), objectClass)
      .whenComplete((requestResults, error) -> {
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

  interface MyUiBinder extends UiBinder<Widget, ThumbnailCardList<? extends IsIndexed>> {
  }
}
