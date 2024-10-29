package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateFileBackfillPlugin extends GenerateRODAEntityBackfillPlugin<File> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) IndexedFile.class;
  }

  @Override
  protected String getObjectId(File object) {
    return IdUtils.getFileId(object);
  }

  @Override
  public Plugin<File> cloneMe() {
    return new GenerateFileBackfillPlugin();
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return List.of(File.class);
  }
}
