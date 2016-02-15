/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import java.io.IOException;
import java.util.Iterator;

import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.storage.Container;
import org.roda.core.storage.fedora.utils.FedoraConversionUtils;

/**
 * Class that implements {@code Iterable<Container>} which is useful for having
 * all the containers
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class IterableContainer implements CloseableIterable<Container> {
  private Iterator<FedoraResource> fedoraResources;

  public IterableContainer(FedoraRepository repository)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    try {
      fedoraResources = repository.getObject("").getChildren(null).iterator();
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error iterating though containers", e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Error iterating though containers", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Error iterating though containers", e);
    } catch (FedoraException e) {
      throw new GenericException("Error iterating though containers", e);
    }
  }

  @Override
  public Iterator<Container> iterator() {
    return new ContainerIterator(fedoraResources);
  }

  public class ContainerIterator implements Iterator<Container> {
    private Iterator<FedoraResource> fedoraResources;

    public ContainerIterator(Iterator<FedoraResource> fedoraResources) {
      this.fedoraResources = fedoraResources;
    }

    @Override
    public boolean hasNext() {
      if (fedoraResources == null) {
        return false;
      }
      return fedoraResources.hasNext();

    }

    @Override
    public Container next() {
      try {
        FedoraResource resource = fedoraResources.next();
        return FedoraConversionUtils.fedoraObjectToContainer((FedoraObject) resource);
      } catch (GenericException | RequestNotValidException e) {
        return null;
      }
    }

    @Override
    public void remove() {
    }

  }

  @Override
  public void close() throws IOException {
    // do nothing
  }
}
