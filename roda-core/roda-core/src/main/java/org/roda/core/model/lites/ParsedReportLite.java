package org.roda.core.model.lites;

import java.io.Serial;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ParsedReportLite extends ParsedLite {

  @Serial
  private static final long serialVersionUID = -6478186989265099271L;

  private final String id;
  private final String jobId;

  public ParsedReportLite(LiteRODAObject aipLite, String[] split) throws GenericException {
    super(aipLite.getInfo());
    id = LiteRODAObjectFactory.decodeId(split[2]);
    jobId = LiteRODAObjectFactory.decodeId(split[1]);
  }

  public String getId() {
    return id;
  }

  public String getJobId() {
    return jobId;
  }

  @Override
  public IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveJobReport(jobId, id);
  }
}
