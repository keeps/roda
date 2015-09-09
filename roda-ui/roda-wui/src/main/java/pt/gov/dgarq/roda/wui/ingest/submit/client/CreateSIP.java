/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.submit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestSubmitConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.SelectableAIPList;
import pt.gov.dgarq.roda.wui.common.fileupload.client.FileUploadPanel;
import pt.gov.dgarq.roda.wui.dissemination.client.DescriptiveMetadataPanel;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;

/**
 * @author Luis Faria
 * 
 */
public class CreateSIP {

  private static IngestSubmitConstants constants = (IngestSubmitConstants) GWT.create(IngestSubmitConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean initialized;

  private VerticalPanel layout;

  private HorizontalPanel descriptiveMetadataHeaderLayout;

  private Label descriptiveMetadataHeader;

  private Label descriptiveMetadataOptionalToggle;

  private DescriptiveMetadataPanel descriptiveMetadata;

  private Label representationHeader;

  private HorizontalPanel representationLayout;

  private ContentModelSelector representationType;

  private FileUploadPanel fileUpload;

  private Label destinationHeader;

  // private CollectionsTreeVerticalScrollPanel destinationChooser;
  private SelectableAIPList destinationChooser;

  private HorizontalPanel submitLayout;

  private Button submitButton;

  private boolean submitting;

  private Image loadingImage;

  private Label submitMessage;

  private Button getRodaIn;

  /**
   * Create a new create SIP panel
   */
  public CreateSIP() {
    layout = new VerticalPanel();
    initialized = false;
    submitting = false;
  }

  /**
   * Initialize the create SIP panel
   */
  public void init() {
    if (!initialized) {
      initialized = true;
      descriptiveMetadataHeaderLayout = new HorizontalPanel();
      descriptiveMetadataHeader = new Label(constants.createMetadataHeader());
      descriptiveMetadataOptionalToggle = new Label();
      descriptiveMetadata = new DescriptiveMetadataPanel();
      descriptiveMetadata.setReadonly(false);
      descriptiveMetadata.setOptionalVisible(false);

      updateOptionalMetadataToggle();
      descriptiveMetadataOptionalToggle.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          descriptiveMetadata.setOptionalVisible(!descriptiveMetadata.isOptionalVisible());
          updateOptionalMetadataToggle();
        }

      });

      representationHeader = new Label(constants.createRepresentationHeader());
      representationLayout = new HorizontalPanel();
      representationType = new ContentModelSelector();
      fileUpload = new FileUploadPanel(representationType.getSelected().getFilenameConstraints());
      representationType.addChangeListener(new ChangeListener() {

        public void onChange(Widget sender) {
          fileUpload.setConstraints(representationType.getSelected().getFilenameConstraints());
          updateVisibles();

        }

      });

      destinationHeader = new Label(constants.createDestinationHeader());
      Filter classPlanFilter = new Filter();
      // TODO add producer filter
      // classPlanFilter.add(new ProducerFilterParameter());
      // int size =
      // DescriptionLevelUtils.ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS.size();
      // String[] classPlanLevels = new String[size];
      // for (int i = 0; i < size; i++) {
      // classPlanLevels[i] =
      // DescriptionLevelUtils.ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS.get(i).getLevel();
      // }
      // classPlanFilter.add(new
      // OneOfManyFilterParameter(RodaConstants.SDO_LEVEL,
      // classPlanLevels));
      // destinationChooser = new
      // CollectionsTreeVerticalScrollPanel(classPlanFilter,
      // CollectionsTreeVerticalScrollPanel.DEFAULT_SORTER, true);
      destinationChooser = new SelectableAIPList();
      destinationChooser.setFilter(classPlanFilter);

      submitLayout = new HorizontalPanel();

      submitButton = new Button(constants.createSubmitButton());
      submitButton.addStyleName("btn");
      submitButton.addStyleName("btn-play");

      submitButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          submit();
        }

      });

      loadingImage = new Image(GWT.getModuleBaseURL() + "images/loadingSmall.gif");
      submitMessage = new Label();

      getRodaIn = new Button(constants.createSipGetRodaIn());
      getRodaIn.addStyleName("btn");
      getRodaIn.addStyleName("btn-download");

      getRodaIn.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          Ingest.downloadRodaIn(null, null);
        }

      });

      descriptiveMetadataHeaderLayout.add(descriptiveMetadataHeader);
      descriptiveMetadataHeaderLayout.add(descriptiveMetadataOptionalToggle);

      representationLayout.add(representationType.getWidget());
      representationLayout.add(fileUpload.getWidget());

      submitLayout.add(submitButton);
      submitLayout.add(loadingImage);
      submitLayout.add(submitMessage);
      submitLayout.add(getRodaIn);

      submitButton.setEnabled(false);
      loadingImage.setVisible(false);

      updateVisibles();

      ChangeListener listener = new ChangeListener() {

        public void onChange(Widget sender) {
          if (!submitting) {
            updateVisibles();
          }
        }

      };

      descriptiveMetadata.addChangeListener(listener);
      fileUpload.addChangeListener(listener);

      destinationChooser.addValueChangeHandler(new ValueChangeHandler<SimpleDescriptionObject>() {

        @Override
        public void onValueChange(ValueChangeEvent<SimpleDescriptionObject> event) {
          updateVisibles();
        }
      });

      layout.add(descriptiveMetadataHeaderLayout);
      layout.add(descriptiveMetadata);
      layout.add(representationHeader);
      layout.add(representationLayout);
      layout.add(destinationHeader);
      layout.add(destinationChooser);
      layout.add(submitLayout);

      layout.addStyleName("wui-ingest-submit-create");
      descriptiveMetadataHeader.addStyleName("h3");
      representationHeader.addStyleName("h3");
      destinationHeader.addStyleName("h3");
      descriptiveMetadataHeaderLayout.addStyleName("create-metadata-header");
      descriptiveMetadataOptionalToggle.addStyleName("create-metadata-toggle");
      descriptiveMetadata.addStyleName("create-metadata");
      representationLayout.addStyleName("create-representation");
      fileUpload.getWidget().addStyleName("create-representation-file");
      submitLayout.addStyleName("create-submit");
      submitButton.addStyleName("create-submit-button");
      loadingImage.addStyleName("create-submit-loading");
      submitMessage.setStylePrimaryName("create-submit-message");
      getRodaIn.addStyleName("create-submit-get-roda-in");
      submitLayout.setCellVerticalAlignment(loadingImage, HasAlignment.ALIGN_MIDDLE);
      submitLayout.setCellVerticalAlignment(submitMessage, HasAlignment.ALIGN_MIDDLE);
      submitLayout.setCellWidth(getRodaIn, "100%");
      submitLayout.setCellHorizontalAlignment(getRodaIn, HasHorizontalAlignment.ALIGN_RIGHT);
    } else {
      // destinationChooser.clear(new AsyncCallback<Integer>() {
      //
      // public void onFailure(Throwable caught) {
      // logger.error("Error updating destination chooser", caught);
      // }
      //
      // public void onSuccess(Integer result) {
      // // nothing to do
      //
      // }
      // });
    }
  }

  private void updateOptionalMetadataToggle() {
    descriptiveMetadataOptionalToggle.setText(descriptiveMetadata.isOptionalVisible() ? constants
      .createHideOptionalMetadata() : constants.createShowOptionalMetadata());
  }

  /**
   * Get widget
   * 
   * @return widget
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Update visible and enabled state of all widgets
   */
  public void updateVisibles() {
    descriptiveMetadata.isValid(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        logger.error("Error validating descriptive metadata", caught);
      }

      public void onSuccess(Boolean isValid) {
        if (!isValid) {
          submitButton.setEnabled(false);
          submitMessage.setText(constants.createMetadataInvalidWarning());
          submitMessage.addStyleDependentName("error");
        } else if (fileUpload.isEmpty()) {
          submitButton.setEnabled(false);
          submitMessage.setText(constants.createNoFilesWarning());
          submitMessage.addStyleDependentName("error");
        } else if (!fileUpload.isValid()) {
          submitButton.setEnabled(false);
          submitMessage.setText(constants.createInvalidFilesWarning());
          submitMessage.addStyleDependentName("error");
        } else if (destinationChooser.getSelected() == null) {
          submitButton.setEnabled(false);
          submitMessage.setText(constants.createNoDestinationWarning());
          submitMessage.addStyleDependentName("error");
        }
        // else if
        // (!DescriptionLevelUtils.ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS
        // .contains(destinationChooser.getSelected().getLevel())) {
        // submitButton.setEnabled(false);
        // submitMessage.setText(constants.createInvalidDestinationWarning());
        // submitMessage.addStyleDependentName("error");
        // }
        else {
          submitButton.setEnabled(true);
          submitMessage.setText("");
          submitMessage.removeStyleDependentName("error");
        }
      }

    });

  }

  /**
   * Submit SIP
   */
  public void submit() {
    submitting = true;
    loadingImage.setVisible(true);
    submitMessage.setText(constants.createSubmitUploadingMessage());
    submitButton.setEnabled(false);
    fileUpload.submit(new AsyncCallback<String[]>() {

      public void onFailure(Throwable caught) {
        if (caught != null) {
          logger.error("Error uploading files", caught);
        }

        submitting = false;
        loadingImage.setVisible(false);
        submitMessage.setText("");
        submitButton.setEnabled(true);

      }

      public void onSuccess(String[] fileCodes) {
        submitMessage.setText(constants.createSubmitSubmitingMessage());
        descriptiveMetadata.save();
        String contentModel = representationType.getSelected().getContentModel();
        // IngestSubmitService.Util.getInstance().createSIP(contentModel,
        // descriptiveMetadata.getDescriptionObject(), fileCodes,
        // destinationChooser.getSelected().getPid(), new
        // AsyncCallback<Boolean>() {
        //
        // public void onFailure(Throwable caught) {
        // logger.error("Error while submiting", caught);
        // submitting = false;
        //
        // loadingImage.setVisible(false);
        // submitMessage.setText("");
        // submitButton.setEnabled(true);
        // }
        //
        // public void onSuccess(Boolean success) {
        // if (success.booleanValue()) {
        // loadingImage.setVisible(false);
        // submitMessage.setText("");
        // submitButton.setEnabled(true);
        // descriptiveMetadata.clear();
        // fileUpload.clear();
        //
        // // Initialize ingest list
        // IngestList.getInstance().init();
        //
        // // Set processing state filter
        // IngestList.getInstance().setStateFilter(IngestList.StateFilter.PROCESSING);
        //
        // // Update ingest list
        // IngestList.getInstance().update();
        //
        // // Show ingest list
        // History.newItem(IngestList.getInstance().getHistoryPath());
        //
        // } else {
        // loadingImage.setVisible(false);
        // submitMessage.setText(constants.createSubmitFailureMessage());
        // submitMessage.addStyleDependentName("error");
        // submitButton.setEnabled(true);
        // }
        // submitting = false;
        // updateVisibles();
        //
        // }
        //
        // });

      }

    });
  }

}
