package org.roda.wui.client.common.panels;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * A generic composite card panel that is collapsible.
 * Inherits metadata field building from GenericMetadataCardPanel.
 *
 * @param <T> The data model type.
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class GenericCollapsibleCardPanel<T> extends GenericMetadataCardPanel<T> {

    private final FlowPanel headerTitleContainer;
    private final FlowPanel headerBadgesContainer;
    private final InlineHTML chevronIcon;

    private boolean isCollapsed;
    private boolean useStepCounter;

    protected GenericCollapsibleCardPanel() {
        this(true, true);
    }

    // Existing constructor allows defining initial state, keeps numbering
    protected GenericCollapsibleCardPanel(boolean initiallyCollapsed) {
        this(initiallyCollapsed, true);
    }

    // 2. New constructor allows you to define both initial state and numbering
    protected GenericCollapsibleCardPanel(boolean initiallyCollapsed, boolean useStepCounter) {
        super();
        this.isCollapsed = initiallyCollapsed;
        this.useStepCounter = useStepCounter;

        // --- Reconfigure Main Container ---
        mainContainer.addStyleName("collapsible-card");
        mainContainer.addStyleName(isCollapsed ? "collapsed" : "expanded");

        // --- Reconfigure Header ---
        headerContainer.addStyleName("collapsible-header addCursorPointer");

        headerTitleContainer = new FlowPanel();
        headerTitleContainer.setStyleName("collapsible-title");

        if (!this.useStepCounter) {
            headerTitleContainer.addStyleName("no-step-counter");
        }

        FlowPanel rightSideContainer = new FlowPanel();
        rightSideContainer.setStyleName("collapsible-right-side");

        headerBadgesContainer = new FlowPanel();
        headerBadgesContainer.setStyleName("collapsible-badges");

        chevronIcon = new InlineHTML("<i class='fa fa-chevron-down'></i>");
        chevronIcon.setStyleName("collapsible-chevron");

        rightSideContainer.add(headerBadgesContainer);
        rightSideContainer.add(chevronIcon);

        headerContainer.add(headerTitleContainer);
        headerContainer.add(rightSideContainer);

        // Parent class doesn't add the header by default, so we insert it here
        mainContainer.insert(headerContainer, 0);

        // --- Reconfigure Body ---
        bodyContainer.addStyleName("collapsible-body");
        bodyContainer.setVisible(!isCollapsed);

        // --- Collapse/Expand Logic ---
        headerContainer.addDomHandler(event -> toggleCollapse(), ClickEvent.getType());
    }

    private void toggleCollapse() {
        isCollapsed = !isCollapsed;
        bodyContainer.setVisible(!isCollapsed);
        if (isCollapsed) {
            mainContainer.removeStyleName("expanded");
            mainContainer.addStyleName("collapsed");
        } else {
            mainContainer.removeStyleName("collapsed");
            mainContainer.addStyleName("expanded");
        }
    }

    protected HeaderBuilder buildHeader(String title) {
        return new HeaderBuilder(title);
    }

    @Override
    public void setData(T data) {
        if (data == null) {
            headerTitleContainer.clear();
            headerBadgesContainer.clear();
            metadataContainer.clear();
            return;
        }

        // 1. Process Collapsible Header
        headerTitleContainer.clear();
        headerBadgesContainer.clear();
        defineHeader(data);

        // 2. Process Fields (using the inherited FieldBuilder)
        metadataContainer.clear();
        buildFields(data);
    }

    /**
     * Since we override setData to handle headers our own way, this parent method
     * is unused. We return null to fulfill the abstract contract.
     */
    @Override
    protected final FlowPanel createHeaderWidget(T data) {
        return null;
    }

    /**
     * Developers define the collapsible header title and badges here.
     */
    protected abstract void defineHeader(T data);

    // ==========================================
    // INNER HEADER BUILDER CLASS
    // ==========================================

    public class HeaderBuilder {
        private final String titleText;
        private final java.util.List<Widget> badgesQueue = new java.util.ArrayList<>();

        private HeaderBuilder(String title) {
            this.titleText = title;
        }

        public HeaderBuilder withBadge(SafeHtml badgeHtml) {
            if (badgeHtml != null) {
                badgesQueue.add(new InlineHTML(badgeHtml));
            }
            return this;
        }

        public HeaderBuilder withBadge(Widget badgeWidget) {
            if (badgeWidget != null) {
                badgesQueue.add(badgeWidget);
            }
            return this;
        }

        public void build() {
            headerTitleContainer.add(new InlineHTML(SafeHtmlUtils.htmlEscape(titleText)));

            for (Widget badge : badgesQueue) {
                headerBadgesContainer.add(badge);
            }
        }
    }
}