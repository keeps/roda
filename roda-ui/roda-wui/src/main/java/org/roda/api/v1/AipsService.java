package org.roda.api.v1;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public abstract class AipsService {

  public abstract Response aipsGet(String start, String limit) throws NotFoundException;

  public abstract Response aipsAipIdGet(String aipId, String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdPut(String aipId, String filepath) throws NotFoundException;

  public abstract Response aipsAipIdPost(String aipId, String filepath) throws NotFoundException;

  public abstract Response aipsAipIdDelete(String aipId) throws NotFoundException;

  public abstract Response aipsAipIdDataGet(String aipId, String start, String limit) throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdGet(HttpServletRequest request, String aipId, String representationId, String acceptFormat)
    throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdPut(String aipId, String representationId, String filepath)
    throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdPost(String aipId, String representationId, String filepath)
    throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdDelete(String aipId, String representationId)
    throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdFileIdGet(String aipId, String representationId, String fileId,
    String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdFileIdPut(String aipId, String representationId, String fileId,
    String filepath) throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdFileIdPost(String aipId, String representationId, String fileId,
    String filepath) throws NotFoundException;

  public abstract Response aipsAipIdDataRepresentationIdFileIdDelete(String aipId, String representationId,
    String fileId) throws NotFoundException;

  public abstract Response aipsAipIdDescriptiveMetadataGet(String aipId, String start, String limit,
    String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdDescriptiveMetadataMetadataIdGet(String aipId, String metadataId,
    String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdDescriptiveMetadataMetadataIdPut(String aipId, String metadataId,
    FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException;

  public abstract Response aipsAipIdDescriptiveMetadataMetadataIdPost(String aipId, String metadataId,
    FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException;

  public abstract Response aipsAipIdDescriptiveMetadataMetadataIdDelete(String aipId, String metadataId)
    throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataGet(String aipId, String start, String limit,
    String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataRepresentationIdGet(String aipId, String representationId,
    String start, String limit, String acceptFormat) throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataRepresentationIdFileIdGet(String aipId, String representationId,
    String fileId) throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataRepresentationIdFileIdPost(String aipId,
    String representationId, InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataRepresentationIdFileIdPut(String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException;

  public abstract Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(String aipId,
    String representationId, String fileId) throws NotFoundException;

}
