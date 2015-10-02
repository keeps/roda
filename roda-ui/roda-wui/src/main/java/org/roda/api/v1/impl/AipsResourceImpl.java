package org.roda.api.v1.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.controllers.Browser;
import org.roda.api.v1.utils.ApiResponseMessage;
import org.roda.api.v1.utils.StreamResponse;
import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.NotFoundException;
import pt.gov.dgarq.roda.core.common.NotImplementedException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsResourceImpl {
  private static final Logger LOGGER = Logger.getLogger(AipsResourceImpl.class);

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

  public Response aipsAipIdDelete(HttpServletRequest request, String aipId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeAIP(user, aipId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDataGet(HttpServletRequest request, String aipId, String start, String limit) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response getAipRepresentation(HttpServletRequest request, String aipId, String representationId,
    String acceptFormat) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentation = Browser.getAipRepresentation(user, aipId, representationId, acceptFormat);

      return Response.ok(aipRepresentation.getStream(), aipRepresentation.getMediaType())
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + aipRepresentation.getFilename()).build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDataRepresentationIdPut(HttpServletRequest request, String aipId, String representationId,
    String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdPost(HttpServletRequest request, String aipId, String representationId,
    String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdDelete(HttpServletRequest request, String aipId,
    String representationId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeRepresentation(user, aipId, representationId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDataRepresentationIdFileIdGet(HttpServletRequest request, String aipId,
    String representationId, String fileId, String acceptFormat) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentationFile = Browser.getAipRepresentationFile(user, aipId, representationId, fileId,
        acceptFormat);

      return Response.ok(aipRepresentationFile.getStream(), aipRepresentationFile.getMediaType())
        .header("content-disposition", "attachment; filename = " + aipRepresentationFile.getFilename()).build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDataRepresentationIdFileIdPut(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdPost(HttpServletRequest request, String aipId,
    String representationId, String fileId, String filepath) {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeRepresentationFile(user, aipId, representationId, fileId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response listAipDescriptiveMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentation = Browser.listAipDescriptiveMetadata(user, aipId, start, limit, acceptFormat);

      return Response.ok(aipRepresentation.getStream(), aipRepresentation.getMediaType())
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + aipRepresentation.getFilename()).build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }

  }

  public Response getAipDescriptiveMetadata(HttpServletRequest request, String aipId, String metadataId,
    String acceptFormat, String language) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipDescriptiveMetadata = Browser.getAipDescritiveMetadata(user, aipId, metadataId, acceptFormat,
        language);

      return Response.ok(aipDescriptiveMetadata.getStream(), aipDescriptiveMetadata.getMediaType())
        .header("content-disposition", "attachment; filename = " + aipDescriptiveMetadata.getFilename()).build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (TransformerException e) {
      String message;

      if (e.getCause() != null) {
        message = e.getCause().getMessage();
      } else {
        message = e.getMessage();
      }

      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message)).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(HttpServletRequest request, String aipId, String metadataId,
    InputStream is, FormDataContentDisposition fileDetail, String metadataType) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.putDescriptiveMetadataFile(user, aipId, metadataId, metadataType, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(HttpServletRequest request, String aipId,
    String metadataId, InputStream is, FormDataContentDisposition fileDetail, String metadataType) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.postDescriptiveMetadataFile(user, aipId, metadataId, metadataType, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(HttpServletRequest request, String aipId,
    String metadataId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.removeDescriptiveMetadataFile(user, aipId, metadataId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response listAipPreservationMetadata(HttpServletRequest request, String aipId, String start, String limit,
    String acceptFormat) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipPreservationMetadataList = Browser.listAipPreservationMetadata(user, aipId, start, limit,
        acceptFormat);

      return Response.ok(aipPreservationMetadataList.getStream(), aipPreservationMetadataList.getMediaType())
        .header("content-disposition", "attachment; filename = " + aipPreservationMetadataList.getFilename()).build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response getAipRepresentationPreservationMetadata(HttpServletRequest request, String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat, String language) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentationPreservationMetadata = Browser.getAipRepresentationPreservationMetadata(user,
        aipId, representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat,
        language);

      return Response
        .ok(aipRepresentationPreservationMetadata.getStream(), aipRepresentationPreservationMetadata.getMediaType())
        .header("content-disposition", "attachment; filename = " + aipRepresentationPreservationMetadata.getFilename())
        .build();

    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotImplementedException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (TransformerException e) {
      String message;

      if (e.getCause() != null) {
        message = e.getCause().getMessage();
      } else {
        message = e.getMessage();
      }

      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message)).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response getAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, String fileId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      StreamResponse aipRepresentationPreservationMetadataFile = Browser
        .getAipRepresentationPreservationMetadataFile(user, aipId, representationId, fileId);

      return Response
        .ok(aipRepresentationPreservationMetadataFile.getStream(),
          aipRepresentationPreservationMetadataFile.getMediaType())
        .header("content-disposition",
          "attachment; filename = " + aipRepresentationPreservationMetadataFile.getFilename())
        .build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response postAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.postAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response putAipRepresentationPreservationMetadataFile(HttpServletRequest request, String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.putAipRepresentationPreservationMetadataFile(user, aipId, representationId, is, fileDetail);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(HttpServletRequest request, String aipId,
    String representationId, String fileId) {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // delegate action to controller
      Browser.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(user, aipId, representationId, fileId);

      // FIXME give a better answer
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (AuthorizationDeniedException e) {
      return Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (GenericException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (NotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
  }

}
