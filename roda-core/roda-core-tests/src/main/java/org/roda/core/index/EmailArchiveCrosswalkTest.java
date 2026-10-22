/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class EmailArchiveCrosswalkTest {

  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws URISyntaxException, GenericException {
    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);
  }

  @BeforeMethod
  public void init() {
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false, false);
  }

  @AfterMethod
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
  }

  // ---------------------------------------------------------------------------
  // Full fixture — 3 emails
  // ---------------------------------------------------------------------------

  @Test
  public void testFullCrosswalkProducesParentFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);

    assertNotNull(doc);
    assertFieldValue(doc, "custodian_txt", "João Silva");
    assertFieldValue(doc, "emailAddress_s", "joao.silva@empresa.pt");
    assertFieldValue(doc, "totalMessages_i", "3");
    assertFieldValue(doc, "originalFormat_s", "PST");
    assertFieldValue(doc, "archivingMotive_txt", "Offboarding");
    assertFieldValue(doc, "content_type", "emailarchive");
  }

  @Test
  public void testFullCrosswalkProducesDateFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);

    assertNotNull(doc);
    assertFieldValue(doc, "dateStart_dt", "2020-01-01T00:00:00Z");
    assertFieldValue(doc, "dateEnd_dt", "2023-12-31T00:00:00Z");
  }

  @Test
  public void testFullCrosswalkProducesThreeChildDocuments() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);

    assertNotNull(doc);
    SolrInputField emailsField = doc.getField("emails");
    assertNotNull("'emails' field must be present for nested children", emailsField);

    Collection<SolrInputDocument> children = getChildDocuments(emailsField);
    assertEquals("Expected 3 child email documents", 3, children.size());
  }

  @Test
  public void testFullCrosswalkFirstChildFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);
    SolrInputDocument first = getChildAt(doc, 0);

    assertFieldValue(first, "content_type", "email");
    assertFieldValue(first, "messageId_s", "<msg001@empresa.pt>");
    assertFieldValue(first, "subject_txt", "Quarterly Report Q1 2021");
    assertFieldValue(first, "sender_s", "joao.silva@empresa.pt");
    assertFieldValue(first, "sentDate_dt", "2021-03-15T09:42:00Z");
    assertFieldValue(first, "folderPath_s", "Inbox/Projects");
    assertFieldValue(first, "hasAttachments_b", "true");
    assertFieldValue(first, "filePath_s", "Inbox/Projects/msg_001.eml");
  }

  @Test
  public void testFullCrosswalkMultipleRecipients() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);
    SolrInputDocument first = getChildAt(doc, 0);

    // First email has two recipients: ana.costa and rui.pinto
    SolrInputField recipientsField = first.getField("recipients_txt");
    assertNotNull("recipients_txt field must be present", recipientsField);
    Collection<?> values = recipientsField.getValues();
    assertNotNull(values);
    assertEquals("Expected 2 recipient values", 2, values.size());
  }

  @Test
  public void testFullCrosswalkThirdChildFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_FULL_FILE);
    SolrInputDocument third = getChildAt(doc, 2);

    assertFieldValue(third, "subject_txt", "Budget Approval Request");
    assertFieldValue(third, "folderPath_s", "Sent");
    assertFieldValue(third, "filePath_s", "Sent/msg_003.eml");
  }

  // ---------------------------------------------------------------------------
  // Minimal fixture — 1 email, only required fields
  // ---------------------------------------------------------------------------

  @Test
  public void testMinimalCrosswalkProducesRequiredFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_MINIMAL_FILE);

    assertNotNull(doc);
    assertFieldValue(doc, "custodian_txt", "Jane Doe");
    assertFieldValue(doc, "emailAddress_s", "jane.doe@example.org");
    assertFieldValue(doc, "content_type", "emailarchive");
  }

  @Test
  public void testMinimalCrosswalkOmitsAbsentOptionalFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_MINIMAL_FILE);

    assertNotNull(doc);
    assertNull("dateStart_dt should be absent when not in source", doc.getField("dateStart_dt"));
    assertNull("dateEnd_dt should be absent when not in source", doc.getField("dateEnd_dt"));
    assertNull("totalMessages_i should be absent when not in source", doc.getField("totalMessages_i"));
    assertNull("originalFormat_s should be absent when not in source", doc.getField("originalFormat_s"));
    assertNull("archivingMotive_txt should be absent when not in source", doc.getField("archivingMotive_txt"));
  }

  @Test
  public void testMinimalCrosswalkProducesOneChild() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_MINIMAL_FILE);
    SolrInputField emailsField = doc.getField("emails");
    assertNotNull(emailsField);
    assertEquals(1, getChildDocuments(emailsField).size());
  }

  @Test
  public void testMinimalCrosswalkChildHasRequiredFields() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_MINIMAL_FILE);
    SolrInputDocument child = getChildAt(doc, 0);

    assertFieldValue(child, "content_type", "email");
    assertFieldValue(child, "messageId_s", "<only-email@example.org>");
    assertFieldValue(child, "subject_txt", "Hello World");
    assertFieldValue(child, "hasAttachments_b", "false");
  }

  // ---------------------------------------------------------------------------
  // No-emails fixture — mailbox with zero email records
  // ---------------------------------------------------------------------------

  @Test
  public void testNoEmailsCrosswalkProducesParentFieldsOnly() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_NO_EMAILS_FILE);

    assertNotNull(doc);
    assertFieldValue(doc, "custodian_txt", "Empty Mailbox User");
    assertFieldValue(doc, "content_type", "emailarchive");
    assertFieldValue(doc, "totalMessages_i", "0");
  }

  @Test
  public void testNoEmailsCrosswalkProducesNoChildDocumentsField() throws RODAException {
    SolrInputDocument doc = getCrosswalkResult(CorporaConstants.EMAIL_ARCHIVE_NO_EMAILS_FILE);
    assertNull("'emails' field must be absent when there are no child emails", doc.getField("emails"));
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private SolrInputDocument getCrosswalkResult(String filename) throws RODAException {
    try {
      DefaultStoragePath path = DefaultStoragePath.parse(
        CorporaConstants.SOURCE_DESC_METADATA_CONTAINER, filename);
      Binary binary = corporaService.getBinary(path);
      return SolrUtils.getDescriptiveMetadataFields(binary, CorporaConstants.EMAIL_ARCHIVE_METADATA_TYPE, null);
    } catch (Exception e) {
      Assert.fail("Unexpected exception loading fixture '" + filename + "': " + e.getMessage());
      return null;
    }
  }

  private void assertFieldValue(SolrInputDocument doc, String fieldName, String expectedValue) {
    SolrInputField field = doc.getField(fieldName);
    assertNotNull("Field '" + fieldName + "' must be present", field);
    assertEquals("Field '" + fieldName + "' value mismatch", expectedValue, field.getValue().toString());
  }

  @SuppressWarnings("unchecked")
  private Collection<SolrInputDocument> getChildDocuments(SolrInputField emailsField) {
    Object value = emailsField.getValue();
    assertNotNull("'emails' field value must not be null", value);
    return (Collection<SolrInputDocument>) value;
  }

  private SolrInputDocument getChildAt(SolrInputDocument parent, int index) {
    SolrInputField emailsField = parent.getField("emails");
    assertNotNull(emailsField);
    Collection<SolrInputDocument> children = getChildDocuments(emailsField);
    return children.stream().skip(index).findFirst()
      .orElseThrow(() -> new AssertionError("No child document at index " + index));
  }
}
