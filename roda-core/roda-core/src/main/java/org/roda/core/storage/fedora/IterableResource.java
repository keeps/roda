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
import org.fcrepo.client.FedoraDatastream;
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
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.fedora.utils.FedoraConversionUtils;
import org.roda.core.storage.fedora.utils.FedoraUtils;

/**
 * Class that implements {@code Iterable<Resource>} for a particular storage
 * path
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class IterableResource implements CloseableIterable<Resource> {
  private FedoraRepository repository;
  private Iterator<FedoraResource> fedoraResources;

  public IterableResource(FedoraRepository repository, StoragePath storagePath)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    this.repository = repository;
    try {
      fedoraResources = repository.getObject(FedoraUtils.storagePathToFedoraPath(storagePath)).getChildren(null).iterator();
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Could not iterate through resource", e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Could not iterate through resource", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Could not iterate through resource", e);
    } catch (FedoraException e) {
      throw new GenericException("Could not iterate through resource", e);
    }
  }

  @Override
  public Iterator<Resource> iterator() {
    return new ResourceIterator(repository, fedoraResources);
  }

  public class ResourceIterator implements Iterator<Resource> {
    private Iterator<FedoraResource> fedoraResources;

    public ResourceIterator(FedoraRepository repository, Iterator<FedoraResource> fedoraResources) {
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
    public Resource next() {
      try {
        FedoraResource resource = fedoraResources.next();
        if (resource instanceof FedoraDatastream) {
          return FedoraConversionUtils.fedoraDatastreamToBinary((FedoraDatastream) resource);
        } else {
          return FedoraConversionUtils.fedoraObjectToDirectory(repository.getRepositoryUrl(), (FedoraObject) resource);
        }
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
