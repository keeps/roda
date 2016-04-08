/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

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
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.storage.ContentPayload;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
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

  public static <T extends IsIndexed> IndexResult<T> find(RodaUser user, Class<T> classToReturn, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets)
      throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    // TODO check permissions for each class
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexResult<T> ret = BrowserHelper.find(classToReturn, filter, sorter, sublist, facets, user);

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
  public static StreamResponse getAipRepresentation(RodaUser user, String aipId, String representationId,
    String acceptFormat)
      throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationParams(acceptFormat);

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.READ);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.getAipRepresentation(aipId, representationId, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentation", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

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

  public static AIP moveInHierarchy(RodaUser user, String aipId, String parentId) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    if (parentId != null) {
      IndexedAIP parentAip = BrowserHelper.retrieve(IndexedAIP.class, parentId);
      UserUtility.checkObjectPermissions(user, parentAip, PermissionType.CREATE);
    }

    // delegate
    AIP returnAIP = BrowserHelper.moveInHierarchy(aipId, parentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "moveInHierarchy", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, "toParent", parentId);

    return returnAIP;
  }

  public static AIP createAIP(RodaUser user, String parentId) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    if (parentId != null) {
      IndexedAIP parentSDO = BrowserHelper.retrieve(IndexedAIP.class, parentId);
      UserUtility.checkObjectPermissions(user, parentSDO, PermissionType.CREATE);
    } else {
      // TODO check user role to create top-level AIPs
    }

    Permissions permissions = new Permissions();
    permissions.setUserPermissions(user.getId(), new HashSet<PermissionType>(Arrays.asList(PermissionType.values())));

    // delegate
    AIP aip = BrowserHelper.createAIP(parentId, permissions);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "createAIP", aip.getId(), duration, "parentId", parentId);

    return aip;
  }

  public static String removeAIP(RodaUser user, SelectedItems aips)
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

  public static String createTransferredResourcesFolder(RodaUser user, String parentUUID, String folderName,
    boolean forceCommit)
      throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();
    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      String uuid = BrowserHelper.createTransferredResourcesFolder(parentUUID, folderName, forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM,
        parentUUID, FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, true);
      return uuid;
    } catch (GenericException e) {
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM,
        parentUUID, FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void removeTransferredResources(RodaUser user, SelectedItems selected)
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

  public static void createTransferredResourceFile(RodaUser user, String parentUUID, String fileName,
    InputStream inputStream, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
      AlreadyExistsException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      BrowserHelper.createTransferredResourceFile(parentUUID, fileName, inputStream, forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourceFile", null, duration, PATH_PARAM, parentUUID,
        FILENAME_PARAM, fileName, SUCCESS_PARAM, true);
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

  public static void createTransferredResource(RodaUser user, String parentUUID, String fileName,
    InputStream inputStream, String name, boolean forceCommit) throws AuthorizationDeniedException, GenericException,
      AlreadyExistsException, RequestNotValidException, NotFoundException {
    if (name == null) {
      Browser.createTransferredResourceFile(user, parentUUID, fileName, inputStream, forceCommit);
    } else {
      Browser.createTransferredResourcesFolder(user, parentUUID, name, forceCommit);
    }
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

  /**
   * @deprecated this method should be moved to the api
   */
  @Deprecated
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

  public static void updateAIPPermissions(RodaUser user, String aipId, Permissions permissions)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);
    IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId);
    UserUtility.checkObjectPermissions(user, aip, PermissionType.UPDATE);

    BrowserHelper.updateAIPPermissions(aip, permissions);

    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "updateAIPPermissions", null, duration, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId, "permissions", permissions);
  }

  public static <T extends IsIndexed> List<String> consolidate(RodaUser user, Class<T> classToReturn,
    SelectedItems selected) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    return BrowserHelper.consolidate(user, classToReturn, selected);
  }

  public static void removeRisk(RodaUser user, SelectedItems selected) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.removeRisk(selected, user);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRisk", null, duration, "selected", selected);
  }

  public static void removeAgent(RodaUser user, SelectedItems selected) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.removeAgent(selected, user);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeAgent", null, duration, "selected", selected);
  }

  public static void removeFormat(RodaUser user, SelectedItems selected) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);

    // delegate
    BrowserHelper.removeFormat(selected, user);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeFormat", null, duration, "selected", selected);
  }
}
