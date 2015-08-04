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
import org.roda.storage.StorageActionException;
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

	public IterableContainer(FedoraRepository repository)
			throws StorageActionException {
		try {
			fedoraResources = repository.getObject("").getChildren(null)
					.iterator();
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
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
				return FedoraConversionUtils
						.fedoraObjectToContainer((FedoraObject) resource);
			} catch (StorageActionException e) {
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
