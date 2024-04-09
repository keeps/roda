package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {

  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id, List<String> fieldsToReturn)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(returnClass, id, fieldsToReturn);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, User user, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(returnClass, filter, sorter, sublist, facets, user, justActive,
      fieldsToReturn);
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, boolean justActive, User user)
      throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(returnClass, filter, user, justActive);
  }
}
