package org.roda.core.plugins.plugins.synchronization.packages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
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

  @Override
  public String getName() {
    return "PreservationAgentPackagePlugin";
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
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    ArrayList<String> preservationAgentList = new ArrayList<>();
    IterableIndexResult<IndexedPreservationAgent> agents = index.findAll(IndexedPreservationAgent.class, new Filter(),
      Arrays.asList(RodaConstants.INDEX_UUID));
    for (IndexedPreservationAgent agent : agents) {
      preservationAgentList.add(agent.getId());
    }
    return preservationAgentList;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {
    for (String agentId : list) {
      PreservationMetadata retrieveAgent = model.retrievePreservationMetadata(agentId,
        PreservationMetadata.PreservationMetadataType.AGENT);
      createAgentBundle(model, retrieveAgent);
    }
  }

  public void createAgentBundle(ModelService model, PreservationMetadata agent) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath agentStoragePath = ModelUtils.getPreservationAgentStoragePath();
    String agentFile = FSUtils.encodePathPartial(agent.getId() + RodaConstants.PREMIS_SUFFIX);

    Path destinationPath = bundlePath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_AGENTS);

    Path agentPath = destinationPath.resolve(agentFile);

    storage.copy(storage, agentStoragePath, agentPath, agentFile);
  }
}
