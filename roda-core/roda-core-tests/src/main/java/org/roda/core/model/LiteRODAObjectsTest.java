package org.roda.core.model;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Optional;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.testng.annotations.Test;

public class LiteRODAObjectsTest {

  @Test
  public void createLites() {
    // 20170110 hsilva: ids have the LiteRODAObjectFactory separator in order to
    // test the encoding/decoding
    String id1 = "01|23";
    String id2 = "45|67";
    String id3 = "89|ab";

    // AIP
    AIP aip = new AIP();
    aip.setId(id1);
    Optional<LiteRODAObject> lite = LiteRODAObjectFactory.get(aip);
    assertTrue(lite.isPresent());

    if (lite.isPresent()) {
      assertEquals(getExpected(AIP.class, id1), lite.get().getInfo());
    }

    // DIP
    DIP dip = new DIP();
    dip.setId(id1);
    lite = LiteRODAObjectFactory.get(dip);
    assertTrue(lite.isPresent());

    if (lite.isPresent()) {
      assertEquals(getExpected(DIP.class, id1), lite.get().getInfo());
    }

    // Format
    Format format = new Format();
    format.setId(id1);
    lite = LiteRODAObjectFactory.get(format);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Format.class, id1), lite.get().getInfo());

    // Job
    Job job = new Job();
    job.setId(id1);
    lite = LiteRODAObjectFactory.get(job);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Job.class, id1), lite.get().getInfo());

    // Report
    Report report = new Report();
    report.setJobId(id1);
    report.setId(id2);
    lite = LiteRODAObjectFactory.get(report);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Report.class, id1, id2), lite.get().getInfo());

    // Notification
    Notification notification = new Notification();
    notification.setId(id1);
    lite = LiteRODAObjectFactory.get(notification);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Notification.class, id1), lite.get().getInfo());

    // Risk
    Risk risk = new Risk();
    risk.setId(id1);
    lite = LiteRODAObjectFactory.get(risk);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Risk.class, id1), lite.get().getInfo());

    // RiskIncidence
    RiskIncidence riskIncidence = new RiskIncidence();
    riskIncidence.setId(id1);
    lite = LiteRODAObjectFactory.get(riskIncidence);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(RiskIncidence.class, id1), lite.get().getInfo());

    // Representation
    Representation representation = new Representation();
    representation.setAipId(id1);
    representation.setId(id2);
    lite = LiteRODAObjectFactory.get(representation);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Representation.class, id1, id2), lite.get().getInfo());

    // TransferredResource
    TransferredResource transferredResource = new TransferredResource();
    transferredResource.setFullPath(id1);
    lite = LiteRODAObjectFactory.get(transferredResource);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(TransferredResource.class, id1), lite.get().getInfo());

    // DescriptiveMetadata - aip level
    DescriptiveMetadata descriptiveMetadataAip = new DescriptiveMetadata();
    descriptiveMetadataAip.setAipId(id1);
    descriptiveMetadataAip.setId(id2);
    lite = LiteRODAObjectFactory.get(descriptiveMetadataAip);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(DescriptiveMetadata.class, id1, id2), lite.get().getInfo());

    // DescriptiveMetadata - representation level
    DescriptiveMetadata descriptiveMetadataRep = new DescriptiveMetadata();
    descriptiveMetadataRep.setAipId(id1);
    descriptiveMetadataRep.setRepresentationId(id2);
    descriptiveMetadataRep.setId(id3);
    lite = LiteRODAObjectFactory.get(descriptiveMetadataRep);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(DescriptiveMetadata.class, id1, id2, id3), lite.get().getInfo());

    // LogEntry
    LogEntry logEntry = new LogEntry();
    logEntry.setId(id1);
    lite = LiteRODAObjectFactory.get(logEntry);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(LogEntry.class, id1), lite.get().getInfo());

    // RODAMember - user
    User user = new User();
    user.setName(id1);
    lite = LiteRODAObjectFactory.get(user);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(User.class, id1), lite.get().getInfo());

    // RODAMember - group
    Group group = new Group();
    group.setName(id1);
    lite = LiteRODAObjectFactory.get(group);
    assertTrue(lite.isPresent());
    assertEquals(getExpected(Group.class, id1), lite.get().getInfo());

  }

  private <T extends IsRODAObject> String getExpected(Class<T> objectClass, String... ids) {
    StringBuilder sb = new StringBuilder();
    sb.append(objectClass.getName());
    for (String id : ids) {
      sb.append(LiteRODAObjectFactory.SEPARATOR)
        .append(id.replaceAll(LiteRODAObjectFactory.SEPARATOR_REGEX, LiteRODAObjectFactory.SEPARATOR_URL_ENCODED));
    }

    return sb.toString();
  }
}
