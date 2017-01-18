package org.roda.wui.client.common.slider;

import java.util.HashMap;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class Sliders {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Sliders() {

  }

  public static SliderPanel createSlider(FlowPanel container, FocusPanel toggleButton) {
    SliderPanel slider = new SliderPanel();
    container.add(slider);
    container.addStyleName("slider-container");
    slider.setToggleButton(toggleButton);
    return slider;
  }

  // DISSEMINATIONS

  public static void updateDisseminationsSliderPanel(final IndexedAIP aip,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getUUID()));
    updateDisseminations(aip, filter, disseminationsSliderPanel);
  }

  public static void updateDisseminationsSliderPanel(IndexedRepresentation representation,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
    updateDisseminations(representation, filter, disseminationsSliderPanel);
  }

  public static void updateDisseminationsSliderPanel(IndexedFile file, final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, file.getUUID()));
    updateDisseminations(file, filter, disseminationsSliderPanel);
  }

  public static <T extends IsIndexed> SliderPanel createDisseminationsSlider(FlowPanel container,
    FocusPanel toggleButton, T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    updateDisseminationsObjectSliderPanel(object, slider);
    return slider;
  }

  private static <T extends IsIndexed> void updateDisseminations(final T object, Filter filter,
    final SliderPanel disseminationsSliderPanel) {
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    Sublist sublist = new Sublist(0, 100);
    Facets facets = Facets.NONE;
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, sublist, facets, localeString,
      justActive, new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          updateDisseminationsSliderPanel(result.getResults(), object, disseminationsSliderPanel);
        }
      });
  }

  private static <T extends IsIndexed> void updateDisseminationsSliderPanel(List<IndexedDIP> dips, T object,
    SliderPanel disseminationsSliderPanel) {

    disseminationsSliderPanel.clear();
    disseminationsSliderPanel.addTitle(new Label(messages.viewRepresentationFileDisseminationTitle()));

    if (dips.isEmpty()) {
      Label dipEmpty = new Label(messages.browseFileDipEmpty());
      disseminationsSliderPanel.addContent(dipEmpty);
      dipEmpty.addStyleName("dip-empty");
    } else {
      for (final IndexedDIP dip : dips) {
        disseminationsSliderPanel.addContent(createDisseminationPanel(dip, object, disseminationsSliderPanel));
      }
    }
  }

  private static <T extends IsIndexed> FlowPanel createDisseminationPanel(final IndexedDIP dip, final T object,
    final SliderPanel disseminationsSliderPanel) {
    FlowPanel layout = new FlowPanel();

    // open layout
    FlowPanel leftLayout = new FlowPanel();
    Label titleLabel = new Label(dip.getTitle());
    Label descriptionLabel = new Label(dip.getDescription());

    leftLayout.add(titleLabel);
    leftLayout.add(descriptionLabel);

    FocusPanel openFocus = new FocusPanel(leftLayout);
    layout.add(openFocus);

    // delete
    HTML deleteIcon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-ban'></i>"));
    FocusPanel deleteButton = new FocusPanel(deleteIcon);
    deleteButton.addStyleName("lightbtn");
    deleteIcon.addStyleName("lightbtn-icon");
    deleteButton.setTitle(messages.browseFileDipDelete());

    deleteButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        deleteDissemination(dip, object, disseminationsSliderPanel);
      }
    });

    layout.add(deleteButton);

    titleLabel.addStyleName("dipTitle");
    descriptionLabel.addStyleName("dipDescription");
    layout.addStyleName("dip");
    leftLayout.addStyleName("dip-left");
    openFocus.addStyleName("dip-focus");
    deleteButton.addStyleName("dip-delete");

    openFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (StringUtils.isNotBlank(dip.getOpenExternalURL())) {
          Window.open(dip.getOpenExternalURL(), "_blank", "");
          Toast.showInfo(messages.browseFileDipOpenedExternalURL(), dip.getOpenExternalURL());
        } else {
          HistoryUtils.openBrowse(dip);
        }
      }
    });

    return layout;
  }

  private static <T extends IsIndexed> void updateDisseminationsObjectSliderPanel(final T object,
    final SliderPanel disseminationsSliderPanel) {
    if (object instanceof IndexedAIP) {
      updateDisseminationsSliderPanel((IndexedAIP) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedRepresentation) {
      updateDisseminationsSliderPanel((IndexedRepresentation) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedFile) {
      updateDisseminationsSliderPanel((IndexedFile) object, disseminationsSliderPanel);
    } else {
      // do nothing
    }
  }

  private static <T extends IsIndexed> void deleteDissemination(final IndexedDIP dip, final T object,
    final SliderPanel disseminationsSliderPanel) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIP(dip.getId(), new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                updateDisseminationsObjectSliderPanel(object, disseminationsSliderPanel);
              }

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
  }

  // INFO

  public static <T extends IsIndexed> SliderPanel createInfoSlider(FlowPanel container, FocusPanel toggleButton,
    T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    updateInfoObjectSliderPanel(object, slider);
    return slider;
  }

  private static <T extends IsIndexed> void updateInfoObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedFile) {
      updateInfoSliderPanel((IndexedFile) object, slider);
    } else if (object instanceof IndexedRepresentation) {
      updateInfoSliderPanel((IndexedRepresentation) object, slider);
    } else if (object instanceof IndexedAIP) {
      updateInfoSliderPanel((IndexedAIP) object, slider);
    } else {
      // do nothing
    }

  }

  private static void updateInfoSliderPanel(IndexedAIP aip, SliderPanel infoSliderPanel) {
    HashMap<String, SafeHtml> values = new HashMap<String, SafeHtml>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (aip != null) {

      if (StringUtils.isNotBlank(aip.getLevel())) {
        values.put(messages.aipLevel(), DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), true));
      }

      if (StringUtils.isNotBlank(aip.getTitle())) {
        values.put(messages.aipGenericTitle(), SafeHtmlUtils.fromString(aip.getTitle()));
      }

      if (aip.getDateInitial() != null || aip.getDateFinal() != null) {
        values.put(messages.aipDates(),
          SafeHtmlUtils.fromString(Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), true)));
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void updateInfoSliderPanel(IndexedRepresentation representation, SliderPanel infoSliderPanel) {
    HashMap<String, SafeHtml> values = new HashMap<String, SafeHtml>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (representation != null) {

      if (StringUtils.isNotBlank(messages.representationType())) {
        values.put(messages.representationType(),
          DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true));
      }

      if (StringUtils.isNotBlank(messages.representationFiles())) {
        values.put(messages.representationFiles(),
          SafeHtmlUtils.fromString(messages.numberOfFiles(representation.getNumberOfDataFiles())));
      }

      if (representation.getNumberOfDataFiles() > 0) {
        values.put(messages.representationFiles(),
          SafeHtmlUtils.fromString(messages.numberOfFiles(representation.getNumberOfDataFiles())));
      }

      values.put(messages.representationOriginal(), SafeHtmlUtils.fromString(
        representation.isOriginal() ? messages.originalRepresentation() : messages.alternativeRepresentation()));
    }

    populate(infoSliderPanel, values);
  }

  private static void updateInfoSliderPanel(IndexedFile file, SliderPanel infoSliderPanel) {
    HashMap<String, SafeHtml> values = new HashMap<String, SafeHtml>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), SafeHtmlUtils.fromString(fileName));

      values.put(messages.viewRepresentationInfoSize(),
        SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize())));

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), SafeHtmlUtils.fromString(fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationName()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationVersion())) {
          values.put(messages.viewRepresentationInfoFormatVersion(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationVersion()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), SafeHtmlUtils.fromString(fileFormat.getPronom()));
        }

      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationVersion()));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          SafeHtmlUtils.fromString(file.getDateCreatedByApplication()));
      }

      if (file.getHash() != null && !file.getHash().isEmpty()) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        for (String hash : file.getHash()) {
          if (first) {
            first = false;
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(hash));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));
        }
        values.put(messages.viewRepresentationInfoHash(), b.toSafeHtml());
      }

      if (file.getStoragePath() != null) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), b.toSafeHtml());
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void populate(SliderPanel infoSliderPanel, HashMap<String, SafeHtml> values) {
    for (String key : values.keySet()) {
      FlowPanel entry = new FlowPanel();

      Label keyLabel = new Label(key);
      HTML valueLabel = new HTML(values.get(key));

      entry.add(keyLabel);
      entry.add(valueLabel);

      infoSliderPanel.addContent(entry);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entry.addStyleName("infoFileEntry");
    }
  }

}
