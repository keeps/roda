package org.roda.core.model.lites;

import java.io.Serial;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public abstract class ParsedLite extends LiteRODAObject {

  @Serial
  private static final long serialVersionUID = -2124850162562270129L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ParsedLite.class);

  protected ParsedLite(String value) {
    super(value);
  }

  public static OptionalWithCause<ParsedLite> parse(LiteRODAObject liteRODAObject) {
    ParsedLite ret = null;
    String[] split = liteRODAObject.getInfo().split(LiteRODAObjectFactory.SEPARATOR_REGEX);
    try {
      if (split.length >= 2) {
        String clazz = split[0];
        if (AIP.class.getName().equals(clazz) || IndexedAIP.class.getName().equals(clazz)) {
          ret = new ParsedAIPLite(liteRODAObject, split);
        } else if (DescriptiveMetadata.class.getName().equals(clazz)) {
          ret = new ParsedDescriptiveMetadataLite(liteRODAObject, split);
        } else if (DIP.class.getName().equals(clazz) || IndexedDIP.class.getName().equals(clazz)) {
          ret = new ParsedDIPLite(liteRODAObject, split);
        } else if (DIPFile.class.getName().equals(clazz)) {
          ret = new ParsedDIPFileLite(liteRODAObject, split);
        } else if (File.class.getName().equals(clazz) || IndexedFile.class.getName().equals(clazz)) {
          ret = new ParsedFileLite(liteRODAObject, split);
        } else if (RepresentationInformation.class.getName().equals(clazz)) {
          ret = new ParsedRepresentationInformationLite(liteRODAObject, split);
        } else if (Job.class.getName().equals(clazz)) {
          ret = new ParsedJobLite(liteRODAObject, split);
        } else if (Notification.class.getName().equals(clazz)) {
          ret = new ParsedNotificationLite(liteRODAObject, split);
        } else if (PreservationMetadata.class.getName().equals(clazz)) {
          ret = new ParsedPreservationMetadataLite(liteRODAObject, split);
        } else if (Report.class.getName().equals(clazz) || IndexedReport.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = new ParsedReportLite(liteRODAObject, split);
          }
        } else if (Risk.class.getName().equals(clazz) || IndexedRisk.class.getName().equals(clazz)) {
          ret = new ParsedRiskLite(liteRODAObject, split);
        } else if (RiskIncidence.class.getName().equals(clazz)) {
          ret = new ParsedRiskIncidenceLite(liteRODAObject, split);
        } else if (Representation.class.getName().equals(clazz)
          || IndexedRepresentation.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = new ParsedRepresentationLite(liteRODAObject, split);
          }
        } else if (TransferredResource.class.getName().equals(clazz)) {
          ret = new ParsedTransferredResourceLite(liteRODAObject, split);
        } else if (User.class.getName().equals(clazz)) {
          ret = new ParsedUserLite(liteRODAObject, split);
        } else if (Group.class.getName().equals(clazz)) {
          ret = new ParsedGroupLite(liteRODAObject, split);
        } else if (DisposalConfirmation.class.getName().equals(clazz)) {
          ret = new ParsedDisposalConfirmationLite(liteRODAObject, split);
        } else if (DisposalHold.class.getName().equals(clazz)) {
          ret = new ParsedDisposalHoldLite(liteRODAObject, split);
        } else if (DisposalSchedule.class.getName().equals(clazz)) {
          ret = new ParsedDisposalSchedule(liteRODAObject, split);
        } else if (DisposalRule.class.getName().equals(clazz)) {
          ret = new ParsedDisposalRule(liteRODAObject, split);
        } else if (DistributedInstance.class.getName().equals(clazz)) {
          ret = new ParsedDistributedInstance(liteRODAObject, split);
        }
      }
    } catch (GenericException e) {
      LOGGER.error("Unable to parse lite {}", liteRODAObject, e);
      return OptionalWithCause.empty(e);
    }
    return OptionalWithCause.of(ret);
  }

  public abstract IsRODAObject toRODAObject(ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;
}
