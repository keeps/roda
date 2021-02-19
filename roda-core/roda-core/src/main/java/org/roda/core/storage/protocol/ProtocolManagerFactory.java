package org.roda.core.storage.protocol;

import org.roda.core.data.exceptions.GenericException;

import java.net.URI;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ProtocolManagerFactory {
  public static ProtocolManager createProtocolManager(URI connectionString) throws GenericException {
    String scheme = connectionString.getScheme();
    ProtocolManager protocolManager;
    if(scheme.equals("https") || scheme.equals("http")) {
      protocolManager = new HTTPProtocolManager(connectionString);
    } else if (scheme.equals("nfs")){
      protocolManager = new NFSProtocolManager(connectionString);
    } else if (scheme.equals("file")){
      protocolManager = new FileProtocolManager(connectionString);
    } else {
      throw new GenericException("Protocol " + scheme + "not implemented");
    }
    return protocolManager;
  }
}
