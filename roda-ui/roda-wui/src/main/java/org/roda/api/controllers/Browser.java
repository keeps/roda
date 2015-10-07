package org.roda.api.controllers;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.v1.utils.StreamResponse;
import org.roda.common.RodaCoreService;
import org.roda.common.UserUtility;
import org.roda.core.common.AuthorizationDeniedException;
import org.roda.core.common.NotFoundException;
import org.roda.core.common.NotImplementedException;
import org.roda.core.common.RODAException;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ValidationException;
import org.roda.storage.Binary;
import org.roda.wui.common.client.GenericException;
import org.roda.wui.dissemination.browse.client.BrowseItemBundle;
import org.roda.wui.dissemination.browse.client.DescriptiveMetadataEditBundle;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Browser extends RodaCoreService {

  private Browser() {
    super();
  }

  public static BrowseItemBundle getItemBundle(RodaUser user, String aipId, Locale locale)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    BrowseItemBundle itemBundle = BrowserHelper.getItemBundle(aipId, locale);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getItemBundle", aipId, duration, "aipId", aipId);

    return itemBundle;
  }

  public static DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(RodaUser user, String aipId,
    String descId) throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    DescriptiveMetadataEditBundle bundle = BrowserHelper.getDescriptiveMetadataEditBundle(aipId, descId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getDescriptiveMetadataEditBundle", aipId, duration, "aipId", aipId, "descId",
      descId);

    return bundle;
  }

  public static IndexResult<SimpleDescriptionObject> findDescriptiveMetadata(RodaUser user, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws RODAException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    IndexResult<SimpleDescriptionObject> descriptiveMetadata = BrowserHelper.findDescriptiveMetadata(filter, sorter,
      sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "findDescriptiveMetadata", null, duration, "filter", filter.toString(), "sorter",
      sorter.toString(), "sublist", sublist.toString());

    return descriptiveMetadata;
  }

  public static Long countDescriptiveMetadata(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    Long count = BrowserHelper.countDescriptiveMetadata(filter);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "countDescriptiveMetadata", null, duration, "filter", filter.toString());

    return count;
  }

  public static SimpleDescriptionObject getSimpleDescriptionObject(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getSimpleDescriptionObject", aipId, duration, "aipId", aipId);

    return sdo;
  }

  public static List<SimpleDescriptionObject> getAncestors(RodaUser user, SimpleDescriptionObject sdo)
    throws AuthorizationDeniedException, GenericException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "browse");

    // delegate
    List<SimpleDescriptionObject> ancestors = BrowserHelper.getAncestors(sdo);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAncestors", sdo.getId(), duration, "sdo", sdo.toString());

    return ancestors;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static StreamResponse getAipRepresentation(RodaUser user, String aipId, String representationId,
    String acceptFormat)
      throws AuthorizationDeniedException, GenericException, NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationParams(acceptFormat);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    StreamResponse aipRepresentation = BrowserHelper.getAipRepresentation(aipId, representationId, acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipRepresentation", aipId, duration, "aip", aipId, "representationId",
      representationId);

    return aipRepresentation;
  }

  public static StreamResponse listAipDescriptiveMetadata(RodaUser user, String aipId, String start, String limit,
    String acceptFormat)
      throws AuthorizationDeniedException, GenericException, NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateListAipDescriptiveMetadataParams(acceptFormat);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    StreamResponse aipDescriptiveMetadataList = BrowserHelper.listAipDescriptiveMetadata(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "listAipDescriptiveMetadata", aipId, duration, "aip", aipId, "start", start,
      "limit", limit);

    return aipDescriptiveMetadataList;
  }

  public static StreamResponse getAipDescritiveMetadata(RodaUser user, String aipId, String metadataId,
    String acceptFormat, String language) throws AuthorizationDeniedException, GenericException, TransformerException,
      NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipDescritiveMetadataParams(acceptFormat);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    StreamResponse aipDescritiveMetadata = BrowserHelper.getAipDescritiveMetadata(aipId, metadataId, acceptFormat,
      language);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipDescritiveMetadata", aipId, duration, "metadataId", metadataId);

    return aipDescritiveMetadata;

  }

  public static StreamResponse listAipPreservationMetadata(RodaUser user, String aipId, String start, String limit,
    String acceptFormat)
      throws AuthorizationDeniedException, GenericException, NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateListAipPreservationMetadataParams(acceptFormat);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    StreamResponse aipPreservationMetadataList = BrowserHelper.aipsAipIdPreservationMetadataGet(aipId, start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "listAipPreservationMetadata", aipId, duration, "aip", aipId, "start", start,
      "limit", limit);

    return aipPreservationMetadataList;
  }

  public static StreamResponse getAipRepresentationPreservationMetadata(RodaUser user, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat, String language) throws AuthorizationDeniedException,
      GenericException, TransformerException, NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationPreservationMetadataParams(acceptFormat, language);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    StreamResponse aipRepresentationPreservationMetadata = BrowserHelper.getAipRepresentationPreservationMetadata(aipId,
      representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat, language);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipRepresentationPreservationMetadata", aipId, duration, "aip", aipId,
      "startAgent", startAgent, "limitAgent", limitAgent, "startEvent", startEvent, "limitEvent", limitEvent,
      "startFile", startFile, "limitFile", limitFile);

    return aipRepresentationPreservationMetadata;

  }

  public static StreamResponse getAipRepresentationPreservationMetadataFile(RodaUser user, String aipId,
    String representationId, String fileId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectReadPermissions(user, sdo);

    // delegate
    StreamResponse aipRepresentationPreservationMetadataFile = BrowserHelper
      .getAipRepresentationPreservationMetadataFile(aipId, representationId, fileId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipRepresentationPreservationMetadataFile", aipId, duration, "aip", aipId,
      "representationId", representationId, "fileId", fileId);

    return aipRepresentationPreservationMetadataFile;
  }

  public static void postAipRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail)
      throws AuthorizationDeniedException, GenericException, NotFoundException {

    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectInsertPermissions(user, sdo);

    // delegate
    BrowserHelper.createOrUpdateAipRepresentationPreservationMetadataFile(aipId, representationId, is, fileDetail,
      true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "postAipRepresentationPreservationMetadataFile", aipId, duration, "aip", aipId,
      "representationId", representationId);

  }

  public static void putAipRepresentationPreservationMetadataFile(RodaUser user, String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail)
      throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectInsertPermissions(user, sdo);

    // delegate
    BrowserHelper.createOrUpdateAipRepresentationPreservationMetadataFile(aipId, representationId, is, fileDetail,
      false);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "aipsAipIdPreservationMetadataRepresentationIdFileIdPut", aipId, duration, "aip",
      aipId, "representationId", representationId);

  }

  public static void aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(RodaUser user, String aipId,
    String representationId, String fileId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectRemovePermissions(user, sdo);

    // delegate
    BrowserHelper.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(aipId, representationId, fileId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "aipsAipIdPreservationMetadataRepresentationIdFileIdDelete", aipId, duration, "aip",
      aipId, "representationId", representationId, "fileId", fileId);

  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */

  public static SimpleDescriptionObject moveInHierarchy(RodaUser user, String aipId, String parentId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);
    sdo = BrowserHelper.getSimpleDescriptionObject(parentId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    sdo = BrowserHelper.moveInHierarchy(aipId, parentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "moveInHierarchy", sdo.getId(), duration, "aip", aipId, "toParent", parentId);

    return sdo;

  }

  public static AIP createAIP(RodaUser user, String parentId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    if (parentId != null) {
      SimpleDescriptionObject parentSDO = BrowserHelper.getSimpleDescriptionObject(parentId);
      UserUtility.checkObjectModifyPermissions(user, parentSDO);
    } else {
      // TODO check user role to create top-level AIPs
    }

    // delegate
    AIP aip = BrowserHelper.createAIP(parentId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "createAIP", aip.getId(), duration, "parentId", parentId);

    return aip;
  }

  public static void removeAIP(RodaUser user, String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    BrowserHelper.removeAIP(aipId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "removeAIP", aipId, duration);
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(RodaUser user, String aipId,
    String descriptiveMetadataId, String descriptiveMetadataType, Binary descriptiveMetadataIdBinary)
      throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.createDescriptiveMetadataFile(aipId, descriptiveMetadataId,
      descriptiveMetadataType, descriptiveMetadataIdBinary);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "createDescriptiveMetadataFile", sdo.getId(), duration, "aipId", aipId,
      "descriptiveMetadataId", descriptiveMetadataId);

    return ret;
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(RodaUser user, String aipId,
    String descriptiveMetadataId, String descriptiveMetadataType, Binary descriptiveMetadataIdBinary)
      throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    DescriptiveMetadata ret = BrowserHelper.updateDescriptiveMetadataFile(aipId, descriptiveMetadataId,
      descriptiveMetadataType, descriptiveMetadataIdBinary);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "editDescriptiveMetadataFile", aipId, duration, "aipId", aipId,
      "descriptiveMetadataId", descriptiveMetadataId);

    return ret;
  }

  public static void removeDescriptiveMetadataFile(RodaUser user, String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    BrowserHelper.removeDescriptiveMetadataFile(itemId, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "removeMetadataFile", sdo.getId(), duration, "itemId", itemId,
      "descriptiveMetadataId", descriptiveMetadataId);
  }

  public static DescriptiveMetadata retrieveMetadataFile(RodaUser user, String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    DescriptiveMetadata dm = BrowserHelper.retrieveMetadataFile(itemId, descriptiveMetadataId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "retrieveMetadataFile", itemId, duration, "itemId", itemId, "descriptiveMetadataId",
      descriptiveMetadataId);

    return dm;
  }

  public static void removeRepresentation(RodaUser user, String itemId, String representationId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    BrowserHelper.removeRepresentation(itemId, representationId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "removeRepresentation", sdo.getId(), duration, "itemId", itemId, "representationId",
      representationId);

  }

  public static void removeRepresentationFile(RodaUser user, String itemId, String representationId, String fileId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Date start = new Date();

    // check user permissions
    UserUtility.checkRoles(user, "administration.metadata_editor");
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(itemId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    BrowserHelper.removeRepresentationFile(itemId, representationId, fileId);

    // register action
    long duration = new Date().getTime() - start.getTime();
    registerAction(user, "Browser", "removeRepresentationFile", sdo.getId(), duration, "itemId", itemId,
      "representationId", representationId, "fileId", fileId);
  }

  public static StreamResponse getAipRepresentationFile(RodaUser user, String aipId, String representationId,
    String fileId, String acceptFormat)
      throws GenericException, AuthorizationDeniedException, NotFoundException, NotImplementedException {
    Date startDate = new Date();

    // validate input
    BrowserHelper.validateGetAipRepresentationFileParams(acceptFormat);

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectModifyPermissions(user, sdo);

    // delegate
    StreamResponse aipRepresentationFile = BrowserHelper.getAipRepresentationFile(aipId, representationId, fileId,
      acceptFormat);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "getAipRepresentationFile", aipId, duration, "representationId", representationId,
      "fileId", fileId);

    return aipRepresentationFile;
  }

  public static void putDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    InputStream is, FormDataContentDisposition fileDetail)
      throws GenericException, AuthorizationDeniedException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectInsertPermissions(user, sdo);

    // delegate
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "putDescriptiveMetadataFile", aipId, duration, "aip", aipId, "metadataId",
      metadataId);

  }

  public static void postDescriptiveMetadataFile(RodaUser user, String aipId, String metadataId, String metadataType,
    InputStream is, FormDataContentDisposition fileDetail)
      throws GenericException, AuthorizationDeniedException, NotFoundException {
    Date startDate = new Date();

    // check user permissions
    SimpleDescriptionObject sdo = BrowserHelper.getSimpleDescriptionObject(aipId);
    UserUtility.checkObjectInsertPermissions(user, sdo);

    // delegate
    BrowserHelper.createOrUpdateAipDescriptiveMetadataFile(aipId, metadataId, metadataType, is, fileDetail, true);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, "Browser", "postDescriptiveMetadataFile", aipId, duration, "aip", aipId, "metadataId",
      metadataId);

  }

}
