package org.roda.api.v1.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.controllers.Browser;
import org.roda.api.v1.utils.ApiResponseMessage;
import org.roda.api.v1.utils.ApiUtils;
import org.roda.api.v1.utils.StreamResponse;
import org.roda.common.RodaCoreFactory;
import org.roda.common.UserUtility;
import org.roda.core.common.RODAException;
import org.roda.core.data.v2.RodaUser;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsResourceImpl {

  public Response aipsGet(HttpServletRequest request, String start, String limit) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdGet(HttpServletRequest request, String aipId, String acceptFormat) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPut(HttpServletRequest request, String aipId, String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPost(HttpServletRequest request, String aipId, String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDelete(HttpServletRequest request, String aipId) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.removeAIP(user, aipId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataGet(HttpServletRequest request, String aipId, String start, String limit)
    throws RODAException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response getAipRepresentation(HttpServletRequest request, String aipId, String representationId,
    String acceptFormat) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.getAipRepresentation(user, aipId, representationId, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  public Response aipsAipIdDataRepresentationIdPut(HttpServletRequest request, String aipId, String representationId,
    String filepath) throws RODAException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdPost(HttpServletRequest request, String aipId, String representationId,
    String filepath) throws RODAException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdDelete(HttpServletRequest request, String aipId, String representationId)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.removeRepresentation(user, aipId, representationId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdGet(HttpServletRequest request, String aipId,
    String representationId, String fileId, String acceptFormat) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentationFile = Browser.getAipRepresentationFile(user, aipId, representationId, fileId,
      acceptFormat);

    return ApiUtils.okResponse(aipRepresentationFile);
  }

  public Response aipsAipIdDataRepresentationIdFileIdPut(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) throws RODAException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdPost(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) throws RODAException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.removeRepresentationFile(user, aipId, representationId, fileId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response listAipDescriptiveMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.listAipDescriptiveMetadata(user, aipId, start, limit, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  public Response getAipDescriptiveMetadata(HttpServletRequest request, String aipId, String metadataId,
    String acceptFormat, String language) throws RODAException {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipDescriptiveMetadata = Browser.getAipDescritiveMetadata(user, aipId, metadataId, acceptFormat,
        language);

      return ApiUtils.okResponse(aipDescriptiveMetadata);

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(HttpServletRequest request, String aipId, String metadataId,
    InputStream is, FormDataContentDisposition fileDetail, String metadataType) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.putDescriptiveMetadataFile(user, aipId, metadataId, metadataType, is, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(HttpServletRequest request, String aipId,
    String metadataId, InputStream is, FormDataContentDisposition fileDetail, String metadataType)
      throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.postDescriptiveMetadataFile(user, aipId, metadataId, metadataType, is, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(HttpServletRequest request, String aipId,
    String metadataId) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.removeDescriptiveMetadataFile(user, aipId, metadataId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response listAipPreservationMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipPreservationMetadataList = Browser.listAipPreservationMetadata(user, aipId, start, limit,
      acceptFormat);

    return ApiUtils.okResponse(aipPreservationMetadataList);
  }

  public Response getAipRepresentationPreservationMetadata(HttpServletRequest request, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat, String language) throws RODAException {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentationPreservationMetadata = Browser.getAipRepresentationPreservationMetadata(user,
        aipId, representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat,
        language);

      return ApiUtils.okResponse(aipRepresentationPreservationMetadata);

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  public Response getAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentationPreservationMetadataFile = Browser
      .getAipRepresentationPreservationMetadataFile(user, aipId, representationId, fileId);

    return ApiUtils.okResponse(aipRepresentationPreservationMetadataFile);
  }

  public Response postAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.postAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response putAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.putAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(user, aipId, representationId, fileId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

}
