package org.roda.api.v1.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.api.controllers.Browser;
import org.roda.api.v1.utils.ApiResponseMessage;
import org.roda.api.v1.utils.NotFoundException;
import org.roda.common.UserUtility;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.PreservationMetadata;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.disseminators.common.tools.ZipEntryInfo;
import pt.gov.dgarq.roda.disseminators.common.tools.ZipTools;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsResourceImpl {
  private static final Logger LOGGER = Logger.getLogger(AipsResourceImpl.class);

  public Response aipsGet(String start, String limit) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic2!")).build();
  }

  public Response aipsAipIdGet(String aipId, String acceptFormat) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPut(String aipId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPost(String aipId, String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDelete(String aipId) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataGet(String aipId, String start, String limit) throws NotFoundException {
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

  public Response aipsAipIdDataRepresentationIdPut(String aipId, String representationId, String filepath)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdPost(String aipId, String representationId, String filepath)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdDelete(String aipId, String representationId) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdGet(String aipId, String representationId, String fileId,
    String acceptFormat) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdPut(String aipId, String representationId, String fileId,
    String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdPost(String aipId, String representationId, String fileId,
    String filepath) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDataRepresentationIdFileIdDelete(String aipId, String representationId, String fileId)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
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

  public Response aipsAipIdDescriptiveMetadataMetadataIdGet(String aipId, String metadataId, String acceptFormat)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(String aipId, String metadataId,
    FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(String aipId, String metadataId,
    FormDataContentDisposition fileDetail, String metadataType) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(String aipId, String metadataId)
    throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  public Response aipsAipIdPreservationMetadataGet(String aipId, String start, String limit, String acceptFormat)
    throws NotFoundException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      ClosableIterable<Representation> representations = model.listRepresentations(aipId);
      int startInt = (start == null) ? 0 : Integer.parseInt(start);
      int limitInt = (limit == null) ? -1 : Integer.parseInt(limit);
      if (acceptFormat != null && acceptFormat.equalsIgnoreCase("bin")) {
        int counter = 0;
        List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
        for (Representation r : representations) {
          ClosableIterable<PreservationMetadata> preservationFiles = model.listPreservationMetadataBinaries(aipId,
            r.getId());
          for (PreservationMetadata preservationFile : preservationFiles) {
            if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
              Binary binary = storage.getBinary(preservationFile.getStoragePath());
              Path tempFile = Files.createTempFile("test", ".tmp");
              Files.copy(binary.getContent().createInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
              ZipEntryInfo info = new ZipEntryInfo(
                r.getId() + File.separator + preservationFile.getStoragePath().getName(), tempFile.toFile());
              zipEntries.add(info);
            } else {
              break;
            }

            counter++;
          }
        }
        String filename = "";
        StreamingOutput stream = null;
        if (zipEntries.size() == 1) {
          stream = new StreamingOutput() {

            public void write(OutputStream os) throws IOException, WebApplicationException {
              IOUtils.copy(zipEntries.get(0).getInputStream(), os);
            }
          };
          filename = zipEntries.get(0).getName();
        } else {
          stream = new StreamingOutput() {

            public void write(OutputStream os) throws IOException, WebApplicationException {
              ZipTools.zip(zipEntries, os);
            }
          };
          filename = aipId + ".zip";
        }
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
          .header("content-disposition", "attachment; filename = " + filename).build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (IOException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdGet(String aipId, String representationId, String start,
    String limit, String acceptFormat) throws NotFoundException {
    try {
      StorageService storage = RodaCoreFactory.getStorageService();
      ModelService model = RodaCoreFactory.getModelService();
      int startInt = (start == null) ? 0 : Integer.parseInt(start);
      int limitInt = (limit == null) ? -1 : Integer.parseInt(limit);
      if (acceptFormat != null && acceptFormat.equalsIgnoreCase("bin")) {
        int counter = 0;
        List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
        ClosableIterable<PreservationMetadata> preservationFiles = model.listPreservationMetadataBinaries(aipId,
          representationId);
        for (PreservationMetadata preservationFile : preservationFiles) {
          if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
            Binary binary = storage.getBinary(preservationFile.getStoragePath());
            Path tempFile = Files.createTempFile("test", ".tmp");
            Files.copy(binary.getContent().createInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            ZipEntryInfo info = new ZipEntryInfo(preservationFile.getStoragePath().getName(), tempFile.toFile());
            zipEntries.add(info);
          } else {
            break;
          }
          counter++;
        }

        String filename = "";
        StreamingOutput stream = null;
        if (zipEntries.size() == 1) {
          stream = new StreamingOutput() {

            public void write(OutputStream os) throws IOException, WebApplicationException {
              IOUtils.copy(zipEntries.get(0).getInputStream(), os);
            }
          };
          filename = zipEntries.get(0).getName();
        } else {
          stream = new StreamingOutput() {

            public void write(OutputStream os) throws IOException, WebApplicationException {
              ZipTools.zip(zipEntries, os);
            }
          };
          filename = aipId + "_" + representationId + ".zip";
        }
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
          .header("content-disposition", "attachment; filename = " + filename).build();
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
          .build();
      }
    } catch (IOException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } catch (StorageServiceException | ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdGet(String aipId, String representationId,
    String fileId) throws NotFoundException {
    try {
      StorageService storage = RodaCoreFactory.getStorageService();
      Binary binary = storage.getBinary(ModelUtils.getPreservationFilePath(aipId, representationId, fileId));

      String filename = binary.getStoragePath().getName();
      StreamingOutput stream = new StreamingOutput() {

        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(binary.getContent().createInputStream(), os);
        }
      };

      return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
        .header("content-disposition", "attachment; filename = " + filename).build();
    } catch (StorageServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPost(String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException {

    try {
      ModelService model = RodaCoreFactory.getModelService();
      Path p = Files.createTempFile("preservation", ".tmp");
      Files.copy(is, p, StandardCopyOption.REPLACE_EXISTING);
      Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
      model.createPreservationMetadata(aipId, representationId, fileDetail.getFileName(), resource);
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (ModelServiceException | StorageServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (IOException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPut(String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail) throws NotFoundException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      Path p = Files.createTempFile("preservation", ".tmp");
      Files.copy(is, p, StandardCopyOption.REPLACE_EXISTING);
      Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
      model.updatePreservationMetadata(aipId, representationId, fileDetail.getFileName(), resource);
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (ModelServiceException | StorageServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    } catch (IOException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
  }

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(String aipId, String representationId,
    String fileId) throws NotFoundException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deletePreservationMetadata(aipId, representationId, fileId);
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    } catch (ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getCode(), e.getMessage());
      } else {
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    }
  }

}
