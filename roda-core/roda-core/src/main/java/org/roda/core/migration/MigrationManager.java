package org.roda.core.migration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.reflections.Reflections;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ModelInfo;
import org.roda.core.data.v2.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MigrationManager.class);

  private Path modelInfoFile;
  // map<model class, workflow>
  private Map<String, MigrationWorkflow> modelMigrations = new HashMap<>();
  private Map<String, MigrationWorkflow> indexMigrations = new HashMap<>();

  public MigrationManager(Path dataFolder) {
    super();
    this.modelInfoFile = dataFolder.resolve("model.json");
  }

  public boolean isNecessaryToPerformMigration() throws GenericException {
    boolean migrationIsNecessary = false;

    // check if model migration is necessary
    migrationIsNecessary = isModelMigrationNecessary();

    // check if index migration is necessary
    migrationIsNecessary = migrationIsNecessary && isIndexMigrationNecessary();

    return migrationIsNecessary;
  }

  // 20161031 hsilva: this method is not invoked in the constructor as it might
  // get very big & therefore
  // should be done in a lazy fashion
  public void setupModelMigrations() {
    // FIXME 20161031 hsilva: the following line is just an example and should
    // be removed as soon as one real migration is configured
    // addModelMigration(Job.class, 2, JobToVersion2.class);
  }

  // 20161031 hsilva: this method is not invoked in the constructor as it might
  // get very big & therefore
  // should be done in a lazy fashion
  public void setupIndexMigrations() {
    // FIXME 20161031 hsilva: the following line is just an example and should
    // be removed as soon as one real migration is configured
    // addIndexMigration();
  }

  private void addModelMigration(Class<? extends IsModelObject> clazz, int toVersion,
    Class<? extends MigrationAction> migrationClass) {
    String className = clazz.getName();
    MigrationWorkflow classMigrations = modelMigrations.getOrDefault(className, new MigrationWorkflow());
    // at the very last I'm updating pointers
    modelMigrations.put(className, classMigrations);
    classMigrations.addMigration(toVersion, migrationClass);
  }

  public void performModelMigrations() throws GenericException {
    ModelInfo modelInfo = JsonUtils.getObjectFromJson(modelInfoFile, ModelInfo.class);

    // perform migrations
    for (Entry<String, MigrationWorkflow> classMigrations : modelMigrations.entrySet()) {
      String className = classMigrations.getKey();
      LOGGER.info("Performing migration for class '{}'", className);
      for (Pair<Integer, Class<? extends MigrationAction>> classMigration : classMigrations.getValue()
        .getMigrations()) {
        Integer toVersion = classMigration.getFirst();
        Class<? extends MigrationAction> migrationClass = classMigration.getSecond();
        LOGGER.info("Migrating to version {} using class '{}'", toVersion, migrationClass.getName());
        try {
          // migrate
          migrationClass.newInstance().migrate();
          LOGGER.info("Done migrating to version {}", toVersion);

          // update class specific version after successful migration
          modelInfo.getInstalledClassesVersions().put(className, toVersion);
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.error("Error instantiating migration class '{}' (which migrates to version {})",
            migrationClass.getName(), toVersion, e);
        }
      }
      LOGGER.info("Done migrating class '{}'", className);
    }

    // update model info. file
    JsonUtils.writeObjectToFile(modelInfo, modelInfoFile);
  }

  public void performIndexMigrations() throws GenericException {
    // FIXME 20161031 hsilva: implement this
  }

  private boolean isModelMigrationNecessary() throws GenericException {
    boolean migrationIsNecessary = false;
    Map<String, Integer> modelClassesVersionsFromCode = getCodeModelClassesVersions(true, "Indexed");
    Map<String, Integer> modelClassesVersionInstalled = new HashMap<>();

    if (Files.exists(modelInfoFile)) {
      modelClassesVersionInstalled = JsonUtils.getObjectFromJson(modelInfoFile, ModelInfo.class)
        .getInstalledClassesVersions();
    }

    if (modelClassesVersionInstalled.isEmpty()) {
      // no information, lets assume first RODA execution
      LOGGER.info("No model info. available. Writing initial model info. to file {}", modelInfoFile);
      JsonUtils.writeObjectToFile(new ModelInfo().setInstalledClassesVersions(modelClassesVersionsFromCode),
        modelInfoFile);
    } else {
      // information exists in file, lets see if any migration is needed
      for (Entry<String, Integer> classVersion : modelClassesVersionsFromCode.entrySet()) {
        String classFromCode = classVersion.getKey();
        int versionFromCode = classVersion.getValue();

        LOGGER.debug("Checking if model class '{}' requires to do a migration...", classFromCode);

        // previous information about a class already exists
        if (modelClassesVersionInstalled.containsKey(classFromCode)) {
          int versionInstalled = modelClassesVersionInstalled.get(classFromCode);
          if (versionInstalled != versionFromCode) {
            LOGGER.warn(
              "A migration may be needed! Model class '{}' version is set to {} in code & installed version is set to {}",
              classFromCode, versionFromCode, versionInstalled);
            migrationIsNecessary = true;
          }
        } else {
          // class information does not exists, probably is new & therefore no
          // model migration is needed
          LOGGER.info("No migration is needed as no previous information about model class '{}' exists");
        }
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

  private boolean isIndexMigrationNecessary() {
    // FIXME 20161031 hsilva: implement this
    return false;
  }

  private Map<String, Integer> getCodeModelClassesVersions(final boolean avoidClassesByNamePrefix,
    final String avoidByNamePrefix) {
    Map<String, Integer> ret = new HashMap<>();
    Reflections reflections = new Reflections("org.roda.core.data.v2");
    Set<Class<? extends IsModelObject>> modelClasses = reflections.getSubTypesOf(IsModelObject.class);
    for (Class<? extends IsModelObject> clazz : modelClasses) {
      if (avoidClassesByNamePrefix && clazz.getSimpleName().startsWith(avoidByNamePrefix)) {
        continue;
      }

      try {
        ret.put(clazz.getName(), clazz.newInstance().getModelVersion());
      } catch (InstantiationException | IllegalAccessException e) {
        LOGGER.error("Unable to determine class '{}' model version", clazz.getName(), e);
      }
    }
    return ret;
  }

  private class MigrationWorkflow {
    private int lastToVersion = -1;
    private List<Pair<Integer, Class<? extends MigrationAction>>> migrations = new ArrayList<>();

    public void addMigration(int toVersion, Class<? extends MigrationAction> migrationActionClazz) {
      if (lastToVersion < toVersion) {
        lastToVersion = toVersion;
        migrations.add(new Pair<Integer, Class<? extends MigrationAction>>(toVersion, migrationActionClazz));
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

  }

}
