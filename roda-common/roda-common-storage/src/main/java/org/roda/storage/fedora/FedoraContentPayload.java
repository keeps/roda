package org.roda.storage.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.roda.storage.ContentPayload;

public class FedoraContentPayload implements ContentPayload {

	private final FedoraDatastream fds;

	public FedoraContentPayload(FedoraDatastream fds) {
		this.fds = fds;
	}

	@Override
	public InputStream createInputStream() throws IOException {
		try {
			return fds.getContent();
		} catch (FedoraException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeToPath(Path path) throws IOException {
		Files.copy(createInputStream(), path,
				StandardCopyOption.REPLACE_EXISTING);

	}

	@Override
	public URI getURI() throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"URI not supported for Fedora Datastreams");
	}

}
