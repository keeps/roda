/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.Messages;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.ObjectPermissionResult;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.RepresentationInformationExtraBundle;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RelationTypeTranslationsBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

public class Browser extends RodaWuiController {

  private Browser() {
    super();
  }

  public static BrowseAIPBundle retrieveBrowseAipBundle(User user, String aipId, Locale locale,
    List<String> aipFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      aipFieldsToReturn.addAll(new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN));
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFieldsToReturn);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveBrowseAipBundle(user, aip, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  public static BrowseRepresentationBundle retrieveBrowseRepresentationBundle(User user, String aipId,
    String representationId, Locale locale, List<String> representationFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      List<String> aipFieldsWithPermissions = new ArrayList<>(Arrays.asList(RodaConstants.AIP_STATE,
        RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));
      aipFieldsWithPermissions.addAll(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);

      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), representationFieldsToReturn);

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId(), aipFieldsWithPermissions);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveBrowseRepresentationBundle(aip, representation, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  public static BrowseFileBundle retrieveBrowseFileBundle(User user, String aipId, String representationId,
    List<String> filePath, String fileId, List<String> fileFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      List<String> aipFieldsWithPermissions = new ArrayList<>(Arrays.asList(RodaConstants.AIP_STATE,
        RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));
      aipFieldsWithPermissions.addAll(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFieldsWithPermissions);
      controllerAssistant.checkObjectPermissions(user, aip);

      List<String> representationFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID,
        RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE, RodaConstants.REPRESENTATION_ORIGINAL);
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aip.getId(), representationId), representationFields);

      String fileUUID = IdUtils.getFileId(aip.getId(), representationId, filePath, fileId);
      IndexedFile file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, fileFieldsToReturn);

      // delegate
      return BrowserHelper.retrieveBrowseFileBundle(aip, representation, file, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId,
        RodaConstants.CONTROLLER_DIRECTORY_PATH_PARAM, filePath, RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId);
    }
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, String aipId,
    String representationId, String metadataId, String type, String version, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      List<String> aipFields = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      aipFields.addAll(Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFields);
      IndexedRepresentation rep = null;

      if (representationId != null) {
        rep = BrowserHelper.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_ID));
      }

      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataEditBundle(user, aip, rep, metadataId, type, version, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, String aipId,
    String representationId, String metadataId, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      List<String> aipFields = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      aipFields.addAll(Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFields);
      IndexedRepresentation rep = null;

      if (representationId != null) {
        rep = BrowserHelper.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_ID));
      }

      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataEditBundle(user, aip, rep, metadataId, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(User user, String aipId,
    String representationId, String metadataId, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataVersionsBundle(aipId, representationId, metadataId, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static DipBundle retrieveDipBundle(User user, String dipUUID, String dipFileUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      IndexedDIP dip = BrowserHelper.retrieve(IndexedDIP.class, dipUUID,
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, dip);

      return BrowserHelper.retrieveDipBundle(dipUUID, dipFileUUID, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, dipUUID, state, RodaConstants.CONTROLLER_DIP_ID_PARAM, dipUUID,
        RodaConstants.CONTROLLER_DIP_FILE_ID_PARAM, dipFileUUID);
    }
  }

  public static <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final Filter filter,
    final Sorter sorter, final Sublist sublist, final Facets facets, final User user, final boolean justActive,
    final List<String> fieldsToReturn) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.find(classToReturn, filter, sorter, sublist, facets, user, justActive, fieldsToReturn);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, filter,
        RodaConstants.CONTROLLER_SORTER_PARAM, sorter, RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);
    }
  }

  public static <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> classToReturn, final Filter filter,
    final User user, final boolean justActive, final List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // delegate
    final IterableIndexResult<T> ret = BrowserHelper.findAll(classToReturn, filter, user, justActive, fieldsToReturn);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_CLASS_PARAM,
      classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, filter,
      RodaConstants.CONTROLLER_JUST_ACTIVE_PARAM, justActive);

    return ret;
  }

  public static <T extends IsIndexed> Long count(final User user, final Class<T> classToReturn, final Filter filter,
    boolean justActive) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.count(classToReturn, filter, justActive, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, filter.toString());
    }
  }

  public static <T extends IsIndexed> T retrieve(final User user, final Class<T> classToReturn, final String id,
    final List<String> fieldsToReturn) throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      final T ret = BrowserHelper.retrieve(classToReturn, id, fieldsToReturn);

      // checking object permissions
      controllerAssistant.checkObjectPermissions(user, ret, classToReturn);

      return ret;
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName());
    }
  }

  public static <T extends IsIndexed> List<T> retrieve(final User user, final Class<T> classToReturn,
    final SelectedItems<T> selectedItems, final List<String> fieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      final List<T> objects = BrowserHelper.retrieve(classToReturn, selectedItems, fieldsToReturn);
      for (T obj : objects) {
        controllerAssistant.checkObjectPermissions(user, obj, classToReturn);
      }

      return objects;
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM, selectedItems);
    }
  }

  public static <T extends IsIndexed> void delete(final User user, final Class<T> classToReturn,
    final SelectedItems<T> ids) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);
    controllerAssistant.checkObjectPermissions(user, ids, classToReturn);

    // delegate
    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.delete(user, classToReturn, ids);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName());
    }
  }

  public static <T extends IsIndexed> List<String> suggest(final User user, final Class<T> classToReturn,
    final String field, final String query, boolean allowPartial)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.suggest(classToReturn, field, query, user, allowPartial);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FIELD_PARAM, field,
        RodaConstants.CONTROLLER_QUERY_PARAM, query);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */

  /**
   * @param user
   *          The user
   * @param selected
   *          The filter to select the AIPs to export
   * @param acceptFormat
   *          The output format
   * @return
   * @throws GenericException
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws IOException
   */
  public static StreamResponse exportAIP(User user, SelectedItems<IndexedAIP> selected, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateExportAIPParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, selected);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveAIPs(selected, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM, selected);
    }
  }

  public static EntityResponse retrieveAIPRepresentation(User user, String aipId, String representationId,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPRepresentationParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);

      controllerAssistant.checkObjectPermissions(user, representation);

      return BrowserHelper.retrieveAIPRepresentation(representation, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  public static StreamResponse retrieveAIPPart(User user, String aipId, String part)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP indexedAIP = BrowserHelper.retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, indexedAIP);

      // delegate
      return BrowserHelper.retrieveAIPPart(indexedAIP, part);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_PART_PARAM, part);
    }
  }

  public static StreamResponse retrieveAIPRepresentationPart(User user, String aipId, String representationId,
    String part) throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, representation);

      return BrowserHelper.retrieveAIPRepresentationPart(representation, part);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_PART_PARAM, part);
    }
  }

  public static EntityResponse listAIPDescriptiveMetadata(User user, String aipId, String start, String limit,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.listAIPDescriptiveMetadata(aipId, start, limit, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_START_PARAM, start, RodaConstants.CONTROLLER_LIMIT_PARAM, limit);
    }
  }

  public static EntityResponse listRepresentationDescriptiveMetadata(User user, String aipId, String representationId,
    String start, String limit, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, representation);

      // delegate
      return BrowserHelper.listRepresentationDescriptiveMetadata(representation.getAipId(), representation.getId(),
        start, limit, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_START_PARAM, start,
        RodaConstants.CONTROLLER_LIMIT_PARAM, limit);
    }
  }

  public static EntityResponse retrieveAIPDescriptiveMetadata(User user, String aipId, String metadataId,
    String acceptFormat, String language) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveAIPDescritiveMetadata(aipId, metadataId, acceptFormat, language);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
  }

  public static EntityResponse retrieveRepresentationDescriptiveMetadata(User user, String aipId,
    String representationId, String metadataId, String versionId, String acceptFormat, String language)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, representation);

      // delegate
      if (versionId == null) {
        return BrowserHelper.retrieveRepresentationDescriptiveMetadata(representation.getAipId(),
          representation.getId(), metadataId, acceptFormat, language);
      } else {
        return BrowserHelper.retrieveRepresentationDescriptiveMetadataVersion(representation.getAipId(),
          representation.getId(), metadataId, versionId, acceptFormat, language);
      }
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static EntityResponse retrieveAIPDescriptiveMetadataVersion(User user, String aipId, String metadataId,
    String versionId, String acceptFormat, String language)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveAIPDescritiveMetadataVersion(aipId, metadataId, versionId, acceptFormat, language);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static EntityResponse listAIPPreservationMetadata(User user, String aipId, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.listAIPPreservationMetadata(aipId, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  public static EntityResponse retrieveAIPRepresentationPreservationMetadata(User user, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPRepresentationPreservationMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation rep = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, rep);

      // delegate
      return BrowserHelper.retrieveAIPRepresentationPreservationMetadata(rep.getAipId(), rep.getId(), startAgent,
        limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_START_AGENT_PARAM,
        startAgent, RodaConstants.CONTROLLER_LIMIT_AGENT_PARAM, limitAgent, RodaConstants.CONTROLLER_START_EVENT_PARAM,
        startEvent, RodaConstants.CONTROLLER_LIMIT_EVENT_PARAM, limitEvent, RodaConstants.CONTROLLER_START_FILE_PARAM,
        startFile, RodaConstants.CONTROLLER_LIMIT_FILE_PARAM, limitFile);
    }
  }

  public static EntityResponse retrieveAIPRepresentationPreservationMetadataFile(User user, String fileUUID,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      return BrowserHelper.retrieveAIPRepresentationPreservationMetadataFile(file.getAipId(),
        file.getRepresentationId(), file.getPath(), file.getId(), acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }

  public static EntityResponse retrievePreservationMetadataEvent(User user, String id, String aipId,
    String representationUUID, String fileUUID, boolean onlyDetails, String acceptFormat, String language)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetPreservationMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      if (aipId != null) {
        IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(user, aip);
      }

      // delegate
      return BrowserHelper.retrievePreservationMetadataEvent(id, aipId, representationUUID, fileUUID, onlyDetails,
        acceptFormat, language);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_UUID_PARAM, representationUUID,
        RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }

  public static EntityResponse retrievePreservationMetadataAgent(User user, String id, String acceptFormat)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetPreservationMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrievePreservationMetadataAgent(id, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_AGENT_ID_PARAM, id);
    }
  }

  public static void createOrUpdatePreservationMetadataWithAIP(User user, String aipId, String fileId, InputStream is,
    String fileName, boolean create) throws AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, AlreadyExistsException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      String id = fileId == null ? fileName : fileId;

      // delegate
      BrowserHelper.createOrUpdateAIPRepresentationPreservationMetadataFile(aipId, null, new ArrayList<>(), id, is,
        create);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId, RodaConstants.CONTROLLER_FILENAME_PARAM, fileName);
    }
  }

  public static void createOrUpdatePreservationMetadataWithRepresentation(User user, String aipId,
    String representationId, String fileId, InputStream is, String fileName, boolean create)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation rep = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, rep);

      String id = fileId == null ? fileName : fileId;

      // delegate
      BrowserHelper.createOrUpdateAIPRepresentationPreservationMetadataFile(rep.getAipId(), rep.getId(),
        new ArrayList<>(), id, is, create);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_FILE_ID_PARAM,
        fileId, RodaConstants.CONTROLLER_FILENAME_PARAM, fileName);
    }
  }

  public static void createOrUpdatePreservationMetadataWithFile(User user, String fileUUID, InputStream is,
    boolean create) throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      BrowserHelper.createOrUpdateAIPRepresentationPreservationMetadataFile(file.getAipId(), file.getRepresentationId(),
        file.getPath(), file.getId(), is, create);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }

  public static void deletePreservationMetadataWithAIP(User user, String aipId, String id, String type)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deletePreservationMetadataFile(PreservationMetadataType.valueOf(type), aipId, null, id, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_ID_PARAM, id, RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static void deletePreservationMetadataWithRepresentation(User user, String aipId, String representationId,
    String id, String type)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation rep = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, rep);

      // delegate
      BrowserHelper.deletePreservationMetadataFile(PreservationMetadataType.valueOf(type), rep.getAipId(), rep.getId(),
        id, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_ID_PARAM, id,
        RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static EntityResponse retrieveOtherMetadata(User user, String aipId, String representationId, String type,
    String suffix, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetOtherMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveOtherMetadata(aipId, representationId, null, null, type, suffix, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static EntityResponse retrieveOtherMetadata(User user, String fileUUID, String type, String suffix,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetOtherMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
        file.getId(), type, suffix, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID,
        RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static void createOrUpdateOtherMetadata(User user, String aipId, String representationId, String type,
    InputStream is, String fileName)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      String name = fileName;
      if (name.contains(".")) {
        name = name.substring(name.lastIndexOf('.'));
      }

      // delegate
      BrowserHelper.createOrUpdateOtherMetadataFile(aipId, representationId, null, null, type, name, is);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type,
        RodaConstants.CONTROLLER_FILENAME_PARAM, fileName);
    }
  }

  public static void createOrUpdateOtherMetadata(User user, String fileUUID, String type, InputStream is,
    String fileName)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      String name = fileName;
      if (name.contains(".")) {
        name = name.substring(name.lastIndexOf('.'));
      }

      // delegate
      BrowserHelper.createOrUpdateOtherMetadataFile(file.getAipId(), file.getRepresentationId(), file.getPath(),
        file.getId(), type, name, is);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID,
        RodaConstants.CONTROLLER_TYPE_PARAM, type, RodaConstants.CONTROLLER_FILENAME_PARAM, fileName);
    }
  }

  public static void deleteOtherMetadata(User user, String aipId, String representationId, String suffix, String type)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteOtherMetadataFile(aipId, representationId, null, null, suffix, type);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  public static void deleteOtherMetadata(User user, String fileUUID, String suffix, String type)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteOtherMetadataFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
        suffix, type);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID,
        RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */

  public static Job moveAIPInHierarchy(User user, SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, selected);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      if (parentId != null) {
        IndexedAIP parentAip = BrowserHelper.retrieve(IndexedAIP.class, parentId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(user, parentAip);
      }

      // delegate
      return BrowserHelper.moveAIPInHierarchy(user, selected, parentId, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, parentId, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TO_PARENT_PARAM, parentId);
    }
  }

  public static AIP createAIP(User user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    if (parentId == null) {
      return createAIPTop(user, type);
    } else {
      return createAIPBelow(user, parentId, type);
    }
  }

  private static AIP createAIPTop(User user, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      Permissions permissions = new Permissions();
      permissions.setUserPermissions(user.getId(), new HashSet<>(Arrays.asList(PermissionType.values())));

      // delegate
      return BrowserHelper.createAIP(user, null, type, permissions);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  private static AIP createAIPBelow(User user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      Permissions permissions = new Permissions();

      if (parentId != null) {
        IndexedAIP parentSDO = BrowserHelper.retrieve(IndexedAIP.class, parentId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(user, parentSDO);
        Permissions parentPermissions = parentSDO.getPermissions();

        for (String name : parentPermissions.getUsernames()) {
          permissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
        }

        for (String name : parentPermissions.getGroupnames()) {
          permissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
        }
      } else {
        throw new RequestNotValidException("Creating AIP that should be below another with a null parentId");
      }

      permissions.setUserPermissions(user.getId(), new HashSet<>(Arrays.asList(PermissionType.values())));

      // delegate
      return BrowserHelper.createAIP(user, parentId, type, permissions);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_PARENT_ID_PARAM, parentId,
        RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static AIP updateAIP(User user, AIP aip)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP indexedAip = BrowserHelper.retrieve(IndexedAIP.class, aip.getId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, indexedAip);

      // delegate
      return BrowserHelper.updateAIP(user, aip);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aip.getId(), state, RodaConstants.CONTROLLER_AIP_PARAM, aip);
    }
  }

  public static Job deleteAIP(User user, SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, aips);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.deleteAIP(user, aips, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, aips);
    }
  }

  public static Job deleteRepresentation(User user, SelectedItems<IndexedRepresentation> representations,
    String details) throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, representations);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.deleteRepresentation(user, representations, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, representations);
    }
  }

  public static Job deleteFile(User user, SelectedItems<IndexedFile> files, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, files);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.deleteFile(user, files, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, files);
    }
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(User user, String aipId, String representationId,
    String metadataId, String metadataType, String metadataVersion, ContentPayload metadataPayload)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.createDescriptiveMetadataFile(aipId, representationId, metadataId, metadataType,
        metadataVersion, metadataPayload);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, metadataType, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        metadataVersion);
    }
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(User user, String aipId, String representationId,
    String metadataId, String metadataType, String metadataVersion, ContentPayload metadataPayload)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      return BrowserHelper.updateDescriptiveMetadataFile(aipId, representationId, metadataId, metadataType,
        metadataVersion, metadataPayload, properties);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static void deleteDescriptiveMetadataFile(User user, String aipId, String representationId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteDescriptiveMetadataFile(aipId, representationId, metadataId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static void deleteRepresentationDescriptiveMetadataFile(User user, String aipId, String representationId,
    String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteDescriptiveMetadataFile(aipId, representationId, metadataId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static Representation createRepresentation(User user, String aipId, String representationId, String type,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.createRepresentation(user, aipId, representationId, type, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static Representation updateRepresentation(User user, Representation representation)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.updateRepresentation(representation);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, representation.getAipId(), state,
        RodaConstants.CONTROLLER_REPRESENTATION_PARAM, representation);
    }
  }

  public static EntityResponse retrieveAIPRepresentationFile(User user, String fileUUID, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetFileParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
      fileFields.add(RodaConstants.FILE_ISDIRECTORY);
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, fileFields);
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      return BrowserHelper.retrieveAIPRepresentationFile(file, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }

  public static DescriptiveMetadata updateAIPDescriptiveMetadataFile(User user, String aipId, String metadataId,
    String metadataType, String metadataVersion, InputStream is) throws GenericException, AuthorizationDeniedException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      return BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(aipId, null, metadataId, metadataType,
        metadataVersion, properties, is, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
  }

  public static DescriptiveMetadata updateRepresentationDescriptiveMetadataFile(User user, String aipId,
    String representationId, String metadataId, String metadataType, String metadataVersion, InputStream is)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, representation);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      return BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(representation.getAipId(), representation.getId(),
        metadataId, metadataType, metadataVersion, properties, is, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, metadataType, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        metadataVersion);
    }
  }

  public static DescriptiveMetadata createAIPDescriptiveMetadataFile(User user, String aipId, String metadataId,
    String metadataType, String metadataVersion, InputStream is) throws GenericException, AuthorizationDeniedException,
    NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.CREATED.toString());

      return BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(aipId, null, metadataId, metadataType,
        metadataVersion, properties, is, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, metadataType,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, metadataVersion);
    }
  }

  public static DescriptiveMetadata createRepresentationDescriptiveMetadataFile(User user, String aipId,
    String representationId, String metadataId, String metadataType, String metadataVersion, InputStream is)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, representation);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.CREATED.toString());

      return BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(representation.getAipId(), representation.getId(),
        metadataId, metadataType, metadataVersion, properties, is, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, metadataType, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        metadataVersion);
    }
  }

  public static TransferredResource createTransferredResourcesFolder(User user, String parentUUID, String folderName,
    boolean forceCommit)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check user permissions
    controllerAssistant.checkRoles(user);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      TransferredResource transferredResource = BrowserHelper.createTransferredResourcesFolder(parentUUID, folderName,
        forceCommit);

      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_PARENT_PARAM,
        parentUUID, RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_SUCCESS_PARAM,
        true);
      return transferredResource;
    } catch (GenericException e) {
      // register action
      // FIXME nvieira 20170518: does this make sense to register with SUCCESS?
      // and the other exception, should they be treated differently?
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_PARENT_PARAM,
        parentUUID, RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_SUCCESS_PARAM,
        false, RodaConstants.CONTROLLER_ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void deleteTransferredResources(User user, SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.deleteTransferredResources(selected, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static TransferredResource reindexTransferredResource(User user, String path) throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.reindexTransferredResource(path);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_PATH_PARAM, path);
    }
  }

  public static TransferredResource createTransferredResourceFile(User user, String parentUUID, String fileName,
    InputStream inputStream, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
    AlreadyExistsException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      TransferredResource transferredResource = BrowserHelper.createTransferredResourceFile(parentUUID, fileName,
        inputStream, forceCommit);

      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_PATH_PARAM, parentUUID,
        RodaConstants.CONTROLLER_FILENAME_PARAM, fileName, RodaConstants.CONTROLLER_SUCCESS_PARAM, true);

      return transferredResource;
    } catch (GenericException e) {
      // register action
      // FIXME nvieira 20170518: does this make sense to register with SUCCESS?
      // and the other exception, should they be treated differently?
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_PATH_PARAM, parentUUID,
        RodaConstants.CONTROLLER_FILENAME_PARAM, fileName, RodaConstants.CONTROLLER_SUCCESS_PARAM, false,
        RodaConstants.CONTROLLER_ERROR_PARAM, e.getMessage());
      throw e;
    }

  }

  public static ConsumesOutputStream retrieveClassificationPlan(User user, String filename)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveClassificationPlan(user, filename);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  public static void updateTransferredResources(User user, Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.updateTransferredResources(folderRelativePath, waitToFinish);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      if (folderRelativePath.isPresent()) {
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FOLDER_RELATIVEPATH_PARAM,
          folderRelativePath.get());
      } else {
        controllerAssistant.registerAction(user, state);
      }
    }
  }

  public static void updateTransferredResource(User user, Optional<String> folderRelativePath, InputStream is,
    String name, boolean waitToFinish)
    throws IsStillUpdatingException, AuthorizationDeniedException, GenericException, IOException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    Path filePath = Files.createTempFile("descriptive", ".tmp");

    try {
      // delegate
      Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(filePath);
      BrowserHelper.updateTransferredResource(folderRelativePath, payload, name, waitToFinish);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      if (folderRelativePath.isPresent()) {
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FOLDER_RELATIVEPATH_PARAM,
          folderRelativePath.get(), RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_NAME_PARAM, name);
      } else {
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_NAME_PARAM, name);
      }

      FSUtils.deletePath(filePath);
    }
  }

  public static List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(User user, String aipId,
    String representationId, Locale locale) throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      List<String> aipFields = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      aipFields.addAll(Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFields);
      IndexedRepresentation rep = null;

      if (representationId != null) {
        rep = BrowserHelper.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_ID));
      }

      // check object permissions
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveSupportedMetadata(user, aip, rep, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.LOCALE, locale);
    }
  }

  public static EntityResponse retrieveTransferredResource(User user, String resourceId, String acceptFormat)
    throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.retrieveTransferredResource(
        BrowserHelper.retrieve(TransferredResource.class, resourceId, new ArrayList<>()), acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, resourceId, state, RodaConstants.CONTROLLER_RESOURCE_ID_PARAM,
        resourceId);
    }
  }

  public static PreservationEventViewBundle retrievePreservationEventViewBundle(User user, String eventId)
    throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      PreservationEventViewBundle resource = BrowserHelper.retrievePreservationEventViewBundle(eventId);
      controllerAssistant.checkObjectPermissions(user, resource.getEvent());
      return resource;
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_INDEX_PRESERVATION_EVENT_ID_PARAM,
        eventId);
    }
  }

  public static void revertDescriptiveMetadataVersion(User user, String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

      BrowserHelper.revertDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId,
        properties);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static void deleteDescriptiveMetadataVersion(User user, String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static void updateAIPPermissions(User user, List<IndexedAIP> aips, Permissions permissions, String details,
    boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      for (IndexedAIP aip : aips) {
        controllerAssistant.checkObjectPermissions(user, aip);
        BrowserHelper.updateAIPPermissions(user, aip, permissions, details, recursive);
      }
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_AIPS_PARAM, aips,
        RodaConstants.CONTROLLER_PERMISSIONS_PARAM, permissions);
    }
  }

  public static void updateDIPPermissions(User user, List<IndexedDIP> dips, Permissions permissions, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      for (IndexedDIP dip : dips) {
        controllerAssistant.checkObjectPermissions(user, dip);
        BrowserHelper.updateDIPPermissions(dip, permissions, details);
      }
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIPS_PARAM, dips,
        RodaConstants.CONTROLLER_PERMISSIONS_PARAM, permissions);
    }
  }

  public static Risk createRisk(User user, Risk risk) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRisk(risk, user, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    }
  }

  public static void updateRisk(User user, Risk risk, int incidences)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      BrowserHelper.updateRisk(risk, user, properties, true, incidences);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, risk.getId(), state, RodaConstants.CONTROLLER_RISK_PARAM, risk,
        RodaConstants.CONTROLLER_MESSAGE_PARAM, RodaConstants.VersionAction.UPDATED.toString());
    }
  }

  public static void revertRiskVersion(User user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

      int incidences = 0;

      try {
        IndexedRisk indexedRisk = RodaCoreFactory.getIndexService().retrieve(IndexedRisk.class, riskId,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCES_COUNT));
        incidences = indexedRisk.getIncidencesCount();
      } catch (NotFoundException e) {
        // do nothing
      }

      BrowserHelper.revertRiskVersion(riskId, versionId, properties, incidences);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId, RodaConstants.CONTROLLER_MESSAGE_PARAM,
        RodaConstants.VersionAction.REVERTED.toString());
    }
  }

  public static void deleteRiskVersion(User user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRiskVersion(riskId, versionId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static RiskVersionsBundle retrieveRiskVersions(User user, String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRiskVersions(riskId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static boolean hasRiskVersions(User user, String riskId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.hasRiskVersions(riskId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static Risk retrieveRiskVersion(User user, String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRiskVersion(riskId, selectedVersion);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_SELECTED_VERSION_PARAM, selectedVersion);
    }
  }

  public static RiskMitigationBundle retrieveShowMitigationTerms(User user, int preMitigationProbability,
    int preMitigationImpact, int posMitigationProbability, int posMitigationImpact)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RiskMitigationBundle ret = BrowserHelper.retrieveShowMitigationTerms(preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS,
      RodaConstants.CONTROLLER_PRE_MITIGATION_PROBABILITY_PARAM, preMitigationProbability,
      RodaConstants.CONTROLLER_PRE_MITIGATION_IMPACT_PARAM, preMitigationImpact,
      RodaConstants.CONTROLLER_POS_MITIGATION_PROBABILITY_PARAM, posMitigationProbability,
      RodaConstants.CONTROLLER_POS_MITIGATION_IMPACT_PARAM, posMitigationImpact);

    return ret;
  }

  public static List<String> retrieveMitigationSeverityLimits(User user) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<String> ret = BrowserHelper.retrieveShowMitigationTerms();

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return ret;
  }

  public static MitigationPropertiesBundle retrieveAllMitigationProperties(User user)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    MitigationPropertiesBundle ret = BrowserHelper.retrieveAllMitigationProperties();

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return ret;
  }

  public static Job deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteRisk(user, selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void updateRiskCounters(User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.updateRiskCounters();
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static void appraisal(User user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason,
    Locale locale) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, selected);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.appraisal(user, selected, accept, rejectReason, locale);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_ACCEPT_PARAM, accept, RodaConstants.CONTROLLER_REJECT_REASON_PARAM, rejectReason);
    }
  }

  public static String retrieveDescriptiveMetadataPreview(User user, SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataPreview(bundle);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TEMPLATE_PARAM, bundle.getLabel());
    }
  }

  public static String renameTransferredResource(User user, String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.renameTransferredResource(transferredResourceId, newName);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_ID_PARAM,
        transferredResourceId, RodaConstants.CONTROLLER_FILENAME_PARAM, newName);
    }
  }

  public static IndexedFile renameFolder(User user, String folderUUID, String newName, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.renameFolder(user, folderUUID, newName, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, folderUUID,
        RodaConstants.CONTROLLER_FOLDERNAME_PARAM, newName);
    }
  }

  public static Job moveFiles(User user, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.moveFiles(user, aipId, representationId, selectedFiles, toFolder, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_FILES_PARAM,
        selectedFiles, RodaConstants.CONTROLLER_FILE_PARAM, toFolder);
    }
  }

  public static IndexedFile createFolder(User user, String aipId, String representationId, String folderUUID,
    String newName, String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    AlreadyExistsException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createFolder(user, aipId, representationId, folderUUID, newName, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_FILE_UUID_PARAM,
        folderUUID, RodaConstants.CONTROLLER_FOLDERNAME_PARAM, newName);
    }
  }

  public static Job moveTransferredResource(User user, SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.moveTransferredResource(user, selected, transferredResource);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_PARAM, transferredResource);
    }
  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(User user,
    SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveSelectedTransferredResource(selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static File createFile(User user, String aipId, String representationId, List<String> directoryPath,
    String fileId, InputStream is, String details) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Path file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      return BrowserHelper.createFile(user, aipId, representationId, directoryPath, fileId, payload, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      BrowserHelper.commit(IndexedFile.class);

      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId,
        RodaConstants.CONTROLLER_DIRECTORY_PATH_PARAM, directoryPath, RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId);
    }
  }

  public static File updateFile(User user, File file, InputStream is, boolean createIfNotExists, boolean notify)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Path temp = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(temp);
      return BrowserHelper.updateFile(file, payload, createIfNotExists, notify);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, file.getAipId(), state, RodaConstants.CONTROLLER_FILE_PARAM, file);
    }
  }

  public static void deleteFile(User user, String fileUUID, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      BrowserHelper.deleteFile(user, SelectedItemsList.create(IndexedFile.class, fileUUID), details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }

  public static void updateRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.updateRiskIncidence(incidence);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidence.getId(), state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidence);
    }
  }

  public static void updateMultipleIncidences(User user, SelectedItems<RiskIncidence> selected, String status,
    String severity, Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.updateMultipleIncidences(selected, status, severity, mitigatedOn, mitigatedBy,
        mitigatedDescription);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void deleteRiskIncidences(User user, SelectedItems<RiskIncidence> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.deleteRiskIncidences(user, selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static DIP createDIP(User user, DIP dip) throws GenericException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.createDIP(dip);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIP_PARAM, dip);
    }
  }

  public static DIP updateDIP(User user, DIP dip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateDIP(dip);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, dip.getId(), state, RodaConstants.CONTROLLER_DIP_PARAM, dip);
    }
  }

  public static void deleteDIPs(User user, SelectedItems<IndexedDIP> dips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteDIPs(dips, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, dips);
    }
  }

  public static EntityResponse retrieveDIP(User user, String dipId, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetDIPParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      IndexedDIP dip = RodaCoreFactory.getIndexService().retrieve(IndexedDIP.class, dipId,
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, dip);

      // delegate
      return BrowserHelper.retrieveDIP(dipId, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, dipId, state, RodaConstants.DIP_ID, dipId);
    }
  }

  public static EntityResponse retrieveDIPFile(User user, String dipFileUUID, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetFileParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      DIPFile file = RodaCoreFactory.getIndexService().retrieve(DIPFile.class, dipFileUUID,
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIPFILE_ID, RodaConstants.DIPFILE_DIP_ID));
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      return BrowserHelper.retrieveDIPFile(dipFileUUID, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.DIPFILE_UUID, dipFileUUID);
    }
  }

  public static DIPFile createDIPFile(User user, String dipId, List<String> directoryPath, String fileId, long size,
    InputStream is) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    AlreadyExistsException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    Path file = Files.createTempFile("descriptive", ".tmp");

    try {
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      // delegate
      return BrowserHelper.createDIPFile(dipId, directoryPath, fileId, size, payload, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      BrowserHelper.commit(DIPFile.class);
      FSUtils.deletePath(file);
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIP_ID_PARAM, dipId,
        RodaConstants.CONTROLLER_DIRECTORY_PATH_PARAM, directoryPath, RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId);
    }
  }

  public static DIPFile createDIPFileWithParentUUID(User user, String parentUUID, String filename, long size,
    InputStream is) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    AlreadyExistsException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    Path file = Files.createTempFile("descriptive", ".tmp");

    try {
      // delegate
      DIPFile dipFile = BrowserHelper.retrieve(DIPFile.class, parentUUID, RodaConstants.DIPFILE_FIELDS_TO_RETURN);

      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);
      List<String> newFileDirectoryPath = dipFile.getPath();
      newFileDirectoryPath.add(dipFile.getId());

      return BrowserHelper.createDIPFile(dipFile.getDipId(), newFileDirectoryPath, filename, size, payload, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      BrowserHelper.commit(DIPFile.class);
      FSUtils.deletePath(file);
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIP_FILE_UUID_PARAM, parentUUID,
        RodaConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  public static DIPFile updateDIPFile(User user, String fileUUID, String filename, long size, InputStream is)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    AlreadyExistsException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    Path file = Files.createTempFile("descriptive", ".tmp");

    try {
      // delegate
      DIPFile dipFile = BrowserHelper.retrieve(DIPFile.class, fileUUID, RodaConstants.DIPFILE_FIELDS_TO_RETURN);

      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      return BrowserHelper.updateDIPFile(dipFile.getDipId(), dipFile.getPath(), dipFile.getId(), filename, size,
        payload, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIP_FILE_UUID_PARAM, fileUUID,
        RodaConstants.CONTROLLER_FILENAME_PARAM, filename);
      FSUtils.deletePath(file);
    }
  }

  public static void deleteDIPFile(User user, SelectedItems<DIPFile> files)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteDIPFiles(files, user);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, files);
    }
  }

  public static EntityResponse retrieveRepresentationInformation(User user, String representationInformationId,
    String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetFileParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      RepresentationInformation ri = RodaCoreFactory.getIndexService().retrieve(RepresentationInformation.class,
        representationInformationId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(user, ri);

      // delegate
      return BrowserHelper.retrieveRepresentationInformation(representationInformationId, acceptFormat);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.REPRESENTATION_INFORMATION_ID,
        representationInformationId);
    }
  }

  public static void createFormatIdentificationJob(User user, SelectedItems<?> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.createFormatIdentificationJob(user, selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void changeAIPType(User user, SelectedItems<IndexedAIP> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.changeAIPType(user, selected, newType, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TYPE_PARAM, newType);
    }
  }

  public static void changeRepresentationType(User user, SelectedItems<IndexedRepresentation> selected, String newType,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.changeRepresentationType(user, selected, newType, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TYPE_PARAM, newType);
    }
  }

  public static void changeRepresentationStates(User user, IndexedRepresentation representation, List<String> newStates,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.changeRepresentationStates(user, representation, newStates, details);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_PARAM, representation,
        RodaConstants.CONTROLLER_STATES_PARAM, newStates);
    }
  }

  public static ObjectPermissionResult verifyPermissions(User user, String username, String permissionType,
    MultivaluedMap<String, String> queryParams)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.verifyPermissions(username, permissionType, queryParams);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USERNAME_PARAM, username,
        RodaConstants.CONTROLLER_PERMISSION_TYPE_PARAM, permissionType, RodaConstants.CONTROLLER_QUERY_PARAMS,
        queryParams);
    }
  }

  public static boolean hasDocumentation(User user, String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.hasDocumentation(aipId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  public static Notification acknowledgeNotification(User user, String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // 20170515 nvieira: decided to not check roles considering the ackToken
    // should be enough and it is not necessary nor usable to create a new role
    // only for this purpose
    // controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.acknowledgeNotification(notificationId, ackToken);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, notificationId, state, RodaConstants.CONTROLLER_NOTIFICATION_ID_PARAM,
        notificationId, RodaConstants.CONTROLLER_NOTIFICATION_TOKEN_PARAM, ackToken);
    }
  }

  public static Reports listReports(User user, String id, String resourceOrSip, int start, int limit,
    String acceptFormat) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListingParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      Reports reportList;

      if (id == null || resourceOrSip == null) {
        reportList = BrowserHelper.listReports(start, limit);
      } else if (RodaConstants.CONTROLLER_SIP_PARAM.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReportsWithSIP(id, start, limit);
      } else if (RodaConstants.CONTROLLER_ID_OBJECT_RESOURCE_PATH.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReports(IdUtils.getTransferredResourceUUID(id), start,
          limit);
      } else if (RodaConstants.CONTROLLER_ID_OBJECT_SOURCE_NAME.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReportsWithSourceOriginalName(id, start, limit);
      } else {
        reportList = BrowserHelper.listTransferredResourcesReports(id, start, limit);
      }

      return reportList;
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_ID_PARAM, id,
        RodaConstants.CONTROLLER_ID_OBJECT_PARAM, resourceOrSip, RodaConstants.CONTROLLER_START_PARAM, start,
        RodaConstants.CONTROLLER_LIMIT_PARAM, limit);
    }
  }

  public static Report lastReport(User user, String id, String resourceOrSip, String acceptFormat)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListingParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      Reports reportList;

      if (id == null || resourceOrSip == null) {
        reportList = BrowserHelper.listReports(0, 1);
      } else if (RodaConstants.CONTROLLER_SIP_PARAM.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReportsWithSIP(id, 0, 1);
      } else if (RodaConstants.CONTROLLER_ID_OBJECT_RESOURCE_PATH.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReports(IdUtils.getTransferredResourceUUID(id), 0, 1);
      } else if (RodaConstants.CONTROLLER_ID_OBJECT_SOURCE_NAME.equals(resourceOrSip)) {
        reportList = BrowserHelper.listTransferredResourcesReportsWithSourceOriginalName(id, 0, 1);
      } else {
        reportList = BrowserHelper.listTransferredResourcesReports(id, 0, 1);
      }

      if (reportList.getObjects().isEmpty()) {
        throw new NotFoundException("Could not find report for value: " + id);
      } else {
        return reportList.getObjects().get(0);
      }
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_ID_PARAM, id,
        RodaConstants.CONTROLLER_ID_OBJECT_PARAM, resourceOrSip, RodaConstants.CONTROLLER_START_PARAM, 0,
        RodaConstants.CONTROLLER_LIMIT_PARAM, 1);
    }
  }

  public static RepresentationInformation createRepresentationInformation(User user, RepresentationInformation ri,
    RepresentationInformationExtraBundle extra) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRepresentationInformation(ri, extra, user.getName(), true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM,
        ri.getId());
    }
  }

  public static void updateRepresentationInformation(User user, RepresentationInformation ri,
    RepresentationInformationExtraBundle extra) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.updateRepresentationInformation(ri, extra, user.getName(), true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, ri.getId(), state,
        RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM, ri.getId());
    }
  }

  public static Job deleteRepresentationInformation(User user, SelectedItems<RepresentationInformation> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteRepresentationInformation(user, selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static Pair<String, Integer> retrieveRepresentationInformationWithFilter(User user, String riFilter)
    throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRepresentationInformationWithFilter(riFilter);
    } catch (GenericException | RequestNotValidException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_FILTER_PARAM,
        riFilter);
    }
  }

  public static RepresentationInformationFilterBundle retrieveObjectClassFields(User user, Messages messages)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);
    return BrowserHelper.retrieveObjectClassFields(messages);
  }

  public static Format createFormat(User user, Format format) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createFormat(format, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FORMAT_PARAM, format);
    }
  }

  public static void updateFormat(User user, Format format) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      BrowserHelper.updateFormat(format, true);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, format.getId(), state, RodaConstants.CONTROLLER_FORMAT_PARAM, format);
    }
  }

  public static Job deleteFormat(User user, SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteFormat(user, selected);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static RelationTypeTranslationsBundle retrieveRelationTypeTranslations(User user, Messages messages)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);
    return BrowserHelper.retrieveRelationTypeTranslations(messages);
  }

  public static void updateRepresentationInformationListWithFilter(User user,
    SelectedItemsList<RepresentationInformation> representationInformationIds, String filterToAdd)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    boolean resultSuccess = BrowserHelper.updateRepresentationInformationListWithFilter(representationInformationIds,
      filterToAdd, user.getName());

    LOG_ENTRY_STATE state = resultSuccess ? LOG_ENTRY_STATE.SUCCESS : LOG_ENTRY_STATE.FAILURE;

    // register action
    controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_PARAM,
      representationInformationIds);
  }

  public static RepresentationInformationExtraBundle retrieveRepresentationInformationExtraBundle(User user,
    String representationInformationId, Locale locale) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    // check user permissions
    controllerAssistant.checkRoles(user);

    RepresentationInformationExtraBundle extra = null;
    try {
      extra = BrowserHelper.retrieveRepresentationInformationExtraBundle(representationInformationId, locale);
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      state = LOG_ENTRY_STATE.FAILURE;
    }

    // register action
    controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM,
      representationInformationId);
    return extra;
  }
}
