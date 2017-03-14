/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.HashMap;
import java.util.Map.Entry;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class InfoSliderHelper {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private InfoSliderHelper() {

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
    HashMap<String, SafeHtml> values = new HashMap<>();

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
    HashMap<String, SafeHtml> values = new HashMap<>();

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
    HashMap<String, SafeHtml> values = new HashMap<>();

    infoSliderPanel.clear();
    infoSliderPanel.addTitle(new Label(messages.viewRepresentationInfoTitle()));

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), SafeHtmlUtils.fromString(fileName));

      if (file.getSize() > 0) {
        values.put(messages.viewRepresentationInfoSize(),
          SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize())));
      }

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
    for (Entry<String, SafeHtml> entry : values.entrySet()) {
      FlowPanel entryPanel = new FlowPanel();

      Label keyLabel = new Label(entry.getKey());
      HTML valueLabel = new HTML(entry.getValue());

      entryPanel.add(keyLabel);
      entryPanel.add(valueLabel);

      infoSliderPanel.addContent(entryPanel);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entryPanel.addStyleName("infoFileEntry");
    }
  }
}
