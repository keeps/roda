package org.roda.wui.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.wui.client.common.panels.GenericCollapsibleCardPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CreateJobOrchestration extends GenericCollapsibleCardPanel<IndexedJob> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private JobPriority selectedPriority = JobPriority.MEDIUM;
  private JobParallelism selectedParallelism = JobParallelism.NORMAL;

  public CreateJobOrchestration() {
    super(true, false);
    setData(new IndexedJob());
  }

  public JobPriority getSelectedPriority() { return selectedPriority; }
  public JobParallelism getSelectedParallelism() { return selectedParallelism; }

  @Override
  protected void defineHeader(IndexedJob data) {
    buildHeader(messages.jobPerformanceAndPriority()).build();
  }

  @Override
  protected void buildFields(IndexedJob data) {
    Label label = new Label(messages.createJobOrchestrationPrioritySeparator());
    label.addStyleName("label mb-16");

    metadataContainer.add(label);
    metadataContainer.add(buildPrioritySelectionGroup());

    Label parallelismLabel = new Label(messages.createJobOrchestrationParallelismSeparator());
    parallelismLabel.addStyleName("label mb-16");
    metadataContainer.add(parallelismLabel);
    metadataContainer.add(buildParallelismSelectionGroup());
  }

  private FlowPanel buildParallelismSelectionGroup() {
    FlowPanel groupContainer = new FlowPanel();
    groupContainer.addStyleName("priority-group mb-16");

    groupContainer.add(buildOptionCard("parallelism", messages.jobParallelismShortBadge(JobParallelism.NORMAL),
            "badge-info", messages.createJobNormalParallelismDescription(), true,
            () -> this.selectedParallelism = JobParallelism.NORMAL)); // Callback added

    groupContainer.add(buildOptionCard("parallelism", messages.jobParallelismShortBadge(JobParallelism.LIMITED),
            "badge-warning", messages.createJobLimitedParallelismDescription(), false,
            () -> this.selectedParallelism = JobParallelism.LIMITED)); // Callback added

    return groupContainer;
  }

  private FlowPanel buildPrioritySelectionGroup() {
    FlowPanel groupContainer = new FlowPanel();
    groupContainer.addStyleName("priority-group mb-16");

    groupContainer.add(buildOptionCard("priority", messages.jobPriorityShortBadge(JobPriority.HIGH), "badge-danger",
            messages.createJobHighPriorityDescription(), false,
            () -> this.selectedPriority = JobPriority.HIGH));

    groupContainer.add(buildOptionCard("priority", messages.jobPriorityShortBadge(JobPriority.MEDIUM), "badge-warning",
            messages.createJobMediumPriorityDescription(), true,
            () -> this.selectedPriority = JobPriority.MEDIUM));

    groupContainer.add(buildOptionCard("priority", messages.jobPriorityShortBadge(JobPriority.LOW), "badge-success",
            messages.createJobLowPriorityDescription(), false,
            () -> this.selectedPriority = JobPriority.LOW));

    return groupContainer;
  }

  private FlowPanel buildOptionCard(String groupName, String badgeText, String badgeCssClass, String description, boolean isDefault, Runnable onSelect) {
    FlowPanel card = new FlowPanel();
    card.addStyleName("priority-card");

    RadioButton radio = new RadioButton(groupName);
    radio.addStyleName("priority-radio");
    radio.setValue(isDefault);

    radio.addClickHandler(event -> {
      if (onSelect != null) onSelect.run();
    });

    // UX Enhancement: Make clicking anywhere on the card select the radio button
    card.addDomHandler(event -> {
      radio.setValue(true);
      if (onSelect != null) onSelect.run();
    }, com.google.gwt.event.dom.client.ClickEvent.getType());

    // 2. The Content Wrapper on the right
    FlowPanel contentColumn = new FlowPanel();
    contentColumn.addStyleName("priority-content");

    // 3. The Badge (Top Right)
    Label badge = new Label(badgeText);
    badge.addStyleName("priority-badge " + badgeCssClass);

    // 4. The Description (Bottom Right)
    HTMLPanel descPanel = new HTMLPanel("p", description);
    descPanel.addStyleName("priority-description");

    contentColumn.add(badge);
    contentColumn.add(descPanel);

    // Assemble the card
    card.add(radio);
    card.add(contentColumn);

    return card;
  }
}