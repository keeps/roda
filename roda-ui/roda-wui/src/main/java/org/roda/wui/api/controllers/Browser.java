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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.storage.ContentPayload;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Browser extends RodaCoreService {

  private static final String AIP_PARAM = "aip";

  private static final String TRANSFERRED_RESOURCE_ID_PARAM = "transferredResourceId";

  private static final String INDEX_PRESERVATION_EVENT_ID = "indexedPreservationEventId";
  private static final String INDEX_PRESERVATION_AGENT_ID = "indexedPreservationAgentId";

  private static final String PARENT_PARAM = "parent";
  private static final String FOLDERNAME_PARAM = "folderName";
  private static final String FILENAME_PARAM = "filename";
  private static final String PATH_PARAM = "path";
  private static final Object CLASSIFICATION_PLAN_TYPE_PARAMETER = "classificationPlanType";

  private static final Object SUCCESS_PARAM = "success";

  private static final Object ERROR_PARAM = "error";

  private Browser() {
    super();
  }

  public static BrowseItemBundle retrieveItemBundle(RodaUser user, String aipId, Locale locale)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    BrowseItemBundle itemBundle = BrowserHelper.retrieveItemBundle(aipId, locale);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId);

    return itemBundle;
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(RodaUser user, String aipId,
    String metadataId, String type, String version)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadataEditBundle bundle = BrowserHelper.retrieveDescriptiveMetadataEditBundle(user, aip, metadataId,
      type, version);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(RodaUser user, String aipId,
    String metadataId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadataEditBundle bundle = BrowserHelper.retrieveDescriptiveMetadataEditBundle(user, aip, metadataId);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(RodaUser user, String aipId,
    String metadataId, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadataVersionsBundle bundle = BrowserHelper.retrieveDescriptiveMetadataVersionsBundle(aipId,
      metadataId, locale);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final Filter filter,
    final Sorter sorter, final Sublist sublist, final Facets facets, final RodaUser user, final boolean justActive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // TODO check permissions for each class

    // delegate
    final IndexResult<T> ret = BrowserHelper.find(classToReturn, filter, sorter, sublist, facets, user, justActive);

    // register action

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "class", classToReturn.getSimpleName(),
      RodaConstants.CONTROLLER_FILTER_PARAM, filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter,
      RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return ret;
  }

  public static <T extends IsIndexed> Long count(final RodaUser user, final Class<T> classToReturn, final Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // TODO check permissions for each class

    // delegate
    final Long count = BrowserHelper.count(classToReturn, filter, user);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "class", classToReturn.getSimpleName(),
      RodaConstants.CONTROLLER_FILTER_PARAM, filter.toString());

    return count;
  }

  public static <T extends IsIndexed> T retrieve(final RodaUser user, final Class<T> classToReturn, final String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() { };

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // TODO check object level permissions

    // delegate
    final T ret = BrowserHelper.retrieve(classToReturn, id);

    // register action
    String aipId = null;
    if (classToReturn.equals(IndexedAIP.class)) {
      aipId = id;
    }
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, "class", classToReturn.getSimpleName());

    return ret;
  }

  public static <T extends IsIndexed> List<T> retrieve(final RodaUser user, final Class<T> classToReturn,
    final SelectedItems<T> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);
    final List<T> objects = BrowserHelper.retrieve(classToReturn, selectedItems);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selectedItems", selectedItems);

    return objects;
  }

  public static <T extends IsIndexed> void delete(final RodaUser user, final Class<T> classToReturn,
    final SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // TODO check object level permissions

    // delegate
    BrowserHelper.delete(user, classToReturn, ids);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "class", classToReturn.getSimpleName());
  }

  public static <T extends IsIndexed> List<String> suggest(final RodaUser user, final Class<T> classToReturn,
    final String field, final String query) throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    // TODO object level permissions

    // delegate
    final List<String> ret = BrowserHelper.suggest(classToReturn, field, query);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "class", classToReturn.getSimpleName(), "field",
      field, "query", query);

    return ret;
  }

  public static List<IndexedAIP> retrieveAncestors(RodaUser user, IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    List<IndexedAIP> ancestors = BrowserHelper.retrieveAncestors(aip);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, AIP_PARAM, aip.toString());

    return ancestors;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static StreamResponse retrieveAIPRepresentation(RodaUser user, String representationUUID, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPRepresentationParams(acceptFormat);

    IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class, representationUUID);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.retrieveAIPRepresentation(representation, acceptFormat);

    // register action
    controllerAssistant.registerAction(user, representation.getAipId(), LOG_ENTRY_STATE.SUCCESS,
      RodaConstants.REPRESENTATION_ID, representation.getId());

    return aipRepresentation;
  }

  public static StreamResponse retrieveAIPRepresentationPart(RodaUser user, String representationUUID, String part)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class, representationUUID);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.retrieveAIPRepresentationPart(representation, part);

    // register action
    controllerAssistant.registerAction(user, representation.getAipId(), LOG_ENTRY_STATE.SUCCESS,
      RodaConstants.REPRESENTATION_ID, representation.getId(), "part", part);

    return aipRepresentation;
  }

  public static StreamResponse listAIPDescriptiveMetadata(RodaUser user, String aipId, String start, String limit,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescriptiveMetadataList = BrowserHelper.listAIPDescriptiveMetadata(aipId, start, limit);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_QUERY_KEY_START, start, RodaConstants.API_QUERY_KEY_LIMIT, limit);

    return aipDescriptiveMetadataList;
  }

  public static StreamResponse retrieveAIPDescritiveMetadata(RodaUser user, String aipId, String metadataId,
    String acceptFormat, String language) throws AuthorizationDeniedException, GenericException, TransformerException,
    NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPDescritiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.retrieveAIPDescritiveMetadata(aipId, metadataId, acceptFormat,
      language);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_METADATA_ID,
      metadataId);

    return aipDescritiveMetadata;

  }

  public static StreamResponse retrieveAIPDescritiveMetadataVersion(RodaUser user, String aipId, String metadataId,
    String versionId, String acceptFormat, String language) throws AuthorizationDeniedException, GenericException,
    TransformerException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPDescritiveMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.retrieveAIPDescritiveMetadataVersion(aipId, metadataId,
      versionId, acceptFormat, language);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_METADATA_ID,
      metadataId);

    return aipDescritiveMetadata;

  }

  public static StreamResponse listAIPPreservationMetadata(RodaUser user, String aipId, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateListAIPPreservationMetadataParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipPreservationMetadataList = BrowserHelper.aipsAIPIdPreservationMetadataGet(aipId);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId);

    return aipPreservationMetadataList;
  }

  public static StreamResponse retrieveAIPRepresentationPreservationMetadata(RodaUser user, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat, String language) throws AuthorizationDeniedException,
    GenericException, TransformerException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPRepresentationPreservationMetadataParams(acceptFormat, language);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationPreservationMetadata = BrowserHelper.retrieveAIPRepresentationPreservationMetadata(
      aipId, representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat,
      language);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      "startAgent", startAgent, "limitAgent", limitAgent, "startEvent", startEvent, "limitEvent", limitEvent,
      "startFile", startFile, "limitFile", limitFile);

    return aipRepresentationPreservationMetadata;

  }

  public static StreamResponse retrieveAIPRepresentationPreservationMetadataFile(RodaUser user, String aipId,
    String representationId, String fileId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationPreservationMetadataFile = BrowserHelper
      .retrieveAIPRepresentationPreservationMetadataFile(aipId, representationId, fileId);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId, RodaConstants.API_PATH_PARAM_FILE_UUID, fileId);

    return aipRepresentationPreservationMetadataFile;
  }

  public static void postAIPRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, FormDataContentDisposition fileDetail)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException, AlreadyExistsException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    BrowserHelper.createOrUpdateAIPRepresentationPreservationMetadataFile(aipId, representationId, fileDirectoryPath,
      fileId, is, fileDetail, true);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);
  }

  public static void putAIPRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, FormDataContentDisposition fileDetail)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    BrowserHelper.createOrUpdateAIPRepresentationPreservationMetadataFile(aipId, representationId, fileDirectoryPath,
      fileId, is, fileDetail, false);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */

  public static IndexedAIP moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, RodaUser user)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    UserUtility.checkObjectPermissions(user, selected, PermissionType.UPDATE);

    if (parentId != null) {
      IndexedAIP parentAip = BrowserHelper.retrieve(IndexedAIP.class, parentId);
      UserUtility.checkObjectPermissions(user, parentAip, PermissionType.CREATE);
    }

    // delegate
    IndexedAIP returnAIP = BrowserHelper.moveAIPInHierarchy(selected, parentId, user);

    // register action
    controllerAssistant.registerAction(user, parentId, LOG_ENTRY_STATE.SUCCESS, "selected", selected, "toParent",
      parentId);

    return returnAIP;
  }

  public static AIP createAIP(RodaUser user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    Permissions permissions = new Permissions();

    if (parentId != null) {
      IndexedAIP parentSDO = BrowserHelper.retrieve(IndexedAIP.class, parentId);
      UserUtility.checkObjectPermissions(user, parentSDO, PermissionType.CREATE);
      Permissions parentPermissions = parentSDO.getPermissions();

      for (String name : parentPermissions.getUsernames()) {
        permissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
      }

      for (String name : parentPermissions.getGroupnames()) {
        permissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
      }
    } else {
      // TODO check user role to create top-level AIPs
    }

    permissions.setUserPermissions(user.getId(), new HashSet<PermissionType>(Arrays.asList(PermissionType.values())));

    // delegate
    AIP aip = BrowserHelper.createAIP(parentId, type, permissions);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, "parentId", parentId);

    return aip;
  }

  public static String deleteAIP(RodaUser user, SelectedItems<IndexedAIP> aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    String parentId = BrowserHelper.deleteAIP(aips, user);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", aips);

    return parentId;
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, String metadataVersion, ContentPayload metadataPayload) throws AuthorizationDeniedException,
    GenericException, ValidationException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.createDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataVersion, metadataPayload);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return ret;
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, String metadataVersion, ContentPayload metadataPayload) throws AuthorizationDeniedException,
    GenericException, ValidationException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Updated by " + user.getName();
    DescriptiveMetadata ret = BrowserHelper.updateDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataVersion, metadataPayload, message);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return ret;
  }

  public static void deleteDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.deleteDescriptiveMetadataFile(aipId, metadataId);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);
  }

  public static DescriptiveMetadata retrieveDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadata dm = BrowserHelper.retrieveMetadataFile(aipId, metadataId);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return dm;
  }

  public static void deleteRepresentation(RodaUser user, String aipId, String representationId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.deleteRepresentation(aipId, representationId);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);
  }

  public static void deleteRepresentationFile(RodaUser user, String fileUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedFile file = BrowserHelper.retrieve(IndexedFile.class, fileUUID);
    // TODO check permissions from indexed file
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.deleteRepresentationFile(fileUUID);

    // register action
    controllerAssistant.registerAction(user, aip.getId(), LOG_ENTRY_STATE.SUCCESS, RodaConstants.FILE_AIPID,
      file.getAipId(), RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId(), RodaConstants.FILE_PATH,
      file.getPath(), RodaConstants.FILE_FILEID, file.getId());
  }

  public static StreamResponse retrieveAIPRepresentationFile(RodaUser user, String fileUuid, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPRepresentationFileParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);
    // TODO get permissions from indexed file
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationFile = BrowserHelper.retrieveAIPRepresentationFile(fileUuid, acceptFormat);

    // register action
    controllerAssistant.registerAction(user, file.getAipId(), LOG_ENTRY_STATE.SUCCESS,
      RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId(), RodaConstants.FILE_PATH, file.getPath(),
      RodaConstants.FILE_FILEID, file.getId());

    return aipRepresentationFile;
  }

  public static void putDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    String metadataVersion, InputStream is, FormDataContentDisposition fileDetail)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Updated by " + user.getName();
    BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(aipId, metadataId, metadataType, metadataVersion, message,
      is, fileDetail, false);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);
  }

  public static void postDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    String metadataVersion, InputStream is, FormDataContentDisposition fileDetail)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Created by " + user.getName();
    BrowserHelper.createOrUpdateAIPDescriptiveMetadataFile(aipId, metadataId, metadataType, metadataVersion, message,
      is, fileDetail, true);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);
  }

  public static TransferredResource createTransferredResourcesFolder(RodaUser user, String parentUUID,
    String folderName, boolean forceCommit)
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
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, PARENT_PARAM, parentUUID, FOLDERNAME_PARAM,
        folderName, SUCCESS_PARAM, true);
      return transferredResource;
    } catch (GenericException e) {
      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, PARENT_PARAM, parentUUID, FOLDERNAME_PARAM,
        folderName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void deleteTransferredResources(RodaUser user, SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteTransferredResources(selected, user);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected);
  }

  public static TransferredResource createTransferredResourceFile(RodaUser user, String parentUUID, String fileName,
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
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, PATH_PARAM, parentUUID, FILENAME_PARAM,
        fileName, SUCCESS_PARAM, true);

      return transferredResource;
    } catch (GenericException e) {
      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, PATH_PARAM, parentUUID, FILENAME_PARAM,
        fileName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }

  }

  public static StreamResponse retrieveClassificationPlan(RodaUser user, String type)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    // delegate
    StreamResponse classificationPlan = BrowserHelper.retrieveClassificationPlan(type, user);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, CLASSIFICATION_PLAN_TYPE_PARAMETER, type);

    return classificationPlan;
  }

  public static TransferredResource createTransferredResource(RodaUser user, String parentUUID, String fileName,
    InputStream inputStream, String name, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
    AlreadyExistsException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    TransferredResource transferredResource;
    if (name == null) {
      transferredResource = Browser.createTransferredResourceFile(user, parentUUID, fileName, inputStream, forceCommit);
    } else {
      transferredResource = Browser.createTransferredResourcesFolder(user, parentUUID, name, forceCommit);
    }

    // register action
    // TODO: what params should be registered?
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "parentUUID", parentUUID,
      "transferredResourceUUID", transferredResource.getUUID());

    return transferredResource;
  }

  public static boolean retrieveScanUpdateStatus() {
    // TODO: should this method use ControllerAssistant to checkRoles and
    // registerAction? Where is RodaUser?
    return BrowserHelper.retrieveScanUpdateStatus();
  }

  public static void updateAllTransferredResources(String subFolderUUID, boolean waitToFinish)
    throws IsStillUpdatingException {
    // TODO: should this method use ControllerAssistant to checkRoles and
    // registerAction? Where is RodaUser?
    BrowserHelper.runTransferredResourceScan(subFolderUUID, waitToFinish);
  }

  public static List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(RodaUser user, String aipId, Locale locale)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);

    // delegate
    List<SupportedMetadataTypeBundle> supportedMetadata = BrowserHelper.retrieveSupportedMetadata(user, aip, locale);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.LOCALE, locale);

    return supportedMetadata;
  }

  public static StreamResponse retrieveTransferredResource(RodaUser user, String resourceId)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    StreamResponse response = BrowserHelper
      .retrieveTransferredResource(BrowserHelper.retrieve(TransferredResource.class, resourceId));

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "resourceId", resourceId);

    return response;
  }

  public static PreservationEventViewBundle retrievePreservationEventViewBundle(RodaUser user, String eventId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // TODO maybe update permissions...
    // check user permissions
    controllerAssistant.checkRoles(user);
    // IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    // UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    PreservationEventViewBundle resource = BrowserHelper.retrievePreservationEventViewBundle(eventId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, INDEX_PRESERVATION_EVENT_ID, eventId);

    return resource;
  }

  public static void revertDescriptiveMetadataVersion(RodaUser user, String aipId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    // TODO externalize this message
    String message = "Reverted by " + user.getId();
    BrowserHelper.revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId, message);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, descriptiveMetadataId, RodaConstants.API_QUERY_PARAM_VERSION,
      versionId);
  }

  public static void deleteDescriptiveMetadataVersion(RodaUser user, String aipId, String descriptiveMetadataId,
    String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    BrowserHelper.deleteDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.API_PATH_PARAM_AIP_ID, aipId,
      RodaConstants.API_PATH_PARAM_METADATA_ID, descriptiveMetadataId, RodaConstants.API_QUERY_PARAM_VERSION,
      versionId);
  }

  public static void updateAIPPermissions(RodaUser user, List<IndexedAIP> aips, Permissions permissions,
    boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    for (IndexedAIP aip : aips) {
      UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);
      BrowserHelper.updateAIPPermissions(aip, permissions, recursive);
    }

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "aips", aips, "permissions", permissions);
  }

  public static <T extends IsIndexed> List<String> consolidate(final RodaUser user, final Class<T> classToReturn,
    final SelectedItems<T> selected) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user, classToReturn);

    final List<String> result = BrowserHelper.consolidate(user, classToReturn, selected);

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "classToReturn", classToReturn, "selectedClass",
      selected.getSelectedClass());

    return result;
  }

  public static void updateRisk(RodaUser user, Risk risk, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.updateRisk(risk, user, message, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "risk", risk);
  }

  public static void updateFormat(RodaUser user, Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.updateFormat(format, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "format", format);
  }

  public static void updateAgent(RodaUser user, Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.updateAgent(agent, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agent", agent);
  }

  public static Risk createRisk(RodaUser user, Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Risk ret = BrowserHelper.createRisk(risk, user, true);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "risk", risk);

    return ret;
  }

  public static Format createFormat(RodaUser user, Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Format ret = BrowserHelper.createFormat(format, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "format", format);

    return ret;
  }

  public static Agent createAgent(RodaUser user, Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Agent ret = BrowserHelper.createAgent(agent, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agent", agent);

    return ret;
  }

  public static List<Format> retrieveFormats(RodaUser user, String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<Format> ret = BrowserHelper.retrieveFormats(agentId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agentId", agentId);

    return ret;
  }

  public static List<Agent> retrieveRequiredAgents(RodaUser user, String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<Agent> ret = BrowserHelper.retrieveRequiredAgents(agentId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agentId", agentId);

    return ret;
  }

  public static void revertRiskVersion(RodaUser user, String riskId, String versionId, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.revertRiskVersion(riskId, versionId, message);

    // register action
    controllerAssistant.registerAction(user, versionId, LOG_ENTRY_STATE.SUCCESS, "riskId", riskId, "versionId",
      versionId, "message", message);
  }

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
  public static StreamResponse exportAIP(RodaUser user, SelectedItems<IndexedAIP> selected, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateExportAIPParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);

    UserUtility.checkObjectPermissions(user, selected, PermissionType.READ);

    // delegate
    StreamResponse aipExport = BrowserHelper.retrieveAIPs(selected, acceptFormat);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return aipExport;
  }

  public static StreamResponse retrieveAIP(RodaUser user, String aipId, String acceptFormat)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input
    BrowserHelper.validateGetAIPParams(acceptFormat);

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP indexedAIP = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, indexedAIP, PermissionType.READ);

    // delegate
    StreamResponse aip = BrowserHelper.retrieveAIP(indexedAIP, acceptFormat);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS);

    return aip;
  }

  public static StreamResponse retrieveAIPPart(RodaUser user, String aipId, String part)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    IndexedAIP indexedAIP = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, indexedAIP, PermissionType.READ);

    // delegate
    StreamResponse aip = BrowserHelper.retrieveAIPPart(indexedAIP, part);

    // register action
    controllerAssistant.registerAction(user, aipId, LOG_ENTRY_STATE.SUCCESS, "part", part);

    return aip;
  }

  public static void deleteRiskVersion(RodaUser user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteRiskVersion(riskId, versionId);

    // register action
    controllerAssistant.registerAction(user, versionId, LOG_ENTRY_STATE.SUCCESS, "riskId", riskId, "versionId",
      versionId);
  }

  public static RiskVersionsBundle retrieveRiskVersions(RodaUser user, String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RiskVersionsBundle ret = BrowserHelper.retrieveRiskVersions(riskId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "riskId", riskId);

    return ret;
  }

  public static boolean hasRiskVersions(RodaUser user, String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    boolean ret = BrowserHelper.hasRiskVersions(id);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "riskId", id);

    return ret;
  }

  public static Risk retrieveRiskVersion(RodaUser user, String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Risk ret = BrowserHelper.retrieveRiskVersion(riskId, selectedVersion);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "riskId", riskId, "selectedVersion",
      selectedVersion);

    return ret;
  }

  public static RiskMitigationBundle retrieveShowMitigationTerms(RodaUser user, int preMitigationProbability,
    int preMitigationImpact, int posMitigationProbability, int posMitigationImpact)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RiskMitigationBundle ret = BrowserHelper.retrieveShowMitigationTerms(preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "preMitigationProbability",
      preMitigationProbability, "preMitigationImpact", preMitigationImpact, "posMitigationProbability",
      posMitigationProbability, "posMitigationImpact", posMitigationImpact);

    return ret;
  }

  public static List<String> retrieveMitigationSeverityLimits(RodaUser user) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<String> ret = BrowserHelper.retrieveShowMitigationTerms();

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return ret;
  }

  public static MitigationPropertiesBundle retrieveAllMitigationProperties(RodaUser user)
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

  public static void deleteRisk(RodaUser user, SelectedItems<IndexedRisk> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteRisk(user, selected);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected);
  }

  public static void deleteAgent(RodaUser user, SelectedItems<Agent> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteAgent(user, selected);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected);
  }

  public static void deleteFormat(RodaUser user, SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteFormat(user, selected);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected);
  }

  public static void deleteRiskIncidences(RodaUser user, String id, SelectedItems<RiskIncidence> incidences)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteRiskIncidences(user, id, incidences);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "incidences", incidences);
  }

  public static void updateRiskCounters(RodaUser user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.updateRiskCounters();

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);
  }

  public static void appraisal(RodaUser user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.appraisal(user, selected, accept, rejectReason);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected, "accept", accept,
      "rejectReason", rejectReason);
  }

  public static IndexedRepresentation retrieveRepresentationById(RodaUser user, String representationId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    IndexedRepresentation ret = BrowserHelper.retrieveRepresentationById(user, representationId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "representationId", representationId);

    return ret;
  }

  public static IndexedFile retrieveFileById(RodaUser user, String fileId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    IndexedFile ret = BrowserHelper.retrieveFileById(user, fileId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "fileId", fileId);

    return ret;
  }

  public static String createDescriptiveMetadataPreview(RodaUser user, String aipId, SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    // delegate
    String payload = BrowserHelper.retrieveDescriptiveMetadataPreview(bundle);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "template", bundle.getLabel());

    return payload;
  }

  public static String renameTransferredResource(RodaUser user, String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    String ret = BrowserHelper.renameTransferredResource(transferredResourceId, newName);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "transferredResourceId", transferredResourceId);
    return ret;
  }

  public static String moveTransferredResource(RodaUser user, SelectedItems selected,
    TransferredResource transferredResource) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, AlreadyExistsException, IsStillUpdatingException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    String ret = BrowserHelper.moveTransferredResource(selected, transferredResource);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected, "transferredResource",
      transferredResource);
    return ret;
  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(RodaUser user,
    SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<TransferredResource> ret = BrowserHelper.retrieveSelectedTransferredResource(selected);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "selected", selected);
    return ret;
  }

  public static void deleteFile(RodaUser user, String fileUUID)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.deleteRepresentationFile(fileUUID);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "fileUUID", fileUUID);
  }

  public static void updateRiskIncidence(RodaUser user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    BrowserHelper.updateRiskIncidence(incidence);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "incidence", incidence);
  }

  public static void showLogs(RodaUser user) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    UserUtility.checkRoles(user, "administration.user");

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);
  }

}
