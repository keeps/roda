package org.roda.wui.api.v2.services;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;


/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class AIPService {

  public List<IndexedAIP> getAncestors(IndexedAIP indexedAIP, User user) throws GenericException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(indexedAIP, user, new ArrayList<>());
  }
}
