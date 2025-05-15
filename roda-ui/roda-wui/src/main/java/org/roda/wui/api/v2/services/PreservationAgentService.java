package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.DirectResourceAccess;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class PreservationAgentService {

  public StreamResponse retrievePreservationAgentFile(String id)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    ModelService model = RodaCoreFactory.getModelService();

    final Binary binary = model.retrievePreservationAgent(id);
    final DirectResourceAccess agentResource = model.getDirectAccess(IndexedPreservationAgent.class, id,
      RodaConstants.PREMIS_SUFFIX);

    final ConsumesOutputStream stream = new BinaryConsumesOutputStream(binary, agentResource.getPath(),
      RodaConstants.MEDIA_TYPE_APPLICATION_XML);
    return new StreamResponse(stream);
  }
}
