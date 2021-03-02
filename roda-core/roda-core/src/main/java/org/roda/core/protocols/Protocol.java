package org.roda.core.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginManager;

/**
 * This interface should be implemented by any class that want to be a RODA
 * protocol.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface Protocol extends IsRODAObject {

  /**
   * Initializes this {@link Protocol}. This method is called by the
   * {@link ProtocolManager} before any other methods in the protocol.
   * 
   * @throws ProtocolException
   */
  void init() throws ProtocolException;

  /**
   * Returns the name of this {@link Protocol}.
   * 
   * @return a {@link String} with the name of this {@link Protocol}.
   */
  String getName();

  /**
   * Returns the version of this {@link Protocol}.
   *
   * @return a <code>String</code> with the version number for this
   *         {@link Protocol}.
   */
  String getVersion();

  /**
   * Returns description of this {@link Protocol}.
   *
   * @return a {@link String} with the description of this {@link Protocol}.
   */
  String getDescription();


  /**
   * Method used by ProtocolManager to obtain a new instance of a protocol, from the
   * current loaded Protocol
   */
  Protocol cloneMe(URI uri);

  /**
   * Get the URI scheme
   * @return the URI scheme
   */
  String getSchema();

  /**
   * This method defines how to access the remote resource
   *
   * @return an InputStream of the resource to be consumed by the RODA.
   * @throws IOException
   */
  InputStream getInputStream() throws IOException;

  /**
   * checks if a resource is available, used at the time of ingestion and when accessing
   *
   * @return Boolean
   */
  Boolean isAvailable();

  /**
   * Obtained the total size in bytes of the resource
   * @return Long
   * @throws IOException
   */
  Long getSize() throws IOException;

  /**
   * Performs the download of the resource to a certain path
   *
   * @param target
   * @throws IOException
   */
  void downloadResource(Path target) throws IOException;

  /**
   * Stops all {@link Protocol} activity. This is the last method to be called by
   * {@link ProtocolManager} on the {@link Protocol}.
   */
  void shutdown();
}
