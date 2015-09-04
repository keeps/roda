package org.roda.api.v1.impl;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.v1.AipsService;
import org.roda.api.v1.ApiResponseMessage;
import org.roda.api.v1.NotFoundException;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsServiceImpl extends AipsService {
  
      @Override
      public Response aipsGet(String start,String limit)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdGet(String aipId,String acceptFormat)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdPut(String aipId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdPost(String aipId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDelete(String aipId)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataGet(String aipId,String start,String limit)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdGet(String aipId,String representationId,String acceptFormat)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdPut(String aipId,String representationId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdPost(String aipId,String representationId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdDelete(String aipId,String representationId)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdFileIdGet(String aipId,String representationId,String fileId,String acceptFormat)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdFileIdPut(String aipId,String representationId,String fileId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdFileIdPost(String aipId,String representationId,String fileId,String filepath)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDataRepresentationIdFileIdDelete(String aipId,String representationId,String fileId)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDescriptiveMetadataGet(String aipId,String start,String limit)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDescriptiveMetadataMetadataIdGet(String aipId,String metadataId,String acceptFormat)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDescriptiveMetadataMetadataIdPut(String aipId,String metadataId,FormDataContentDisposition fileDetail,String metadataType)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDescriptiveMetadataMetadataIdPost(String aipId,String metadataId,FormDataContentDisposition fileDetail,String metadataType)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
      @Override
      public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(String aipId,String metadataId)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
}
