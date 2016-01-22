/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileVisitorChecker implements FileVisitor<Path> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileVisitorChecker.class);

  private IndexService index;
  private Path basePath;
  private boolean ok;

  public FileVisitorChecker(Path basePath, IndexService index) {
    this.index = index;
    this.ok = true;
    this.basePath = basePath;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if (basePath.relativize(dir).getNameCount() > 1) {
      try {
        index.retrieve(TransferredResource.class, basePath.relativize(dir).toString());
      } catch (NotFoundException | GenericException e) {
        LOGGER.error("Error", e);
        this.ok = false;
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (basePath.relativize(file).getNameCount() > 1) {
      try {
        index.retrieve(TransferredResource.class, basePath.relativize(file).toString());
      } catch (NotFoundException | GenericException e) {
        LOGGER.error("Error", e);
        this.ok = false;
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  public boolean isOk() {
    return ok;
  }

}