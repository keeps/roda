/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.migration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.common.util.NamedList;
import org.reflections.Reflections;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.XMLUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ModelInfo;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.migration.model.PreservationMetadataFileToVersion2;
import org.roda.core.migration.model.RepresentationToVersion2;
import org.roda.core.migration.model.RiskToVersion2;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MigrationManager.class);

  private Path modelInfoFile;
  // map<model class, workflow>
  private Map<String, MigrationWorkflow> modelMigrations = new HashMap<>();

  public MigrationManager(Path dataFolder) {
    super();
    this.modelInfoFile = dataFolder.resolve("model.json");
  }

  // 20161031 hsilva: this method is not invoked in the constructor as it might
  // get very big & therefore should be done in a lazy fashion
  public void setupModelMigrations() throws GenericException {
    addModelMigration(Risk.class, 2, RiskToVersion2.class);
    addModelMigration(PreservationMetadata.class, 2, PreservationMetadataFileToVersion2.class);
    addModelMigration(Representation.class, 2, RepresentationToVersion2.class);
  }

  private <T extends IsModelObject> void addModelMigration(final Class<T> clazz, final int toVersion,
    final Class<? extends MigrationAction<T>> migrationClass) throws GenericException {
    String className = clazz.getName();
    MigrationWorkflow classMigrations = modelMigrations.getOrDefault(className, new MigrationWorkflow());
    // at the very last I'm updating pointers
    modelMigrations.put(className, classMigrations);

    try {
      MigrationAction<T> migrationAction = migrationClass.newInstance();
      if (migrationAction.isToVersionValid(toVersion)) {
        classMigrations.addMigration(toVersion, migrationClass);
      } else {
        LOGGER.error(
          "Trying to configure migration for model class '{}', setting toVersion to '{}' using action class '{}' but this class says the toVersion is not valid",
          className, toVersion, migrationClass.getName());
        throw new GenericException(
          "Trying to configure migration for model class '" + className + "' with the wrong toVersion");
      }
    } catch (InstantiationException | IllegalAccessException e) {
      LOGGER.error("Error instantiating migration action class '{}' (which migrates to version {})",
        migrationClass.getName(), toVersion, e);
      throw new GenericException("Error instantiating migration action class '" + migrationClass.getName()
        + "' (which migrates to version '" + toVersion + "')");
    }
  }

  public boolean isNecessaryToPerformMigration(final SolrClient solrClient, final Optional<Path> tempIndexConfigsPath)
    throws GenericException {
    boolean migrationIsNecessary;

    // check if model migration is necessary
    migrationIsNecessary = isModelMigrationNecessary();

    // check if index migration is necessary
    migrationIsNecessary |= isIndexMigrationNecessary(solrClient, tempIndexConfigsPath);

    return migrationIsNecessary;
  }

  private boolean isModelMigrationNecessary() throws GenericException {
    boolean migrationIsNecessary = false;
    Map<String, Integer> modelClassesVersionsFromCode = getModelClassesVersionsFromCode(true, "Indexed");
    Map<String, Integer> modelClassesVersionsInstalled = new HashMap<>();

    if (FSUtils.exists(modelInfoFile)) {
      modelClassesVersionsInstalled = JsonUtils.getObjectFromJson(modelInfoFile, ModelInfo.class)
        .getInstalledClassesVersions();
    }

    if (modelClassesVersionsInstalled.isEmpty()) {
      // no information, lets assume first RODA execution
      LOGGER.info("No model info. available. Writing initial model info. to file {}", modelInfoFile);
      JsonUtils.writeObjectToFile(new ModelInfo().setInstalledClassesVersions(modelClassesVersionsFromCode),
        modelInfoFile);
    } else {
      // information exists in file, lets see if any migration is needed
      Map<String, Integer> newModelClassesVersionsToBeWritten = new HashMap<>();
      for (Entry<String, Integer> classVersionFromCode : modelClassesVersionsFromCode.entrySet()) {
        String classFromCode = classVersionFromCode.getKey();
        int versionFromCode = classVersionFromCode.getValue();

        LOGGER.debug("Checking if model class '{}' requires to do a migration...", classFromCode);

        // previous information about a class already exists
        if (modelClassesVersionsInstalled.containsKey(classFromCode)) {
          int versionInstalled = modelClassesVersionsInstalled.get(classFromCode);
          if (versionInstalled != versionFromCode) {
            LOGGER.warn(
              "A migration may be needed! Model class '{}' version is set to {} in code & installed version is set to {}",
              classFromCode, versionFromCode, versionInstalled);
            migrationIsNecessary = true;
          }
        } else {
          // class information does not exists, probably is new & therefore no
          // model migration is needed
          LOGGER.info(
            "No migration is needed as no previous information about model class '{}' exists. Will write its information as it is new!",
            classFromCode);
          newModelClassesVersionsToBeWritten.put(classFromCode, versionFromCode);
        }
      }

      // write (just) new classes
      if (!newModelClassesVersionsToBeWritten.isEmpty()) {
        modelClassesVersionsInstalled.putAll(newModelClassesVersionsToBeWritten);
        LOGGER.info("Updating model info. with new classes. to file {}", modelInfoFile);
        JsonUtils.writeObjectToFile(new ModelInfo().setInstalledClassesVersions(modelClassesVersionsInstalled),
          modelInfoFile);
      }

      if (migrationIsNecessary) {
        LOGGER.warn(
          "A migration might be needed. But if you know what you're doing & realize that no migration is needed, write the following content in file '{}':{}{}{}{}",
          modelInfoFile, System.lineSeparator(), System.lineSeparator(),
          JsonUtils.getJsonFromObject(new ModelInfo().setInstalledClassesVersions(modelClassesVersionsFromCode)),
          System.lineSeparator());
      }
    }

    return migrationIsNecessary;
  }

  private Map<String, Integer> getModelClassesVersionsFromCode(final boolean avoidClassesByNamePrefix,
    final String avoidByNamePrefix) {
    Map<String, Integer> ret = new HashMap<>();
    Reflections reflections = new Reflections("org.roda.core.data.v2");
    Set<Class<? extends IsModelObject>> modelClasses = reflections.getSubTypesOf(IsModelObject.class);
    for (Class<? extends IsModelObject> clazz : modelClasses) {
      if (Modifier.isAbstract(clazz.getModifiers())
        || (avoidClassesByNamePrefix && clazz.getSimpleName().startsWith(avoidByNamePrefix))) {
        continue;
      }

      try {
        ret.put(clazz.getName(), clazz.newInstance().getClassVersion());
      } catch (InstantiationException | IllegalAccessException e) {
        LOGGER.error("Unable to determine class '{}' model version", clazz.getName(), e);
      }
    }
    return ret;
  }

  public void performModelMigrations() throws GenericException {
    ModelInfo modelInfo = JsonUtils.getObjectFromJson(modelInfoFile, ModelInfo.class);

    // perform migrations
    for (Entry<String, MigrationWorkflow> classMigrations : modelMigrations.entrySet()) {
      String className = classMigrations.getKey();
      MigrationWorkflow migrationWorkflow = classMigrations.getValue();
      int installedVersion = modelInfo.getInstalledClassesVersions().getOrDefault(className, Integer.MAX_VALUE);

      // see if there is no need to continue processing this particular class
      // based on installed version (if any)
      if (installedVersion >= migrationWorkflow.getLastToVersion()) {
        continue;
      }

      LOGGER.info("Performing migration for class '{}'", className);
      for (Pair<Integer, Class<? extends MigrationAction>> classMigration : migrationWorkflow.getMigrations()) {
        Integer toVersion = classMigration.getFirst();
        Class<? extends MigrationAction> migrationClass = classMigration.getSecond();

        // see if there is no need to perform this particular migration
        if (installedVersion >= toVersion) {
          continue;
        }

        LOGGER.info("Migrating to version {} using class '{}'", toVersion, migrationClass.getName());
        try {
          // migrate
          migrationClass.newInstance().migrate(RodaCoreFactory.getModelService());
          LOGGER.info("Migrated with success to version {}", toVersion);

          // update class specific version after successful migration
          modelInfo.getInstalledClassesVersions().put(className, toVersion);
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.error("Error instantiating migration action class '{}' (which migrates to version {})",
            migrationClass.getName(), toVersion, e);
          break;
        } catch (RODAException e) {
          LOGGER.error("Error executing migration action '{}'. Stopping migrations for class '{}'.",
            migrationClass.getName(), className, e);
          break;
        }
      }
      LOGGER.info("Done migrating class '{}'", className);
    }

    // update model info. file
    JsonUtils.writeObjectToFile(modelInfo, modelInfoFile);
  }

  private boolean isIndexMigrationNecessary(SolrClient solrClient, Optional<Path> tempIndexConfigsPath)
    throws GenericException {
    boolean migrationIsNecessary = false;
    if (tempIndexConfigsPath.isPresent()) {
      Path indexConfigsFolder = tempIndexConfigsPath.get().resolve(RodaConstants.CORE_CONFIG_FOLDER)
        .resolve(RodaConstants.CORE_INDEX_FOLDER);
      List<String> solrCollections = SolrCollectionRegistry.registryIndexNames();

      Integer baseIndexVersion = getIndexVersionsFromCode();
      Map<String, Integer> indexVersionsInstalled = getIndexVersionsFromSolr(solrClient, solrCollections);

      if (baseIndexVersion == 0 || indexVersionsInstalled.isEmpty()) {
        LOGGER.error("Unable to determine if index migration/migrations is/are needed");
        throw new GenericException("Unable to determine if index migration/migrations is/are needed");
      } else {
        for (Entry<String, Integer> installedIndexVersion : indexVersionsInstalled.entrySet()) {
          String installedCollection = installedIndexVersion.getKey();
          Integer installedCollectionVersion = installedIndexVersion.getValue();
          if (!installedCollectionVersion.equals(baseIndexVersion)) {
            LOGGER.warn("A migration is needed! Collection '{}' version is set to {} but should be {}",
              installedCollection, installedCollectionVersion, baseIndexVersion);
            migrationIsNecessary = true;
          }

        }
      }
    } else {
      LOGGER.error("Unable to determine Solr collections via folder with index configs");
      throw new GenericException("Unable to determine Solr collections via folder with index configs");
    }

    return migrationIsNecessary;
  }

  private Integer getIndexVersionsFromCode() {

    Integer version = 0;

    try (InputStream schema = MigrationManager.class.getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/"
      + RodaConstants.CORE_INDEX_FOLDER + "/" + SolrUtils.COMMON + "/" + SolrUtils.CONF + "/" + SolrUtils.SCHEMA)) {

      String versionString = XMLUtility.getString(schema, "/schema/@name").replaceFirst(".*-", "");

      version = Integer.parseInt(versionString);
    } catch (NumberFormatException | IOException e) {
      LOGGER.error("Error parsing base Solr schema version", e);
    }

    return version;
  }

  private Map<String, Integer> getIndexVersionsFromSolr(SolrClient solrClient, List<String> collections) {
    Map<String, Integer> ret = new HashMap<>();

    SolrRequest request = new SchemaRequest.SchemaName();
    try {
      for (String collection : collections) {
        NamedList<Object> response = solrClient.request(request, collection);
        for (Entry<String, Object> entry : response) {
          String value = entry.getValue().toString();
          if ("name".equals(entry.getKey()) && StringUtils.isNotBlank(value)) {
            String version = value.replaceFirst(".*-", "");
            try {
              ret.put(collection, Integer.parseInt(version));
            } catch (NumberFormatException e) {
              LOGGER.warn("Could not extract version for collection {} from Solr schema name: {}", collection, value,
                e);
            }
            break;
          }
        }
      }
    } catch (SolrServerException | IOException e) {
      // do nothing
    }
    return ret;
  }

  private class MigrationWorkflow {
    private int lastToVersion = Integer.MIN_VALUE;
    private List<Pair<Integer, Class<? extends MigrationAction>>> migrations = new ArrayList<>();

    public void addMigration(int toVersion, Class<? extends MigrationAction> migrationActionClazz) {
      if (lastToVersion < toVersion) {
        lastToVersion = toVersion;
        migrations.add(Pair.of(toVersion, migrationActionClazz));
      } else {
        LOGGER.error(
          "Error trying to add a migration action class out of order (last toVersion added: {}; toVersion to be added: {})",
          lastToVersion, toVersion);
        throw new RuntimeException("Error trying to add a migration action class out of order");
      }
    }

    public List<Pair<Integer, Class<? extends MigrationAction>>> getMigrations() {
      return migrations;
    }

    public int getLastToVersion() {
      return lastToVersion;
    }

  }

}
