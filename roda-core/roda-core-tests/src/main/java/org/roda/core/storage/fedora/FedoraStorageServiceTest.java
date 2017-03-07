/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import java.io.IOException;
import java.nio.file.Paths;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.storage.fs.FSUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

// @RunWith(SpringJUnit4ClassRunner.class)
//@Test(groups = {"travis"})
@Test(enabled=false)
@ContextConfiguration("/fcrepo/spring-test/test-container.xml")
public class FedoraStorageServiceTest extends AbstractTestNGSpringContextTests {

  private FedoraStorageServiceTestDelegate delegate = new FedoraStorageServiceTestDelegate();

  @AfterClass
  public static void tearDown() throws NotFoundException, GenericException {
    FSUtils.deletePath(Paths.get("fcrepo4-data"));
  }

  @Test
  public void testClassInstantiation() throws RODAException {
    delegate.testClassInstantiation();
  }

  @Test
  public void testListContainer() throws RODAException {
    delegate.testListContainer();
  }

  @Test
  public void testCreateGetDeleteContainer() throws RODAException {
    delegate.testCreateGetDeleteContainer();
  }

  @Test
  public void testGetContainerThatDoesntExist() throws RODAException {
    delegate.testGetContainerThatDoesntExist();
  }

  @Test
  public void testGetContainerThatIsActuallyADirectory() throws RODAException {
    delegate.testGetContainerThatIsActuallyADirectory();
  }

  @Test
  public void testGetContainerThatIsActuallyABinary() throws RODAException {
    delegate.testGetContainerThatIsActuallyABinary();
  }

  @Test
  public void testDeleteContainerThatDoesntExist() throws RODAException {
    delegate.testDeleteContainerThatDoesntExist();
  }

  @Test
  public void testDeleteNonEmptyContaienr() throws RODAException {
    delegate.testDeleteNonEmptyContainer();
  }

  @Test
  public void testListResourcesUnderContainer() throws RODAException {
    delegate.testListResourcesUnderContainer();
  }

  @Test
  public void testCreateGetDeleteDirectory() throws RODAException {
    delegate.testCreateGetDeleteDirectory();
  }

  @Test
  public void testGetDirectoryThatDoesntExist() throws RODAException {
    delegate.testGetDirectoryThatDoesntExist();
  }

  @Test
  public void testGetDirectoryThatIsActuallyABinary() throws RODAException {
    delegate.testGetDirectoryThatIsActuallyABinary();
  }

  @Test
  public void testGetDirectoryThatIsActuallyAContainer() throws RODAException {
    delegate.testGetDirectoryThatIsActuallyAContainer();
  }

  @Test
  public void testListResourcesUnderDirectory() throws RODAException, IOException {
    delegate.testListResourcesUnderDirectory();
  }

  @Test
  public void testCreateGetDeleteBinary() throws RODAException, IOException {
    delegate.testCreateGetDeleteBinary();
  }

  @Test
  public void testCreateGetDeleteBinaryAsReference() throws RODAException, IOException {
    delegate.testCreateGetDeleteBinaryAsReference();
  }

  @Test
  public void testUpdateBinaryContent() throws RODAException, IOException {
    delegate.testUpdateBinaryContent();
  }

  @Test
  public void testUpdateBinaryThatDoesntExist() throws RODAException, IOException {
    delegate.testUpdateBinaryThatDoesntExist();
  }

  @Test
  public void testGetBinaryThatDoesntExist() throws RODAException {
    delegate.testGetBinaryThatDoesntExist();
  }

  @Test
  public void testGetBinaryThatIsActuallyADirectory() throws RODAException {
    delegate.testGetBinaryThatIsActuallyADirectory();
  }

  @Test
  public void testGetBinaryThatIsActuallyAContainer() throws RODAException {
    delegate.testGetBinaryThatIsActuallyAContainer();
  }

  @Test
  public void testDeleteNonEmptyDirectory() throws RODAException {
    delegate.testDeleteNonEmptyDirectory();
  }

  @Test
  public void testCopyContainerToSameStorage() throws RODAException, IOException {
    delegate.testCopyContainerToSameStorage();
  }

  @Test
  public void testCopyDirectoryToSameStorage() throws RODAException, IOException {
    delegate.testCopyDirectoryToSameStorage();
  }

  @Test
  public void testCopyBinaryToSameStorage() throws RODAException, IOException {
    delegate.testCopyBinaryToSameStorage();
  }

  @Test
  public void testMoveContainerToSameStorage() throws RODAException, IOException {
    delegate.testMoveContainerToSameStorage();
  }

  @Test
  public void testMoveDirectoryToSameStorage() throws RODAException, IOException {
    delegate.testMoveDirectoryToSameStorage();
  }

  @Test(enabled = false)
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    delegate.testMoveBinaryToSameStorage();
  }

  @Test(enabled = false)
  public void testBinaryVersions() throws RODAException, IOException {
    // TODO re-introduce this test once workaround to last version delete
    // constraint is done
    delegate.testBinaryVersions();
  }

}
