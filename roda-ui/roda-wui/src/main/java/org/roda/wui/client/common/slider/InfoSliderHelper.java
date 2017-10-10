/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.planning.RepresentationInformationRegister;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class InfoSliderHelper {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String INFO_ICON = "<i class='fa fa-info-circle browseFileInformationIcon' aria-hidden='true'></i>";

  private InfoSliderHelper() {
    // do nothing
  }

  protected static <T extends IsIndexed> void updateInfoObjectSliderPanel(T object, SliderPanel slider) {
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
    HashMap<String, Widget> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (aip != null) {
      if (StringUtils.isNotBlank(aip.getLevel())) {
        values.put(messages.aipLevel(),
          new InlineHTML(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), true)));
      }

      if (StringUtils.isNotBlank(aip.getTitle())) {
        values.put(messages.aipGenericTitle(), new InlineHTML(SafeHtmlUtils.fromString(aip.getTitle())));
      }

      if (aip.getDateInitial() != null || aip.getDateFinal() != null) {
        values.put(messages.aipDates(), new InlineHTML(
          SafeHtmlUtils.fromString(Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), true))));
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void updateInfoSliderPanel(IndexedRepresentation representation, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (representation != null) {

      if (StringUtils.isNotBlank(messages.representationType())) {
        values.put(messages.representationType(),
          new InlineHTML(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), true)));
      }

      if (StringUtils.isNotBlank(messages.representationFiles())) {
        values.put(messages.representationFiles(),
          new InlineHTML(SafeHtmlUtils.fromString(messages.numberOfFiles(representation.getNumberOfDataFiles()))));
      }

      if (representation.getNumberOfDataFiles() > 0) {
        values.put(messages.representationFiles(),
          new InlineHTML(SafeHtmlUtils.fromString(messages.numberOfFiles(representation.getNumberOfDataFiles()))));
      }

      values.put(messages.representationOriginal(), new InlineHTML(SafeHtmlUtils.fromString(
        representation.isOriginal() ? messages.originalRepresentation() : messages.alternativeRepresentation())));
    }

    populate(infoSliderPanel, values);
  }

  private static void updateInfoSliderPanel(IndexedFile file, SliderPanel infoSliderPanel) {
    HashMap<String, Widget> values = new HashMap<>();
    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), new InlineHTML(SafeHtmlUtils.fromString(fileName)));

      if (file.getSize() > 0) {
        values.put(messages.viewRepresentationInfoSize(),
          new InlineHTML(SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize()))));
      }

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), createMimetypeHTML(fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(), createExtensionHTML(fileFormat.getFormatDesignation()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), createPronomHTML(fileFormat.getPronom()));
        }
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          new InlineHTML(SafeHtmlUtils.fromString(file.getCreatingApplicationName())));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          new InlineHTML(SafeHtmlUtils.fromString(file.getCreatingApplicationVersion())));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          new InlineHTML(SafeHtmlUtils.fromString(file.getDateCreatedByApplication())));
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
        values.put(messages.viewRepresentationInfoHash(), new InlineHTML(b.toSafeHtml()));
      }

      if (file.getStoragePath() != null) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
      }
    }

    populate(infoSliderPanel, values);
  }

  private static void populate(SliderPanel infoSliderPanel, HashMap<String, Widget> values) {
    for (Entry<String, Widget> entry : values.entrySet()) {
      FlowPanel entryPanel = new FlowPanel();

      Label keyLabel = new Label(entry.getKey());
      Widget valueLabel = entry.getValue();

      entryPanel.add(keyLabel);
      entryPanel.add(valueLabel);
      infoSliderPanel.addContent(entryPanel);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entryPanel.addStyleName("infoFileEntry");
    }
  }

  private static FlowPanel createMimetypeHTML(String mimetype) {
    FlowPanel mimetypePanel = new FlowPanel();
    mimetypePanel.add(new InlineHTML(SafeHtmlUtils.fromString(mimetype)));

    final Anchor anchor = new Anchor();
    anchor.setHTML(SafeHtmlUtils.fromSafeConstant(INFO_ICON));
    final List<String> filter = RepresentationInformationUtils.createRepresentationInformationFileFilter(null, mimetype,
      null);

    BrowserService.Util.getInstance().retrieveRepresentationInformationWithFilter(filter.get(0),
      new AsyncCallback<Pair<String, Integer>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Pair<String, Integer> pair) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          anchor.removeStyleName("browseIconRed");

          if (pair.getSecond() == 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(ShowRepresentationInformation.RESOLVER, pair.getFirst()));
          } else if (pair.getSecond() > 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          } else {
            anchor.addStyleName("browseIconRed");
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          }
        }
      });

    mimetypePanel.add(anchor);
    return mimetypePanel;
  }

  private static FlowPanel createPronomHTML(String pronom) {
    FlowPanel pronomPanel = new FlowPanel();
    pronomPanel.add(new InlineHTML(SafeHtmlUtils.fromString(pronom)));

    final Anchor anchor = new Anchor();
    anchor.setHTML(SafeHtmlUtils.fromSafeConstant(INFO_ICON));
    final List<String> filter = RepresentationInformationUtils.createRepresentationInformationFileFilter(pronom, null,
      null);

    BrowserService.Util.getInstance().retrieveRepresentationInformationWithFilter(filter.get(0),
      new AsyncCallback<Pair<String, Integer>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Pair<String, Integer> pair) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          anchor.removeStyleName("browseIconRed");

          if (pair.getSecond() == 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(ShowRepresentationInformation.RESOLVER, pair.getFirst()));
          } else if (pair.getSecond() > 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          } else {
            anchor.addStyleName("browseIconRed");
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          }
        }
      });

    pronomPanel.add(anchor);
    return pronomPanel;
  }

  private static FlowPanel createExtensionHTML(String designation) {
    FlowPanel pronomPanel = new FlowPanel();
    pronomPanel.add(new InlineHTML(SafeHtmlUtils.fromString(designation)));

    final Anchor anchor = new Anchor();
    anchor.setHTML(SafeHtmlUtils.fromSafeConstant(INFO_ICON));
    final List<String> filter = RepresentationInformationUtils.createRepresentationInformationFileFilter(null, null,
      designation);

    BrowserService.Util.getInstance().retrieveRepresentationInformationWithFilter(filter.get(0),
      new AsyncCallback<Pair<String, Integer>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Pair<String, Integer> pair) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          anchor.removeStyleName("browseIconRed");

          if (pair.getSecond() == 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(ShowRepresentationInformation.RESOLVER, pair.getFirst()));
          } else if (pair.getSecond() > 1) {
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          } else {
            anchor.addStyleName("browseIconRed");
            anchor.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationRegister.RESOLVER,
              Search.RESOLVER.getHistoryToken(), RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter.get(0)));
          }
        }
      });

    pronomPanel.add(anchor);
    return pronomPanel;
  }
}
