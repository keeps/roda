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
import java.util.Date;
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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
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
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Browser extends RodaCoreService {

  private static final String AIP_PARAM = "aip";

  private static final String BROWSER_COMPONENT = "Browser";
  private static final String ADMINISTRATION_METADATA_EDITOR_ROLE = "administration.metadata_editor";
  private static final String INGEST_TRANSFER_ROLE = "ingest.transfer";
  private static final String BROWSE_ROLE = "browse";

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

  public static BrowseItemBundle getItemBundle(RodaUser user, String aipId, Locale locale)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    BrowseItemBundle itemBundle = BrowserHelper.getItemBundle(aipId, locale);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getItemBundle", aipId, duration, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId);

    return itemBundle;
  }

  public static DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(RodaUser user, String aipId,
    String metadataId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadataEditBundle bundle = BrowserHelper.getDescriptiveMetadataEditBundle(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getDescriptiveMetadataEditBundle", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static DescriptiveMetadataVersionsBundle getDescriptiveMetadataVersionsBundle(RodaUser user, String aipId,
    String metadataId, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadataVersionsBundle bundle = BrowserHelper.getDescriptiveMetadataVersionsBundle(aipId, metadataId,
      locale);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getDescriptiveMetadataEditBundle", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, RodaUser user, boolean justActive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    // TODO check permissions for each class
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexResult<T> ret = BrowserHelper.find(classToReturn, filter, sorter, sublist, facets, user, justActive);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "find", null, duration, "class", classToReturn.getSimpleName(),
      RodaConstants.CONTROLLER_FILTER_PARAM, filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter,
      RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return ret;
  }

  public static <T extends IsIndexed> Long count(RodaUser user, Class<T> classToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    // TODO check permissions for each class
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    Long count = BrowserHelper.count(classToReturn, filter, user);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "count", null, duration, "class", classToReturn.getSimpleName(),
      RodaConstants.CONTROLLER_FILTER_PARAM, filter.toString());

    return count;
  }

  public static <T extends IsIndexed> T retrieve(RodaUser user, Class<T> classToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    // TODO check object level permissions

    // delegate
    T ret = BrowserHelper.retrieve(classToReturn, id);

    // register action
    String aipId = null;
    if (classToReturn.equals(IndexedAIP.class)) {
      aipId = id;
    }

    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieve", aipId, duration, "class", classToReturn.getSimpleName());

    return ret;
  }

  public static <T extends IsIndexed> void delete(RodaUser user, Class<T> classToReturn, SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    // TODO check object level permissions

    // delegate
    BrowserHelper.delete(user, classToReturn, ids);

    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "delete", null, duration, "class", classToReturn.getSimpleName());
  }

  public static <T extends IsIndexed> List<String> suggest(RodaUser user, Class<T> classToReturn, String field,
    String query) throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    // TODO object level permissions

    // delegate
    List<String> ret = BrowserHelper.suggest(classToReturn, field, query);

    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "suggest", null, duration, "class", classToReturn.getSimpleName(), "field",
      field, "query", query);

    return ret;
  }

  public static List<IndexedAIP> getAncestors(RodaUser user, IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    List<IndexedAIP> ancestors = BrowserHelper.getAncestors(aip);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAncestors", aip.getId(), duration, AIP_PARAM, aip.toString());

    return ancestors;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static StreamResponse getAipRepresentation(RodaUser user, String representationUUID, String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationParams(acceptFormat);

    IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class, representationUUID);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.getAipRepresentation(representation, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentation", representation.getAipId(), duration,
      RodaConstants.REPRESENTATION_ID, representation.getId());

    return aipRepresentation;
  }

  public static StreamResponse getAipRepresentationPart(RodaUser user, String representationUUID, String part)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    Date startDate = new Date();

    IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class, representationUUID);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.getAipRepresentationPart(representation, part);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentationPart", representation.getAipId(), duration,
      RodaConstants.REPRESENTATION_ID, representation.getId(), "part", part);

    return aipRepresentation;
  }

  public static StreamResponse listAipDescriptiveMetadata(RodaUser user, String aipId, String start, String limit,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateListAipDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescriptiveMetadataList = BrowserHelper.listAipDescriptiveMetadata(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "listAipDescriptiveMetadata", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_QUERY_KEY_START, start,
      RodaConstants.API_QUERY_KEY_LIMIT, limit);

    return aipDescriptiveMetadataList;
  }

  public static StreamResponse getAipDescritiveMetadata(RodaUser user, String aipId, String metadataId,
    String acceptFormat, String language) throws AuthorizationDeniedException, GenericException, TransformerException,
    NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipDescritiveMetadataParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.getAipDescritiveMetadata(aipId, metadataId, acceptFormat,
      language);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipDescritiveMetadata", aipId, duration,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return aipDescritiveMetadata;

  }

  public static StreamResponse getAipDescritiveMetadataVersion(RodaUser user, String aipId, String metadataId,
    String versionId, String acceptFormat, String language) throws AuthorizationDeniedException, GenericException,
    TransformerException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipDescritiveMetadataParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.getAipDescritiveMetadataVersion(aipId, metadataId, versionId,
      acceptFormat, language);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipDescritiveMetadata", aipId, duration,
      RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return aipDescritiveMetadata;

  }

  public static StreamResponse listAipPreservationMetadata(RodaUser user, String aipId, String start, String limit,
    String acceptFormat)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateListAipPreservationMetadataParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipPreservationMetadataList = BrowserHelper.aipsAipIdPreservationMetadataGet(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "listAipPreservationMetadata", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_QUERY_KEY_START, start,
      RodaConstants.API_QUERY_KEY_LIMIT, limit);

    return aipPreservationMetadataList;
  }

  public static StreamResponse getAipRepresentationPreservationMetadata(RodaUser user, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat, String language) throws AuthorizationDeniedException,
    GenericException, TransformerException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationPreservationMetadataParams(acceptFormat, language);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationPreservationMetadata = BrowserHelper.getAipRepresentationPreservationMetadata(aipId,
      representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat, language);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentationPreservationMetadata", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, "startAgent", startAgent, "limitAgent", limitAgent, "startEvent",
      startEvent, "limitEvent", limitEvent, "startFile", startFile, "limitFile", limitFile);

    return aipRepresentationPreservationMetadata;

  }

  public static StreamResponse getAipRepresentationPreservationMetadataFile(RodaUser user, String aipId,
    String representationId, String fileId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationPreservationMetadataFile = BrowserHelper
      .getAipRepresentationPreservationMetadataFile(aipId, representationId, fileId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentationPreservationMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId,
      RodaConstants.API_PATH_PARAM_FILE_UUID, fileId);

    return aipRepresentationPreservationMetadataFile;
  }

  public static void postAipRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, FormDataContentDisposition fileDetail)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException, AlreadyExistsException {

    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    BrowserHelper.createOrUpdateAipRepresentationPreservationMetadataFile(aipId, representationId, fileDirectoryPath,
      fileId, is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "postAipRepresentationPreservationMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

  }

  public static void putAipRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, FormDataContentDisposition fileDetail)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException, AlreadyExistsException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    BrowserHelper.createOrUpdateAipRepresentationPreservationMetadataFile(aipId, representationId, fileDirectoryPath,
      fileId, is, fileDetail, false);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "aipsAipIdPreservationMetadataRepresentationIdFileIdPut", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

  }

  public static void aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(RodaUser user, String aipId,
    String representationId, String fileId, String preservationId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(aipId, representationId, fileId,
      preservationId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "aipsAipIdPreservationMetadataRepresentationIdFileIdDelete", aipId,
      duration, RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID,
      representationId, RodaConstants.API_PATH_PARAM_FILE_UUID, fileId);

  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */

  public static IndexedAIP moveInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, RodaUser user)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    UserUtility.checkObjectPermissions(user, selected, PermissionType.UPDATE);

    if (parentId != null) {
      IndexedAIP parentAip = BrowserHelper.retrieve(IndexedAIP.class, parentId);
      UserUtility.checkObjectPermissions(user, parentAip, PermissionType.CREATE);
    }

    // delegate
    IndexedAIP returnAIP = BrowserHelper.moveInHierarchy(selected, parentId, user);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "moveInHierarchy", parentId, duration, "selected", selected, "toParent",
      parentId);

    return returnAIP;
  }

  public static AIP createAIP(RodaUser user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
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
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "createAIP", aip.getId(), duration, "parentId", parentId);

    return aip;
  }

  public static String removeAIP(RodaUser user, SelectedItems<IndexedAIP> aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    String parentId = BrowserHelper.removeAIP(aips, user);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeAIP", null, duration, "selected", aips);
    return parentId;
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, String metadataVersion, ContentPayload metadataPayload) throws AuthorizationDeniedException,
    GenericException, ValidationException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.createDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataVersion, metadataPayload);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "createDescriptiveMetadataFile", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return ret;
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, String metadataVersion, ContentPayload metadataPayload) throws AuthorizationDeniedException,
    GenericException, ValidationException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Updated by " + user.getName();
    DescriptiveMetadata ret = BrowserHelper.updateDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataVersion, metadataPayload, message);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "editDescriptiveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return ret;
  }

  public static void removeDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.removeDescriptiveMetadataFile(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeMetadataFile", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);
  }

  public static DescriptiveMetadata retrieveDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    DescriptiveMetadata dm = BrowserHelper.retrieveMetadataFile(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return dm;
  }

  public static void removeRepresentation(RodaUser user, String aipId, String representationId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.removeRepresentation(aipId, representationId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRepresentation", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

  }

  public static void removeRepresentationFile(RodaUser user, String fileUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedFile file = BrowserHelper.retrieve(IndexedFile.class, fileUUID);
    // TODO check permissions from indexed file
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // delegate
    BrowserHelper.removeRepresentationFile(fileUUID);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRepresentationFile", aip.getId(), duration, RodaConstants.FILE_AIPID,
      file.getAipId(), RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId(), RodaConstants.FILE_PATH,
      file.getPath(), RodaConstants.FILE_FILEID, file.getId());
  }

  public static StreamResponse getAipRepresentationFile(RodaUser user, String fileUuid, String acceptFormat)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationFileParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);
    // TODO get permissions from indexed file
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, file.getAipId());
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentationFile = BrowserHelper.getAipRepresentationFile(fileUuid, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentationFile", file.getAipId(), duration,
      RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId(), RodaConstants.FILE_PATH, file.getPath(),
      RodaConstants.FILE_FILEID, file.getId());

    return aipRepresentationFile;
  }

  public static void putDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    String metadataVersion, InputStream is, FormDataContentDisposition fileDetail)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Updated by " + user.getName();
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, metadataVersion, message,
      is, fileDetail, false);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "putDescriptiveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

  }

  public static void postDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    String metadataVersion, InputStream is, FormDataContentDisposition fileDetail)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    String message = "Created by " + user.getName();
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, metadataVersion, message,
      is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "postDescriptiveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

  }

  public static TransferredResource createTransferredResourcesFolder(RodaUser user, String parentUUID,
    String folderName, boolean forceCommit)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();
    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      TransferredResource transferredResource = BrowserHelper.createTransferredResourcesFolder(parentUUID, folderName,
        forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM,
        parentUUID, FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, true);
      return transferredResource;
    } catch (GenericException e) {
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM,
        parentUUID, FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void removeTransferredResources(RodaUser user, SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    // delegate
    BrowserHelper.removeTransferredResources(selected, user);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeTransferredResources", null, duration, "selected", selected);
  }

  public static TransferredResource createTransferredResourceFile(RodaUser user, String parentUUID, String fileName,
    InputStream inputStream, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
    AlreadyExistsException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      TransferredResource transferredResource = BrowserHelper.createTransferredResourceFile(parentUUID, fileName,
        inputStream, forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourceFile", null, duration, PATH_PARAM, parentUUID,
        FILENAME_PARAM, fileName, SUCCESS_PARAM, true);

      return transferredResource;
    } catch (GenericException e) {
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourceFile", null, duration, PATH_PARAM, parentUUID,
        FILENAME_PARAM, fileName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }

  }

  public static StreamResponse getClassificationPlan(RodaUser user, String type)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    StreamResponse classificationPlan = BrowserHelper.getClassificationPlan(type, user);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getClassificationPlan", null, duration, CLASSIFICATION_PLAN_TYPE_PARAMETER,
      type);

    return classificationPlan;
  }

  public static TransferredResource createTransferredResource(RodaUser user, String parentUUID, String fileName,
    InputStream inputStream, String name, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
    AlreadyExistsException, RequestNotValidException, NotFoundException {
    TransferredResource transferredResource;
    if (name == null) {
      transferredResource = Browser.createTransferredResourceFile(user, parentUUID, fileName, inputStream, forceCommit);
    } else {
      transferredResource = Browser.createTransferredResourcesFolder(user, parentUUID, name, forceCommit);
    }

    return transferredResource;
  }

  public static boolean getScanUpdateStatus() {
    return BrowserHelper.getScanUpdateStatus();
  }

  public static void updateAllTransferredResources(String subFolderUUID, boolean waitToFinish)
    throws IsStillUpdatingException {
    BrowserHelper.runTransferredResourceScan(subFolderUUID, waitToFinish);
  }

  public static List<SupportedMetadataTypeBundle> getSupportedMetadata(RodaUser user, Locale locale)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // delegate
    List<SupportedMetadataTypeBundle> supportedMetadata = BrowserHelper.getSupportedMetadata(user, locale);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getSupportedMetadata", null, duration, RodaConstants.LOCALE, locale);
    return supportedMetadata;
  }

  public static StreamResponse getTransferredResource(RodaUser user, String resourceId)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    StreamResponse response = BrowserHelper
      .getTransferredResource(BrowserHelper.retrieve(TransferredResource.class, resourceId));

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getTransferredResource", null, duration, "resourceId", resourceId);

    return response;
  }

  public static PreservationEventViewBundle retrievePreservationEventViewBundle(RodaUser user, String eventId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    Date startDate = new Date();

    // TODO maybe update permissions...
    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    // IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    // UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    PreservationEventViewBundle resource = BrowserHelper.retrievePreservationEventViewBundle(eventId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrievePreservationEventViewBundle", null, duration,
      INDEX_PRESERVATION_EVENT_ID, eventId);

    return resource;
  }

  public static void revertDescriptiveMetadataVersion(RodaUser user, String aipId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    // delegate
    // TODO externalize this message
    String message = "Reverted by " + user.getId();
    BrowserHelper.revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId, message);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "revertDescriptiveMetadataVersion", null, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, descriptiveMetadataId,
      RodaConstants.API_QUERY_PARAM_VERSION, versionId);
  }

  public static void removeDescriptiveMetadataVersion(RodaUser user, String aipId, String descriptiveMetadataId,
    String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.DELETE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    BrowserHelper.removeDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeDescriptiveMetadataVersion", null, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, descriptiveMetadataId,
      RodaConstants.API_QUERY_PARAM_VERSION, versionId);
  }

  public static void updateAIPPermissions(RodaUser user, String aipId, Permissions permissions, boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    BrowserHelper.updateAIPPermissions(aip, permissions, recursive);

    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "updateAIPPermissions", null, duration, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId, "permissions", permissions);
  }

  public static <T extends IsIndexed> List<String> consolidate(RodaUser user, Class<T> classToReturn,
    SelectedItems<T> selected) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    return BrowserHelper.consolidate(user, classToReturn, selected);
  }

  public static void modifyRisk(RodaUser user, Risk risk, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.modifyRisk(risk, message, false);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "modifyRisk", null, duration, "risk", risk);
  }

  public static void modifyFormat(RodaUser user, Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.modifyFormat(format, false);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "modifyFormat", null, duration, "format", format);
  }

  public static void modifyAgent(RodaUser user, Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.modifyAgent(agent, false);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "modifyAgent", null, duration, "agent", agent);
  }

  public static Risk addRisk(RodaUser user, Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    Risk ret = BrowserHelper.addRisk(risk, true);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "addRisk", null, duration, "risk", risk);
    return ret;
  }

  public static Format addFormat(RodaUser user, Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    Format ret = BrowserHelper.addFormat(format, false);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "addFormat", null, duration, "format", format);
    return ret;
  }

  public static Agent addAgent(RodaUser user, Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    Agent ret = BrowserHelper.addAgent(agent, false);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "addAgent", null, duration, "agent", agent);
    return ret;
  }

  public static List<Format> retrieveFormats(RodaUser user, String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    List<Format> ret = BrowserHelper.retrieveFormats(agentId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveFormats", null, duration, "agentId", agentId);
    return ret;
  }

  public static List<Agent> retrieveRequiredAgents(RodaUser user, String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    List<Agent> ret = BrowserHelper.retrieveRequiredAgents(agentId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveRequiredAgents", null, duration, "agentId", agentId);
    return ret;
  }

  public static void revertRiskVersion(RodaUser user, String riskId, String versionId, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.revertRiskVersion(riskId, versionId, message);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "revertRiskVersion", versionId, duration, "riskId", riskId, "versionId",
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
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateExportAipParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    UserUtility.checkObjectPermissions(user, selected, PermissionType.READ);

    // delegate
    StreamResponse aipExport = BrowserHelper.getAIPs(selected, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "exportAIP", null, duration);

    return aipExport;
  }

  public static StreamResponse getAIP(RodaUser user, String aipId, String acceptFormat)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP indexedAIP = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, indexedAIP, PermissionType.READ);

    // delegate
    StreamResponse aip = BrowserHelper.getAIP(indexedAIP, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAIP", aipId, duration);

    return aip;
  }

  public static StreamResponse getAIPPart(RodaUser user, String aipId, String part)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP indexedAIP = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, indexedAIP, PermissionType.READ);

    // delegate
    StreamResponse aip = BrowserHelper.getAIPPart(indexedAIP, part);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAIPPart", aipId, duration, "part", part);

    return aip;
  }

  public static void removeRiskVersion(RodaUser user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.removeRiskVersion(riskId, versionId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRiskVersion", versionId, duration, "riskId", riskId, "versionId",
      versionId);
  }

  public static RiskVersionsBundle retrieveRiskVersions(RodaUser user, String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    RiskVersionsBundle ret = BrowserHelper.retrieveRiskVersions(riskId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveRiskVersions", null, duration, "riskId", riskId);
    return ret;
  }

  public static boolean hasRiskVersions(RodaUser user, String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    boolean ret = BrowserHelper.hasRiskVersions(id);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "hasRiskVersions", null, duration, "riskId", id);
    return ret;
  }

  public static Risk retrieveRiskVersion(RodaUser user, String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    Risk ret = BrowserHelper.retrieveRiskVersion(riskId, selectedVersion);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveRiskVersion", null, duration, "riskId", riskId, "selectedVersion",
      selectedVersion);
    return ret;
  }

  public static RiskMitigationBundle retrieveShowMitigationTerms(RodaUser user, int preMitigationProbability,
    int preMitigationImpact, int posMitigationProbability, int posMitigationImpact)
    throws AuthorizationDeniedException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    RiskMitigationBundle ret = BrowserHelper.retrieveShowMitigationTerms(preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveShowMitigationTerms", null, duration, "preMitigationProbability",
      preMitigationProbability, "preMitigationImpact", preMitigationImpact, "posMitigationProbability",
      posMitigationProbability, "posMitigationImpact", posMitigationImpact);
    return ret;
  }

  public static List<String> retrieveMitigationSeverityLimits(RodaUser user) throws AuthorizationDeniedException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    List<String> ret = BrowserHelper.retrieveShowMitigationTerms();

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveMitigationSeverityLimits", null, duration);
    return ret;
  }

  public static MitigationPropertiesBundle retrieveAllMitigationProperties(RodaUser user)
    throws AuthorizationDeniedException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    MitigationPropertiesBundle ret = BrowserHelper.retrieveAllMitigationProperties();

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveAllMitigationProperties", null, duration);
    return ret;
  }

  public static void deleteRisk(RodaUser user, SelectedItems<IndexedRisk> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.deleteRisk(user, selected);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "deleteRisk", null, duration, "selected", selected);
  }

  public static void deleteAgent(RodaUser user, SelectedItems<Agent> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.deleteAgent(user, selected);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "deleteAgent", null, duration, "selected", selected);
  }

  public static void deleteFormat(RodaUser user, SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.deleteFormat(user, selected);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "deleteFormat", null, duration, "selected", selected);
  }

  public static List<String> getRiskOnAIP(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    List<String> riskList = BrowserHelper.getRiskOnAIP(aipId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "getRiskOnAIP", null, duration, "aipId", aipId);
    return riskList;
  }

  public static void deleteRiskIncidences(RodaUser user, String id, SelectedItems<RiskIncidence> incidences)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.deleteRiskIncidences(user, id, incidences);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "deleteRiskIncidences", null, duration, "incidences", incidences);
  }

  public static void updateRiskCounters(RodaUser user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.updateRiskCounters();

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "updateRiskCounters", null, duration);
  }

  public static void appraisal(RodaUser user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    // TODO define appraisal role
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.appraisal(user, selected, accept, rejectReason);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "appraisal", null, duration, "selected", selected, "accept", accept,
      "rejectReason", rejectReason);
  }

  public static String getRepresentationUUID(RodaUser user, String representationId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    // TODO define appraisal role
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    String ret = BrowserHelper.getRepresentationUUID(user, representationId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "getRepresentationUUID", null, duration, "representationId",
      representationId);
    return ret;
  }

  public static Pair<String, String> getRepresentationAndFileUUID(RodaUser user, String representationId, String fileId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    // TODO define appraisal role
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    Pair<String, String> ret = BrowserHelper.getRepresentationAndFileUUID(user, representationId, fileId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "getRepresentationAndFileUUID", null, duration, "fileId", fileId);
    return ret;
  }

}
