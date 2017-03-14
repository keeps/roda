/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.AIP;
import org.roda_project.commons_ip.model.IPContentType;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPMetadata;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MetadataType;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.RepresentationContentType;
import org.roda_project.commons_ip.model.RepresentationStatus;
import org.roda_project.commons_ip.model.impl.AIPWrap;
import org.roda_project.commons_ip.model.impl.BasicAIP;
import org.roda_project.commons_ip.utils.IPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link AIP} implementation that can read a RODA AIP from a folder.
 *
 * @author Rui Castro (rui.castro@gmail.com)
 */
final class RodaFolderAIP extends AIPWrap {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaFolderAIP.class);

  /**
   * Constant string "id".
   */
  private static final String ID = "id";
  /**
   * Constant string "type".
   */
  private static final String TYPE = "type";
  /**
   * Constant string "version".
   */
  private static final String VERSION = "version";
  /**
   * Constant string "representations".
   */
  private static final String REPRESENTATIONS = "representations";
  /**
   * Constant string "metadata".
   */
  private static final String METADATA = "metadata";
  /**
   * Constant string "descriptive".
   */
  private static final String DESCRIPTIVE = "descriptive";
  /**
   * Constant string "descriptiveMetadata".
   */
  private static final String DESCRIPTIVE_METADATA = "descriptiveMetadata";
  /**
   * Constant string "preservation".
   */
  private static final String PRESERVATION = "preservation";
  /**
   * Constant string "other".
   */
  private static final String OTHER = "other";
  /**
   * Constant string "schemas".
   */
  private static final String SCHEMAS = "schemas";
  /**
   * Constant string "documentation".
   */
  private static final String DOCUMENTATION = "documentation";
  /**
   * Constant string "submission".
   */
  private static final String SUBMISSION = "submission";

  /**
   * Constructor.
   *
   * @param aip
   *          the {@link AIP} to wrap.
   * @param path
   *          AIP path.
   */
  private RodaFolderAIP(final AIP aip, final Path path) {
    super(aip);
    setBasePath(path);
  }

  /**
   * Reads a {@link RodaFolderAIP} from the given folder.
   *
   * @param source
   *          the source folder.
   * @return a {@link RodaFolderAIP}.
   * @throws ParseException
   *           if some error occurs.
   */
  static AIP parse(final Path source) throws ParseException {
    return new RodaFolderAIP(new BasicAIP(), source).read();
  }

  /**
   * Read the AIP.
   *
   * @return the {@link AIP}.
   * @throws ParseException
   *           if some error occurred.
   */
  private AIP read() throws ParseException {
    try {

      final ObjectMapper mapper = new ObjectMapper();
      final JsonNode json = mapper.readTree(getBasePath().resolve("aip.json").toFile());

      this.setId(getBasePath().getFileName().toString());
      if (json.has("parentId")) {
        this.setAncestors(Collections.singletonList(json.get("parentId").asText()));
      }
      this.setContentType(new IPContentType(json.get(TYPE).asText()));
      this.setState(json.get("state").asText());

      final Path mdPath = getBasePath().resolve(METADATA);

      readJsonDescriptiveMDs(this, json);
      findAndAddPreservationMDs(this::addPreservationMetadata);
      findAndAddOtherMDs(mdPath.resolve(OTHER), this::addOtherMetadata);
      findIPFiles(getBasePath().resolve(SCHEMAS)).forEach(this::addSchema);
      findIPFiles(getBasePath().resolve(DOCUMENTATION)).forEach(this::addDocumentation);
      findIPFiles(getBasePath().resolve(SUBMISSION)).forEach(this::addSubmission);

      readRepresentations(this, json);

      return this;
    } catch (final IOException | IPException e) {
      LOGGER.debug("Error reading aip.json", e);
      throw new ParseException("Error reading aip.json", e);
    }
  }

  /**
   * Read {@link IPRepresentation} and update the {@link AIP}.
   *
   * @param aip
   *          the {@link AIP}.
   * @param json
   *          the JSON node.
   * @throws IPException
   *           if some error occurs.
   * @throws IOException
   *           if some I/O error occurs.
   */
  private void readRepresentations(final AIP aip, final JsonNode json) throws IPException, IOException {
    if (json.has(REPRESENTATIONS)) {
      final Iterator<JsonNode> iterator = json.get(REPRESENTATIONS).elements();
      while (iterator.hasNext()) {
        aip.addRepresentation(readRepresentation(iterator.next()));
      }
    }
  }

  /**
   * Read {@link IPRepresentation}.
   *
   * @param json
   *          the JSON node.
   * @return the {@link IPRepresentation}.
   * @throws IOException
   *           if some I/O error occurs.
   * @throws IPException
   *           if some error occured.
   */
  private IPRepresentation readRepresentation(final JsonNode json) throws IOException, IPException {
    final IPRepresentation rep = new IPRepresentation(json.get(ID).asText());
    rep.setStatus(
      json.get("original").asBoolean() ? RepresentationStatus.getORIGINAL() : RepresentationStatus.getOTHER());
    rep.setContentType(new RepresentationContentType(json.get(TYPE).asText()));

    final Path repPath = getBasePath().resolve(REPRESENTATIONS).resolve(rep.getRepresentationID());
    final Path mdPath = repPath.resolve(METADATA);

    findIPFiles(repPath.resolve("data")).forEach(rep::addFile);
    findDescriptiveMDs(mdPath.resolve(DESCRIPTIVE)).forEach(rep::addDescriptiveMetadata);
    findPreservationMDs(mdPath.resolve(PRESERVATION)).forEach(rep::addPreservationMetadata);
    findAndAddOtherMDs(mdPath.resolve(OTHER), rep::addOtherMetadata);
    // TODO: ask for example of representation agents
    findIPFiles(repPath.resolve(SCHEMAS)).forEach(rep::addDocumentation);
    findIPFiles(repPath.resolve(DOCUMENTATION)).forEach(rep::addDocumentation);

    return rep;
  }

  /**
   * Read {@link IPDescriptiveMetadata} records from <code>aip.json</code>
   * "descriptiveMetadata" field and update the {@link AIP}.
   *
   * @param aip
   *          the {@link AIP}.
   * @param json
   *          the JSON node.
   * @throws IPException
   *           if some error occurs.
   */
  private void readJsonDescriptiveMDs(final AIP aip, final JsonNode json) throws IPException {
    if (json.has(DESCRIPTIVE_METADATA)) {
      final Iterator<JsonNode> iterator = json.get(DESCRIPTIVE_METADATA).elements();
      while (iterator.hasNext()) {
        aip.addDescriptiveMetadata(readJsonDescriptionMD(iterator.next()));
      }
    }
  }

  /**
   * Read {@link IPDescriptiveMetadata} from a {@link JsonNode}.
   *
   * @param json
   *          the JSON node.
   * @return the {@link IPDescriptiveMetadata}.
   */
  private IPDescriptiveMetadata readJsonDescriptionMD(final JsonNode json) {
    final String id = json.get(ID).asText();
    final String version = json.has(VERSION) ? json.get(VERSION).asText() : null;
    return new IPDescriptiveMetadata(id, new IPFile(getBasePath().resolve(METADATA).resolve(DESCRIPTIVE).resolve(id)),
      new MetadataType(json.get(TYPE).asText()), version);
  }

  /**
   * Find preservation metadata files and adds them to {@link AIP}.
   *
   * @param mdSetter
   *          the {@link IPMetadataSetter}.
   * @throws IOException
   *           if some I/O error occurs.
   * @throws IPException
   *           if some other error occurs.
   */
  private void findAndAddPreservationMDs(final IPMetadataSetter mdSetter) throws IOException, IPException {
    for (IPMetadata md : findPreservationMDs(getBasePath().resolve(METADATA).resolve(PRESERVATION))) {
      mdSetter.addMetadata(md);
    }
  }

  /**
   * Find other metadata files and for each one calls
   * {@link IPMetadataSetter#addMetadata(IPMetadata)}.
   *
   * @param omdPath
   *          the {@link Path} to other metadata files.
   * @param mdSetter
   *          the {@link IPMetadataSetter}.
   * @throws IOException
   *           if some I/O error occurs.
   * @throws IPException
   *           if some other error occurs.
   */
  private void findAndAddOtherMDs(final Path omdPath, final IPMetadataSetter mdSetter) throws IOException, IPException {
    if (FSUtils.isDirectory(omdPath)) {
      final Iterator<Path> paths = Files.list(omdPath).iterator();
      while (paths.hasNext()) {
        final Path mdPath = paths.next();
        if (FSUtils.isDirectory(mdPath)) {
          for (IPMetadata md : findMDs(mdPath, MetadataType.OTHER().setOtherType(mdPath.getFileName().toString()))) {
            mdSetter.addMetadata(md);
          }
        }
        if (FSUtils.isFile(mdPath)) {
          for (IPMetadata md : findMDs(mdPath, MetadataType.OTHER())) {
            mdSetter.addMetadata(md);
          }
        }
      }
    }
  }

  /**
   * Find {@link IPDescriptiveMetadata}s in the given {@link Path}.
   *
   * @param dmdPath
   *          the {@link Path}.
   * @return the {@link List <IPDescriptiveMetadata>}.
   * @throws IOException
   *           if some I/O error occurs.
   */
  private List<IPDescriptiveMetadata> findDescriptiveMDs(final Path dmdPath) throws IOException {
    final List<IPDescriptiveMetadata> list = new ArrayList<>();
    if (FSUtils.isDirectory(dmdPath)) {
      try (Stream<Path> paths = Files.walk(dmdPath)) {
        paths.forEach(filePath -> {
          if (FSUtils.isFile(filePath)) {
            list.add(new IPDescriptiveMetadata(FilenameUtils.removeExtension(filePath.getFileName().toString()),
              new IPFile(filePath), MetadataType.OTHER(), null));
          }
        });
      }
    }
    return list;
  }

  /**
   * Find {@link IPMetadata}s in the given {@link Path}.
   *
   * @param mdPath
   *          the {@link Path}.
   * @return the {@link List<IPMetadata>}.
   * @throws IOException
   *           if some I/O error occurs.
   */
  private List<IPMetadata> findPreservationMDs(final Path mdPath) throws IOException {
    return findMDs(mdPath, new MetadataType(MetadataType.MetadataTypeEnum.PREMIS));
  }

  /**
   * Find {@link IPMetadata}s in the given {@link Path}.
   *
   * @param mdPath
   *          the {@link Path}.
   * @param mdType
   *          the {@link MetadataType}.
   * @return the {@link List<IPMetadata>}.
   * @throws IOException
   *           if some I/O error occurs.
   */
  private List<IPMetadata> findMDs(final Path mdPath, final MetadataType mdType) throws IOException {
    final List<IPMetadata> list = new ArrayList<>();
    if (FSUtils.isDirectory(mdPath)) {
      try (Stream<Path> paths = Files.walk(mdPath)) {
        paths.forEach(filePath -> {
          if (FSUtils.isFile(filePath)) {
            list.add(new IPMetadata(FilenameUtils.removeExtension(filePath.getFileName().toString()),
              new IPFile(filePath), mdType));
          }
        });
      }
    }
    return list;
  }

  /**
   * Find {@link IPFile}s in the given {@link Path}.
   *
   * @param filesPath
   *          the {@link Path}.
   * @return the {@link List<IPFile>}.
   * @throws IOException
   *           if some I/O error occurs.
   */
  private List<IPFile> findIPFiles(final Path filesPath) throws IOException {
    final List<IPFile> list = new ArrayList<>();
    if (FSUtils.isDirectory(filesPath)) {
      try (Stream<Path> paths = Files.walk(filesPath)) {
        paths.forEach(filePath -> {
          if (FSUtils.isFile(filePath)) {
            list.add(new IPFile(filePath));
          }
        });
      }
    }
    return list;
  }

  /**
   * {@link IPMetadata} setter interface. Implementations of this interface
   * should add the {@link IPMetadata} records to themselves.
   */
  interface IPMetadataSetter {
    /**
     * Add the specified metadata.
     *
     * @param md
     *          the {@link IPMetadata}.
     * @throws IPException
     *           if some error occurred.
     */
    void addMetadata(IPMetadata md) throws IPException;
  }

}
