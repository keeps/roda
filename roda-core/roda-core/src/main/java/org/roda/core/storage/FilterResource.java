package org.roda.core.storage;

@FunctionalInterface
public interface FilterResource {

  boolean accept(Resource resource);

}
