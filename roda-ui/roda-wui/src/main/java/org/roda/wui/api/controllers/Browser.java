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
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.storage.ContentPayload;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
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

    // delegate
    DescriptiveMetadataEditBundle bundle = BrowserHelper.getDescriptiveMetadataEditBundle(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getDescriptiveMetadataEditBundle", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return bundle;
  }

  public static IndexResult<IndexedAIP> findDescriptiveMetadata(RodaUser user, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexResult<IndexedAIP> descriptiveMetadata = BrowserHelper.findDescriptiveMetadata(filter, sorter, sublist,
      facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "findDescriptiveMetadata", null, duration,
      RodaConstants.CONTROLLER_FILTER_PARAM, filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter,
      RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return descriptiveMetadata;
  }

  public static Long countDescriptiveMetadata(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    Long count = BrowserHelper.countDescriptiveMetadata(filter);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "countDescriptiveMetadata", null, duration,
      RodaConstants.CONTROLLER_FILTER_PARAM, filter.toString());

    return count;
  }

  public static IndexedAIP getIndexedAip(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getIndexedAip", aipId, duration, RodaConstants.API_PATH_PARAM_AIP_ID,
      aipId);

    return aip;
  }

  public static List<IndexedAIP> getAncestors(RodaUser user, IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectReadPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectReadPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.getAipDescritiveMetadata(aipId, metadataId, acceptFormat,
      language);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectReadPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectReadPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectReadPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectInsertPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectInsertPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectRemovePermissions(user, aip);

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

  public static IndexedAIP moveInHierarchy(RodaUser user, String aipId, String parentId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);
    aip = BrowserHelper.getIndexedAIP(parentId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    aip = BrowserHelper.moveInHierarchy(aipId, parentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "moveInHierarchy", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, "toParent", parentId);

    return aip;

  }

  public static AIP createAIP(RodaUser user, String parentId) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    if (parentId != null) {
      IndexedAIP parentSDO = BrowserHelper.getIndexedAIP(parentId);
      UserUtility.checkObjectModifyPermissions(user, parentSDO);
    } else {
      // TODO check user role to create top-level AIPs
    }

    // delegate
    AIP aip = BrowserHelper.createAIP(parentId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "createAIP", aip.getId(), duration, "parentId", parentId);

    return aip;
  }

  public static String removeAIP(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    String parentId = BrowserHelper.removeAIP(aipId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeAIP", aipId, duration);
    return parentId;
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, ContentPayload metadataPayload) throws AuthorizationDeniedException, GenericException,
      ValidationException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.createDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataPayload);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "createDescriptiveMetadataFile", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

    return ret;
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId,
    String metadataType, ContentPayload metadataPayload) throws AuthorizationDeniedException, GenericException,
      ValidationException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.updateDescriptiveMetadataFile(aipId, metadataId, metadataType,
      metadataPayload);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    BrowserHelper.removeDescriptiveMetadataFile(aipId, metadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeMetadataFile", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);
  }

  public static DescriptiveMetadata retrieveMetadataFile(RodaUser user, String aipId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

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
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    BrowserHelper.removeRepresentation(aipId, representationId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRepresentation", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId);

  }

  public static void removeRepresentationFile(RodaUser user, String aipId, String representationId,
    List<String> directoryPath, String fileId)
      throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, ADMINISTRATION_METADATA_EDITOR_ROLE);
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    BrowserHelper.removeRepresentationFile(aipId, representationId, directoryPath, fileId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeRepresentationFile", aip.getId(), duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId,
      RodaConstants.API_PATH_PARAM_FILE_UUID, fileId);
  }

  public static StreamResponse getAipRepresentationFile(RodaUser user, String aipId, String representationId,
    String fileUuid, String acceptFormat)
      throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationFileParams(acceptFormat);

    // check user permissions
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectModifyPermissions(user, aip);

    // delegate
    StreamResponse aipRepresentationFile = BrowserHelper.getAipRepresentationFile(aipId, representationId, fileUuid,
      acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getAipRepresentationFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_REPRESENTATION_ID, representationId, RodaConstants.API_PATH_PARAM_FILE_UUID,
      fileUuid);

    return aipRepresentationFile;
  }

  public static void putDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    InputStream is, FormDataContentDisposition fileDetail) throws GenericException, AuthorizationDeniedException,
      NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectInsertPermissions(user, aip);

    // delegate
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "putDescriptiveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

  }

  public static void postDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    InputStream is, FormDataContentDisposition fileDetail) throws GenericException, AuthorizationDeniedException,
      NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
    Date startDate = new Date();

    // check user permissions
    IndexedAIP aip = BrowserHelper.getIndexedAIP(aipId);
    UserUtility.checkObjectInsertPermissions(user, aip);

    // delegate
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "postDescriptiveMetadataFile", aipId, duration,
      RodaConstants.API_PATH_PARAM_AIP_ID, aipId, RodaConstants.API_PATH_PARAM_METADATA_ID, metadataId);

  }

  public static IndexResult<TransferredResource> findTransferredResources(RodaUser user, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    IndexResult<TransferredResource> resources = BrowserHelper.findTransferredResources(filter, sorter, sublist,
      facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "findTransferredResources", null, duration,
      RodaConstants.CONTROLLER_FILTER_PARAM, filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter,
      RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return resources;
  }

  public static TransferredResource retrieveTransferredResource(RodaUser user, String transferredResourceId)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    Date startDate = new Date();
    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    TransferredResource resource = BrowserHelper.retrieveTransferredResource(transferredResourceId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveTransferredResource", null, duration,
      TRANSFERRED_RESOURCE_ID_PARAM, transferredResourceId);

    return resource;
  }

  public static String createTransferredResourcesFolder(RodaUser user, String parent, String folderName,
    boolean forceCommit) throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();
    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parent));

    // delegate
    try {
      String id = BrowserHelper.createTransferredResourcesFolder(parent, folderName, forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM, parent,
        FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, true);
      return id;
    } catch (GenericException e) {
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourcesFolder", null, duration, PARENT_PARAM, parent,
        FOLDERNAME_PARAM, folderName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void removeTransferredResources(RodaUser user, List<String> ids, boolean forceCommit)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, ids);

    // delegate
    BrowserHelper.removeTransferredResources(ids, forceCommit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "removeTransferredResources", null, duration, PATH_PARAM, ids);
  }

  public static void createTransferredResourceFile(RodaUser user, String path, String fileName, InputStream inputStream,
    boolean forceCommit) throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(path));

    // delegate
    try {
      BrowserHelper.createTransferredResourceFile(path, fileName, inputStream, forceCommit);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourceFile", null, duration, PATH_PARAM, path,
        FILENAME_PARAM, fileName, SUCCESS_PARAM, true);
    } catch (GenericException e) {
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, BROWSER_COMPONENT, "createTransferredResourceFile", null, duration, PATH_PARAM, path,
        FILENAME_PARAM, fileName, SUCCESS_PARAM, false, ERROR_PARAM, e.getMessage());
      throw e;
    }

  }

  public static StreamResponse getClassificationPlan(RodaUser user, String type)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // delegate
    StreamResponse classificationPlan = BrowserHelper.getClassificationPlan(type, user);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getClassificationPlan", null, duration, CLASSIFICATION_PLAN_TYPE_PARAMETER,
      type);

    return classificationPlan;
  }

  public static void createTransferredResource(RodaUser user, String parentId, String fileName, InputStream inputStream,
    String name, boolean forceCommit) throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    if (name == null) {
      Browser.createTransferredResourceFile(user, parentId, fileName, inputStream, forceCommit);
    } else {
      Browser.createTransferredResourcesFolder(user, parentId, name, forceCommit);
    }
  }

  public static boolean isTransferFullyInitialized(RodaUser user) {
    return BrowserHelper.isTransferFullyInitialized();
  }

  public static IndexResult<IndexedFile> findFiles(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException, RequestNotValidException {
    Date startDate = new Date();

    // TODO
    // check user permissions
    // UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexResult<IndexedFile> files = BrowserHelper.findFiles(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "findFiles", null, duration, RodaConstants.CONTROLLER_FILTER_PARAM, filter,
      RodaConstants.CONTROLLER_SORTER_PARAM, sorter, RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return files;
  }

  public static IndexedFile retrieveFile(RodaUser user, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId)
      throws GenericException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // TODO
    // check user permissions
    // UserUtility.checkRoles(user, BROWSE_ROLE);

    // delegate
    IndexedFile file = BrowserHelper.retrieveFile(aipId, representationId, fileDirectoryPath, fileId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveFile", null, duration, RodaConstants.FILE_AIPID, aipId,
      RodaConstants.FILE_REPRESENTATIONID, representationId, RodaConstants.FILE_PATH, fileDirectoryPath,
      RodaConstants.FILE_FILEID, fileId);

    return file;
  }

  public static List<SupportedMetadataTypeBundle> getSupportedMetadata(RodaUser user, Locale locale)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // delegate
    List<SupportedMetadataTypeBundle> supportedMetadata = BrowserHelper.getSupportedMetadata(locale);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getSupportedMetadata", null, duration, RodaConstants.LOCALE, locale);
    return supportedMetadata;
  }

  public static IndexResult<IndexedPreservationEvent> findIndexedPreservationEvents(RodaUser user, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets)
      throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Date startDate = new Date();

    // TODO maybe update permissions...
    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    IndexResult<IndexedPreservationEvent> resources = BrowserHelper.findIndexedPreservationEvents(filter, sorter,
      sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "findIndexedPreservationEvents", null, duration,
      RodaConstants.CONTROLLER_FILTER_PARAM, filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter,
      RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return resources;
  }

  public static IndexedPreservationEvent retrieveIndexedPreservationEvent(RodaUser user,
    String indexedPreservationEventId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // TODO maybe update permissions...
    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    IndexedPreservationEvent resource = BrowserHelper.retrieveIndexedPreservationEvent(indexedPreservationEventId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveIndexedPreservationEvent", null, duration,
      INDEX_PRESERVATION_EVENT_ID, indexedPreservationEventId);

    return resource;
  }

  public static StreamResponse getTransferredResource(RodaUser user, String resourceId)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, INGEST_TRANSFER_ROLE);

    StreamResponse response = BrowserHelper
      .getTransferredResource(BrowserHelper.retrieveTransferredResource(resourceId));

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "getTransferredResource", null, duration, "resourceId", resourceId);

    return response;
  }

  public static IndexedPreservationAgent retrieveIndexedPreservationAgent(RodaUser user,
    String indexedPreservationAgentId) throws NotFoundException, GenericException, AuthorizationDeniedException {
    Date startDate = new Date();

    // TODO maybe update permissions...
    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

    // TODO if not admin, add to filter a constraint for the resource to belong
    // to this user

    // delegate
    IndexedPreservationAgent resource = BrowserHelper.retrieveIndexedPreservationAgent(indexedPreservationAgentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, BROWSER_COMPONENT, "retrieveIndexedPreservationAgent", null, duration,
      INDEX_PRESERVATION_AGENT_ID, indexedPreservationAgentId);

    return resource;
  }

  public static PreservationEventViewBundle retrievePreservationEventViewBundle(RodaUser user, String eventId) throws AuthorizationDeniedException, NotFoundException, GenericException {
    Date startDate = new Date();

    // TODO maybe update permissions...
    // check user permissions
    UserUtility.checkRoles(user, BROWSE_ROLE);

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
}
