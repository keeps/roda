package pt.gov.dgarq.roda.common.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.common.FormatUtility;

public class FormatUtilityTest {

  @Test
  public void getMimetypeFromFile() {
    final File f = new File("pom.xml");
    final String mime = FormatUtility.getMimetype(f, f.getName());
    Assert.assertEquals("application/xml", mime);
  }

//  @Test
//  public void getMimetypeFromName() {
//    final File f = new File("pom.xml");
//    final String mime = FormatUtility.getMimetype(f.getName());
//    Assert.assertEquals("text/xml", mime);
//  }

//  @Test
//  public void getMimetypeFromNameAndInputstream() throws FileNotFoundException {
//    final File f = new File("pom.xml");
//    final String mime = FormatUtility.getMimetype(f.getName(), new FileInputStream(f));
//    Assert.assertEquals("text/xml", mime);
//  }

//  @Test
//  public void getMimetypeFromInputstream() throws FileNotFoundException {
//    final File f = new File("pom.xml");
//    final String mime = FormatUtility.getMimetype(new FileInputStream(f), f.getName());
//    Assert.assertEquals("text/xml", mime);
//  }

  @Test
  public void getFileFormat() throws FileNotFoundException {
    final File f = new File("pom.xml");
    final FileFormat fileFormat = FormatUtility.getFileFormat(f, f.getName());
    Assert.assertEquals("application/xml", fileFormat.getMimetype());
    if(fileFormat.getVersion() != null){
       Assert.assertEquals("1.0", fileFormat.getVersion());
    }
  }
}
