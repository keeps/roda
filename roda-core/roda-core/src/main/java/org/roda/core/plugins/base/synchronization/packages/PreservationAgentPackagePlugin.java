package org.roda.core.plugins.base.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PreservationAgentPackagePlugin extends RodaEntityPackagesPlugin<IndexedPreservationAgent> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "PreservationAgentPackagePlugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new PreservationAgentPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "preservation_agent";
  }

  @Override
  protected Class<IndexedPreservationAgent> getEntityClass() {
    return IndexedPreservationAgent.class;
  }

  @Override
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    return Arrays
      .asList(index.findAll(IndexedPreservationAgent.class, new Filter(), Arrays.asList(RodaConstants.INDEX_UUID)));
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof IndexedPreservationAgent) {
        PreservationMetadata retrieveAgent = model.retrievePreservationMetadata(
          ((IndexedPreservationAgent) object).getId(), PreservationMetadata.PreservationMetadataType.AGENT);
        createAgentBundle(model, retrieveAgent);
      }
    }
  }

  private void createAgentBundle(ModelService model, PreservationMetadata agent)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    StorageService storage = model.getStorage();
    StoragePath agentStoragePath = ModelUtils.getPreservationAgentStoragePath();
    String agentFile = FSUtils.encodePathPartial(agent.getId() + RodaConstants.PREMIS_SUFFIX);

    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_AGENTS);

    Path agentPath = destinationPath.resolve(agentFile);

    storage.copy(storage, agentStoragePath, agentPath, agentFile);
  }
}
