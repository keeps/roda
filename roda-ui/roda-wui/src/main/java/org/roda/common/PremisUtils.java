package org.roda.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.roda.core.data.FileFormat;
import org.roda.core.data.v2.Fixity;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.model.File;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.util.FileUtility;

public class PremisUtils {
  private final static Logger logger = Logger.getLogger(PremisUtils.class);

  public static Fixity calculateFixity(Binary binary, String digestAlgorithm, String originator) throws IOException, NoSuchAlgorithmException {
    InputStream dsInputStream = binary.getContent().createInputStream();
    Fixity fixity = new Fixity(digestAlgorithm, FileUtility.calculateChecksumInHex(dsInputStream, digestAlgorithm),
      originator);
    dsInputStream.close();
    return fixity;
  }

  public static RepresentationFilePreservationObject createPremisFromFile(File file, Binary binaryFile, String originator) throws IOException, PremisMetadataException{
    RepresentationFilePreservationObject pObjectFile = new RepresentationFilePreservationObject();
    pObjectFile.setID(file.getId());
    pObjectFile.setPreservationLevel(RepresentationFilePreservationObject.PRESERVATION_LEVEL_FULL);
    pObjectFile.setCompositionLevel(0);
    try {
      Fixity[] fixities = new Fixity[2];
      fixities[0] = calculateFixity(binaryFile, "MD5", originator);
      fixities[1] = calculateFixity(binaryFile, "SHA-1", originator);
      pObjectFile.setFixities(fixities);
    } catch (NoSuchAlgorithmException e) {
      logger.error("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new PremisMetadataException("Error calculating datastream checksum - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new PremisMetadataException("Error calculating datastream checksum - " + e.getMessage(), e);
    }

    pObjectFile.setSize(binaryFile.getSizeInBytes());
    pObjectFile.setObjectCharacteristicsExtension("");
    pObjectFile.setOriginalName(binaryFile.getStoragePath().getName());
    pObjectFile.setContentLocationType("");
    pObjectFile.setContentLocationValue("");
    pObjectFile.setFormatDesignationName("");
    return pObjectFile;
  }
  
  public static RepresentationFilePreservationObject addFormatToPremis(RepresentationFilePreservationObject pObjectFile,FileFormat format) throws IOException, PremisMetadataException{
    pObjectFile.setFormatDesignationName(format.getMimetype());
    pObjectFile.setFormatRegistryName("pronom");
    pObjectFile.setFormatRegistryKey(format.getPuid());    
    return pObjectFile;
  }

  public static RepresentationFilePreservationObject getPremisFile(StorageService storage, String aipID, String representationID, String fileID) throws StorageServiceException, IOException, PremisMetadataException {
    Binary binary = storage.getBinary(ModelUtils.getPreservationFilePath(aipID, representationID, fileID));
    Path p = Files.createTempFile("temp", ".premis.xml");
    Files.copy(binary.getContent().createInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
    return PremisFileObjectHelper.newInstance(Files.newInputStream(p)).getRepresentationFilePreservationObject();
  }
}
