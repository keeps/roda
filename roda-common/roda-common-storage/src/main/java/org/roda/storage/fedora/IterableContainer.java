/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.storage.fedora;

import java.io.IOException;
import java.util.Iterator;

import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.roda.storage.ClosableIterable;
import org.roda.storage.Container;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fedora.utils.FedoraConversionUtils;

/**
 * Class that implements {@code Iterable<Container>} which is useful for having
 * all the containers
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class IterableContainer implements ClosableIterable<Container> {
  private Iterator<FedoraResource> fedoraResources;

  public IterableContainer(FedoraRepository repository) throws StorageServiceException {
    try {
      fedoraResources = repository.getObject("").getChildren(null).iterator();
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
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
      } catch (StorageServiceException e) {
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
