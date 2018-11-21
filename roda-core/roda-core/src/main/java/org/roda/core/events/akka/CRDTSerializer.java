/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.events.akka;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import akka.serialization.SerializerWithStringManifest;

public class CRDTSerializer extends SerializerWithStringManifest {

  private static final String WRAPPER_MANIFEST = "wrapper";

  @Override
  public int identifier() {
    return 123456789;
  }

  @Override
  public String manifest(Object obj) {
    if (obj instanceof CRDTWrapper) {
      return WRAPPER_MANIFEST;
    } else {
      throw new IllegalArgumentException("Unknown type: " + obj);
    }
  }

  @Override
  public Object fromBinary(byte[] obj, String manifest) throws NotSerializableException {
    if (WRAPPER_MANIFEST.equals(manifest)) {
      try (ByteArrayInputStream bis = new ByteArrayInputStream(obj);) {
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    } else {
      throw new IllegalArgumentException("Unknown manifest: " + manifest);
    }
  }

  @Override
  public byte[] toBinary(Object obj) {
    if (obj instanceof CRDTWrapper) {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.flush();
        return bos.toByteArray();
      } catch (IOException e) {
        throw new IllegalArgumentException("Unknown type: " + obj);
      }
    } else {
      throw new IllegalArgumentException("Unknown type: " + obj);
    }
  }

}
