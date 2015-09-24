package org.roda.api.v1.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.controllers.Browser;
import org.roda.api.v1.utils.ApiResponseMessage;
import org.roda.api.v1.utils.NotFoundException;
import org.roda.api.v1.utils.StreamResponse;
import org.roda.common.UserUtility;
import org.roda.model.ModelServiceException;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsResourceImpl {
  private static final Logger LOGGER = Logger.getLogger(AipsResourceImpl.class);

  public Response aipsGet(HttpServletRequest request, String start, String limit) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic2!")).build();
  }

  public Response aipsAipIdGet(HttpServletRequest request, String aipId, String acceptFormat) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPut(HttpServletRequest request, String aipId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPost(HttpServletRequest request, String aipId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDelete(HttpServletRequest request, String aipId) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeAIP(user, aipId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdDataGet(HttpServletRequest request, String aipId, String start, String limit)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response getAipRepresentation(HttpServletRequest request, String aipId, String representationId,
    String acceptFormat) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      if (acceptFormat != null && acceptFormat.equalsIgnoreCase("bin")) {
        // get user
        RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
        // delegate action to controller
        Pair<String, StreamingOutput> aipRepresentation = Browser.getAipRepresentation(user, aipId, representationId);
        return Response.ok(aipRepresentation.getSecond(), MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + aipRepresentation.getFirst()).build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdDataRepresentationIdPut(HttpServletRequest request, String aipId, String representationId,
    String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdPost(HttpServletRequest request, String aipId, String representationId,
    String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdDelete(HttpServletRequest request, String aipId, String representationId)
    throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeRepresentation(user, aipId, representationId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdDataRepresentationIdFileIdGet(HttpServletRequest request, String aipId,
    String representationId, String fileId, String acceptFormat) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentationFile = Browser.getAipRepresentationFile(user, aipId, representationId, fileId,
        acceptFormat);
      return Response.ok(aipRepresentationFile.getStream(), aipRepresentationFile.getMediaType())
        .header("content-disposition", "attachment; filename = " + aipRepresentationFile.getFilename()).build();
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdDataRepresentationIdFileIdPut(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdPost(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeRepresentationFile(user, aipId, representationId, fileId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response listAipDescriptiveMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      if (acceptFormat != null && acceptFormat.equalsIgnoreCase("bin")) {
        // get user
        RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
        // delegate action to controller
        Pair<String, StreamingOutput> aipRepresentation = Browser.listAipDescriptiveMetadata(user, aipId, start, limit);
        return Response.ok(aipRepresentation.getSecond(), MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + aipRepresentation.getFirst()).build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }

  }

  public Response getAipDescriptiveMetadata(HttpServletRequest request, String aipId, String metadataId,
    String acceptFormat, String language) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      if (acceptFormat != null) { // && acceptFormat.equalsIgnoreCase("bin")) {
        // get user
        RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
        // delegate action to controller
        StreamResponse aipDescriptiveMetadata = Browser.getAipDescritiveMetadata(user, aipId, metadataId, acceptFormat,
          language);
        return Response.ok(aipDescriptiveMetadata.getStream(), aipDescriptiveMetadata.getMediaType())
          .header("content-disposition", "attachment; filename = " + aipDescriptiveMetadata.getFilename()).build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(HttpServletRequest request, String aipId, String metadataId,
    FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(HttpServletRequest request, String aipId,
    String metadataId, FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(HttpServletRequest request, String aipId,
    String metadataId) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeDescriptiveMetadataFile(user, aipId, metadataId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response listAipPreservationMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Pair<String, StreamingOutput> aipPreservationMetadataList = Browser.listAipPreservationMetadata(user, aipId,
        start, limit);
      return Response.ok(aipPreservationMetadataList.getSecond(), MediaType.APPLICATION_OCTET_STREAM)
        .header("content-disposition", "attachment; filename = " + aipPreservationMetadataList.getFirst()).build();
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response getAipRepresentationPreservationMetadata(HttpServletRequest request, String aipId,
    String representationId, String start, String limit, String acceptFormat) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      if (acceptFormat != null && acceptFormat.equalsIgnoreCase("bin")) {
        // get user
        RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
        // delegate action to controller
        Pair<String, StreamingOutput> aipRepresentationPreservationMetadata = Browser
          .getAipRepresentationPreservationMetadata(user, aipId, representationId, start, limit);
        return Response.ok(aipRepresentationPreservationMetadata.getSecond(), MediaType.APPLICATION_OCTET_STREAM)
          .header("content-disposition", "attachment; filename = " + aipRepresentationPreservationMetadata.getFirst())
          .build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response getAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {

      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Pair<String, StreamingOutput> aipRepresentationPreservationMetadataFile = Browser
        .getAipRepresentationPreservationMetadataFile(user, aipId, representationId, fileId);

      return Response.ok(aipRepresentationPreservationMetadataFile.getSecond(), MediaType.APPLICATION_OCTET_STREAM)
        .header("content-disposition", "attachment; filename = " + aipRepresentationPreservationMetadataFile.getFirst())
        .build();
    } catch (StorageServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response postAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.postAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response putAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.putAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) throws NotFoundException {
    String authorization = request.getHeader("Authorization");
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(user, aipId, representationId, fileId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

}
