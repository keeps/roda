package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateDIPFileBackfillPlugin extends GenerateRODAEntityBackfillPlugin<DIPFile> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) DIPFile.class;
  }

  @Override
  protected String getObjectId(DIPFile object) {
    return IdUtils.getDIPFileId(object);
  }

  @Override
  public Plugin<DIPFile> cloneMe() {
    return new GenerateDIPFileBackfillPlugin();
  }

  @Override
  public List<Class<DIPFile>> getObjectClasses() {
    return List.of(DIPFile.class);
  }
}
