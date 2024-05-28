package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationCreateRequest;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationForm;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.base.disposal.confirmation.CreateDisposalConfirmationPlugin;
import org.roda.core.plugins.base.disposal.confirmation.DeleteDisposalConfirmationPlugin;
import org.roda.core.plugins.base.disposal.confirmation.DestroyRecordsPlugin;
import org.roda.core.plugins.base.disposal.confirmation.PermanentlyDeleteRecordsPlugin;
import org.roda.core.plugins.base.disposal.confirmation.RecoverDisposalConfirmationExecutionFailedPlugin;
import org.roda.core.plugins.base.disposal.confirmation.RestoreRecordsPlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DisposalConfirmationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalConfirmationService.class);

  private static final String DISPOSAL_CONFIRMATION_COMMAND_PROPERTY = "core.confirmation.generate.report.command";
  private static final String METADATA_FILE_PLACEHOLDER = "metadataFile";
  private static final String AIPS_FILE_PLACEHOLDER = "aipsFile";
  private static final String SCHEDULES_FILE_PLACEHOLDER = "schedulesFile";
  private static final String HOLDS_FILE_PLACEHOLDER = "holdsFile";

  private static final String DISPOSAL_CONFIRMATION_REPORT_HBS = "disposal_confirmation_report.html.hbs";
  private static final String DISPOSAL_CONFIRMATION_REPORT_PRINT_HBS = "disposal_confirmation_report_print.html.hbs";

  private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String HBS_DATEFORMAT_HELPER_NAME = "humanize";

  public Job destroyRecordsInDisposalConfirmation(User user, SelectedItems<DisposalConfirmation> selectedItems)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return CommonServicesUtils.createAndExecuteInternalJob("Destroy records from disposal confirmation report",
      selectedItems, DestroyRecordsPlugin.class, user, Collections.emptyMap(),
      "Could not execute destruction of records in disposal confirmation report action");
  }

  public Job permanentlyDeleteRecordsInDisposalConfirmation(User user,
    SelectedItems<DisposalConfirmation> selectedItems)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return CommonServicesUtils.createAndExecuteInternalJob("Permanently delete records from disposal bin",
      selectedItems, PermanentlyDeleteRecordsPlugin.class, user, Collections.emptyMap(),
      "Could not execute permanent deletion of records in disposal bin action");
  }

  public Job restoreRecordsInDisposalConfirmation(User user, SelectedItems<DisposalConfirmation> selectedItems)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return CommonServicesUtils.createAndExecuteInternalJob("Restore destroyed records from disposal bin", selectedItems,
      RestoreRecordsPlugin.class, user, Collections.emptyMap(),
      "Could not execute restoration of destroyed records from disposal bin action");
  }

  public Job recoverDisposalConfirmationExecutionFailed(User user, SelectedItems<DisposalConfirmation> selectedItems)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return CommonServicesUtils.createAndExecuteInternalJob("Recovers disposal confirmation from a failure state",
      selectedItems, RecoverDisposalConfirmationExecutionFailedPlugin.class, user, Collections.emptyMap(),
      "Could not execute recover the disposal confirmation from a previous faulty state");
  }

  public Job deleteDisposalConfirmation(User user, SelectedItems<DisposalConfirmation> selectedItems, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);

    return CommonServicesUtils.createAndExecuteInternalJob("Delete disposal confirmation report", selectedItems,
      DeleteDisposalConfirmationPlugin.class, user, pluginParameters,
      "Could not execute delete disposal confirmation report");
  }

  public Job createDisposalConfirmation(User user, DisposalConfirmationCreateRequest createRequest)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> extraInformation = getDisposalConfirmationExtra(createRequest.getForm());
    String extraInformationJson = JsonUtils.getJsonFromObject(extraInformation);

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE, createRequest.getTitle());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO, extraInformationJson);

    return CommonServicesUtils.createAndExecuteInternalJob("Create disposal confirmation report",
      createRequest.getSelectedItems(), CreateDisposalConfirmationPlugin.class, user, pluginParameters,
      "Could not execute create disposal confirmation report action");
  }

  private Map<String, String> getDisposalConfirmationExtra(DisposalConfirmationForm form) {
    Map<String, String> data = new HashMap<>();

    if (form != null) {
      Set<MetadataValue> values = form.getValues();
      if (values != null) {
        values.forEach(metadataValue -> {
          String val = metadataValue.get("value");
          if (val != null) {
            val = val.replaceAll("\\s", "");
            if (!val.isEmpty()) {
              data.put(metadataValue.get("name"), metadataValue.get("value"));
            }
          }
        });
      }
    }

    return data;
  }

  public String createDisposalConfirmationReport(String confirmationId, boolean isToPrint)
    throws RODAException, IOException {
    String jqCommandTemplate = RodaCoreFactory.getRodaConfigurationAsString(DISPOSAL_CONFIRMATION_COMMAND_PROPERTY);

    Path metadataPath = getDisposalConfirmationMetadataPath(confirmationId);
    Path aipsPath = getDisposalConfirmationAIPsPath(confirmationId);
    Path schedulesPath = getDisposalConfirmationSchedulesPath(confirmationId);
    Path holdsPath = getDisposalConfirmationHoldsPath(confirmationId);

    Map<String, String> values = new HashMap<>();

    values.put(METADATA_FILE_PLACEHOLDER, metadataPath.toString());
    values.put(AIPS_FILE_PLACEHOLDER, aipsPath.toString());
    values.put(SCHEDULES_FILE_PLACEHOLDER, schedulesPath.toString());
    values.put(HOLDS_FILE_PLACEHOLDER, holdsPath.toString());

    String jqCommandParams = HandlebarsUtility.executeHandlebars(jqCommandTemplate, values);

    List<String> jqCommand = new ArrayList<>();
    Collections.addAll(jqCommand, jqCommandParams.split(" "));

    String output = null;
    try {
      output = CommandUtility.execute(jqCommand);
    } catch (CommandException e) {
      throw new RODAException(e);
    }
    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    Map<String, Object> confirmationValues = new ObjectMapper().readValue(output, typeRef);
    InputStream templateStream;

    if (isToPrint) {
      templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER + "/" + DISPOSAL_CONFIRMATION_REPORT_PRINT_HBS);
    } else {
      templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER + "/" + DISPOSAL_CONFIRMATION_REPORT_HBS);
    }

    String reportTemplate = IOUtils.toString(templateStream, StandardCharsets.UTF_8);

    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper(HBS_DATEFORMAT_HELPER_NAME, new Helper<Long>() {
      @Override
      public Object apply(Long value, Options options) throws IOException {
        ZonedDateTime date = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC);
        return DateTimeFormatter.ofPattern(DATETIME_FORMAT).format(date);
      }
    });
    Template template = handlebars.compileInline(reportTemplate);
    return template.apply(confirmationValues);
  }

  private Path getDisposalConfirmationMetadataPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    return entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME);
  }

  private Path getDisposalConfirmationAIPsPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    return entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME);
  }

  private Path getDisposalConfirmationSchedulesPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    return entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_SCHEDULES_FILENAME);
  }

  private Path getDisposalConfirmationHoldsPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    return entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_HOLDS_FILENAME);
  }

  public DisposalConfirmationForm retrieveDisposalConfirmationExtraBundle() {
    String template = null;

    try (InputStream templateStream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER + "/"
        + RodaConstants.DISPOSAL_CONFIRMATION_EXTRA_METADATA_FILE)) {
      template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOGGER.error("Error getting template from stream", e);
    }

    return new DisposalConfirmationForm(ServerTools.transform(template));
  }
}
