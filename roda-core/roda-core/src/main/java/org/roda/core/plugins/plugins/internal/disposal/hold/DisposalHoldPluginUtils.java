package org.roda.core.plugins.plugins.internal.disposal.hold;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;

import java.util.Date;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldPluginUtils {

  public static DisposalHoldAssociation createDisposalHoldAssociation(String disposalHoldId, String associatedBy) {
    DisposalHoldAssociation disposalHoldAssociation = new DisposalHoldAssociation();
    disposalHoldAssociation.setAssociatedOn(new Date());
    disposalHoldAssociation.setAssociatedBy(associatedBy);
    disposalHoldAssociation.setId(disposalHoldId);

    return disposalHoldAssociation;
  }

  public static void liftDisposalHoldFromAIP(AIP aip, String liftedBy) {


  }
}
