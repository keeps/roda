package org.roda.core.model.utils;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceListUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceListUtils.class);

  public static CloseableIterable<Resource> listFileResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> aipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIterator = aipResources.iterator();

    return new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResources.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> repResources = null;
          Iterator<Resource> repIterator = null;
          CloseableIterable<Resource> fileResources = null;
          Iterator<Resource> fileIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (repResources == null) {
              while (aipIterator.hasNext()) {
                try {
                  StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                  if (storage.hasDirectory(repPath)) {
                    repResources = storage.listResourcesUnderDirectory(repPath, false);
                    repIterator = repResources.iterator();

                    while (repIterator.hasNext()) {
                      StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                        RodaConstants.STORAGE_DIRECTORY_DATA);

                      if (storage.hasDirectory(filePath)) {
                        fileResources = storage.listResourcesUnderDirectory(filePath, true);
                        fileIterator = fileResources.iterator();
                        if (fileIterator.hasNext()) {
                          nextResource = fileIterator.next();
                          break;
                        } else {
                          IOUtils.closeQuietly(fileResources);
                        }
                      }
                    }

                    if (nextResource == null) {
                      IOUtils.closeQuietly(repResources);
                    } else {
                      break;
                    }
                  }
                } catch (RODAException e) {
                  return false;
                }
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (fileIterator.hasNext()) {
              nextResource = fileIterator.next();
            } else {
              while (repIterator.hasNext()) {
                try {
                  StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_DATA);

                  if (storage.hasDirectory(filePath)) {
                    IOUtils.closeQuietly(fileResources);
                    fileResources = storage.listResourcesUnderDirectory(filePath, true);
                    fileIterator = fileResources.iterator();
                    if (fileIterator.hasNext()) {
                      break;
                    } else {
                      IOUtils.closeQuietly(fileResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under representation data directory", e);
                }
              }

              if (fileIterator != null && fileIterator.hasNext()) {
                nextResource = fileIterator.next();
              } else {
                outerloop: while (aipIterator.hasNext()) {
                  try {
                    StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                      RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                    if (storage.hasDirectory(repPath)) {
                      IOUtils.closeQuietly(repResources);
                      repResources = storage.listResourcesUnderDirectory(repPath, false);
                      repIterator = repResources.iterator();
                      while (repIterator.hasNext()) {
                        StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                          RodaConstants.STORAGE_DIRECTORY_DATA);

                        if (storage.hasDirectory(filePath)) {
                          IOUtils.closeQuietly(fileResources);
                          fileResources = storage.listResourcesUnderDirectory(filePath, true);
                          fileIterator = fileResources.iterator();
                          if (fileIterator.hasNext()) {
                            break outerloop;
                          } else {
                            IOUtils.closeQuietly(fileResources);
                          }
                        }
                      }
                    }
                  } catch (RODAException e) {
                    LOGGER.error("Could not list resources under AIP", e);
                  }
                }

                if (fileIterator != null && fileIterator.hasNext()) {
                  nextResource = fileIterator.next();
                }
              }
            }

            return ret;
          }
        };
      }
    };
  }

  public static CloseableIterable<Resource> listRepresentationResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> aipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIterator = aipResources.iterator();

    return new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResources.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> repResources = null;
          Iterator<Resource> repIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (repResources == null) {
              try {
                while (aipIterator.hasNext()) {
                  StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                  if (storage.hasDirectory(repPath)) {
                    repResources = storage.listResourcesUnderDirectory(repPath, false);
                    repIterator = repResources.iterator();

                    if (repIterator.hasNext()) {
                      nextResource = repIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(repResources);
                    }
                  }
                }
              } catch (RODAException e) {
                return false;
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (repIterator.hasNext()) {
              nextResource = repIterator.next();
            } else {
              while (aipIterator.hasNext()) {
                try {
                  StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                  if (storage.hasDirectory(repPath)) {
                    repResources = storage.listResourcesUnderDirectory(repPath, false);
                    repIterator = repResources.iterator();

                    if (repIterator.hasNext()) {
                      nextResource = repIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(repResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under AIP data directory", e);
                }
              }
            }

            return ret;
          }
        };
      }
    };
  }

  public static CloseableIterable<Resource> listDIPFileResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> dipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(DIP.class), false);
    final Iterator<Resource> dipIterator = dipResources.iterator();

    return new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        dipResources.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> fileResources = null;
          Iterator<Resource> fileIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (fileResources == null) {
              while (dipIterator.hasNext()) {
                try {
                  StoragePath dataPath = DefaultStoragePath.parse(dipIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_DATA);

                  if (storage.hasDirectory(dataPath)) {
                    fileResources = storage.listResourcesUnderDirectory(dataPath, true);
                    fileIterator = fileResources.iterator();

                    if (fileIterator.hasNext()) {
                      nextResource = fileIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(fileResources);
                    }
                  }
                } catch (RODAException e) {
                  return false;
                }
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (fileIterator.hasNext()) {
              nextResource = fileIterator.next();
            } else {
              while (dipIterator.hasNext()) {
                try {
                  StoragePath dataPath = DefaultStoragePath.parse(dipIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_DATA);

                  if (storage.hasDirectory(dataPath)) {
                    fileResources = storage.listResourcesUnderDirectory(dataPath, true);
                    fileIterator = fileResources.iterator();

                    if (fileIterator.hasNext()) {
                      nextResource = fileIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(fileResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under DIP data directory", e);
                }
              }
            }

            return ret;
          }
        };
      }
    };
  }

  public static CloseableIterable<Resource> listPreservationMetadataResources(StorageService storage)
    throws RODAException {
    final CloseableIterable<Resource> aipResourcesTop = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorTop = aipResourcesTop.iterator();

    CloseableIterable<Resource> aipMetadata = new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResourcesTop.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> metadataResources = null;
          Iterator<Resource> metadataIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (metadataResources == null) {
              try {
                while (aipIteratorTop.hasNext()) {
                  StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

                  if (storage.hasDirectory(metadataPath)) {
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                    metadataIterator = metadataResources.iterator();

                    if (metadataIterator.hasNext()) {
                      nextResource = metadataIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                }
              } catch (RODAException e) {
                return false;
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (metadataIterator.hasNext()) {
              nextResource = metadataIterator.next();
            } else {
              while (aipIteratorTop.hasNext()) {
                try {
                  StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

                  if (storage.hasDirectory(metadataPath)) {
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                    metadataIterator = metadataResources.iterator();

                    if (metadataIterator.hasNext()) {
                      nextResource = metadataIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under AIP data directory", e);
                }
              }
            }

            return ret;
          }
        };
      }
    };

    final CloseableIterable<Resource> aipResourcesSub = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorSub = aipResourcesSub.iterator();

    CloseableIterable<Resource> repMetadata = new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResourcesSub.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> repResources = null;
          Iterator<Resource> repIterator = null;
          CloseableIterable<Resource> metadataResources = null;
          Iterator<Resource> metadataIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (repResources == null) {
              try {
                while (aipIteratorSub.hasNext()) {
                  StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                  if (storage.hasDirectory(repPath)) {
                    repResources = storage.listResourcesUnderDirectory(repPath, false);
                    repIterator = repResources.iterator();

                    while (repIterator.hasNext()) {
                      StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                        RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

                      if (storage.hasDirectory(metadataPath)) {
                        metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                        metadataIterator = metadataResources.iterator();
                        if (metadataIterator.hasNext()) {
                          nextResource = metadataIterator.next();
                          break;
                        } else {
                          IOUtils.closeQuietly(metadataResources);
                        }
                      }
                    }

                    if (nextResource == null) {
                      IOUtils.closeQuietly(repResources);
                    } else {
                      break;
                    }
                  }
                }
              } catch (RODAException e) {
                return false;
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (metadataIterator.hasNext()) {
              nextResource = metadataIterator.next();
            } else {
              while (repIterator.hasNext()) {
                try {
                  StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

                  if (storage.hasDirectory(metadataPath)) {
                    IOUtils.closeQuietly(metadataResources);
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                    metadataIterator = metadataResources.iterator();
                    if (metadataIterator.hasNext()) {
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under representation data directory", e);
                }
              }

              if (metadataIterator.hasNext()) {
                nextResource = metadataIterator.next();
              } else {
                outerloop: while (aipIteratorSub.hasNext()) {
                  try {
                    StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                      RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                    if (storage.hasDirectory(repPath)) {
                      IOUtils.closeQuietly(repResources);
                      repResources = storage.listResourcesUnderDirectory(repPath, false);
                      repIterator = repResources.iterator();
                      while (repIterator.hasNext()) {
                        StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                          RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

                        if (storage.hasDirectory(metadataPath)) {
                          IOUtils.closeQuietly(metadataResources);
                          metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                          metadataIterator = metadataResources.iterator();
                          if (metadataIterator.hasNext()) {
                            break outerloop;
                          } else {
                            IOUtils.closeQuietly(metadataResources);
                          }
                        }
                      }
                    }
                  } catch (RODAException e) {
                    LOGGER.error("Could not list resources under AIP", e);
                  }
                }

                if (metadataIterator != null && metadataIterator.hasNext()) {
                  nextResource = metadataIterator.next();
                }
              }
            }

            return ret;
          }
        };
      }
    };

    DefaultStoragePath agentPath = DefaultStoragePath.parse(RodaConstants.STORAGE_DIRECTORY_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS);
    CloseableIterable<Resource> agentMetadata = storage.listResourcesUnderDirectory(agentPath, false);

    return CloseableIterables.concat(aipMetadata, repMetadata, agentMetadata);
  }

  public static CloseableIterable<Resource> listDescriptiveMetadataResources(StorageService storage)
    throws RODAException {
    final CloseableIterable<Resource> aipResourcesTop = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorTop = aipResourcesTop.iterator();

    CloseableIterable<Resource> aipMetadata = new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResourcesTop.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> metadataResources = null;
          Iterator<Resource> metadataIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (metadataResources == null) {
              try {
                while (aipIteratorTop.hasNext()) {
                  StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                  if (storage.hasDirectory(metadataPath)) {
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                    metadataIterator = metadataResources.iterator();

                    if (metadataIterator.hasNext()) {
                      nextResource = metadataIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                }
              } catch (RODAException e) {
                return false;
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (metadataIterator.hasNext()) {
              nextResource = metadataIterator.next();
            } else {
              while (aipIteratorTop.hasNext()) {
                try {
                  StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                  if (storage.hasDirectory(metadataPath)) {
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                    metadataIterator = metadataResources.iterator();

                    if (metadataIterator.hasNext()) {
                      nextResource = metadataIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under AIP data directory", e);
                }
              }
            }

            return ret;
          }
        };
      }
    };

    final CloseableIterable<Resource> aipResourcesSub = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorSub = aipResourcesSub.iterator();

    CloseableIterable<Resource> repMetadata = new CloseableIterable<Resource>() {

      @Override
      public void close() throws IOException {
        aipResourcesSub.close();
      }

      @Override
      public Iterator<Resource> iterator() {

        return new Iterator<Resource>() {
          CloseableIterable<Resource> repResources = null;
          Iterator<Resource> repIterator = null;
          CloseableIterable<Resource> metadataResources = null;
          Iterator<Resource> metadataIterator = null;
          Resource nextResource = null;

          @Override
          public boolean hasNext() {
            if (repResources == null) {
              try {
                while (aipIteratorSub.hasNext()) {
                  StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                  if (storage.hasDirectory(repPath)) {
                    repResources = storage.listResourcesUnderDirectory(repPath, false);
                    repIterator = repResources.iterator();

                    while (repIterator.hasNext()) {
                      StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                        RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                      if (storage.hasDirectory(metadataPath)) {
                        metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                        metadataIterator = metadataResources.iterator();
                        if (metadataIterator.hasNext()) {
                          nextResource = metadataIterator.next();
                          break;
                        } else {
                          IOUtils.closeQuietly(metadataResources);
                        }
                      }
                    }

                    if (nextResource == null) {
                      IOUtils.closeQuietly(repResources);
                    } else {
                      break;
                    }
                  }
                }
              } catch (RODAException e) {
                return false;
              }
            }

            return nextResource != null;
          }

          @Override
          public Resource next() {
            Resource ret = nextResource;
            nextResource = null;

            if (metadataIterator.hasNext()) {
              nextResource = metadataIterator.next();
            } else {
              while (repIterator.hasNext()) {
                try {
                  StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                  if (storage.hasDirectory(metadataPath)) {
                    IOUtils.closeQuietly(metadataResources);
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                    metadataIterator = metadataResources.iterator();
                    if (metadataIterator.hasNext()) {
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                } catch (RODAException e) {
                  LOGGER.error("Could not list resources under representation data directory", e);
                }
              }

              if (metadataIterator != null && metadataIterator.hasNext()) {
                nextResource = metadataIterator.next();
              } else {
                outerloop: while (aipIteratorSub.hasNext()) {
                  try {
                    StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                      RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                    if (storage.hasDirectory(repPath)) {
                      IOUtils.closeQuietly(repResources);
                      repResources = storage.listResourcesUnderDirectory(repPath, false);
                      repIterator = repResources.iterator();
                      while (repIterator.hasNext()) {
                        StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                          RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                        if (storage.hasDirectory(metadataPath)) {
                          IOUtils.closeQuietly(metadataResources);
                          metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                          metadataIterator = metadataResources.iterator();
                          if (metadataIterator.hasNext()) {
                            break outerloop;
                          } else {
                            IOUtils.closeQuietly(metadataResources);
                          }
                        }
                      }
                    }
                  } catch (RODAException e) {
                    LOGGER.error("Could not list resources under AIP", e);
                  }
                }

                if (metadataIterator != null && metadataIterator.hasNext()) {
                  nextResource = metadataIterator.next();
                }
              }
            }

            return ret;
          }
        };
      }
    };

    return CloseableIterables.concat(aipMetadata, repMetadata);
  }

}
