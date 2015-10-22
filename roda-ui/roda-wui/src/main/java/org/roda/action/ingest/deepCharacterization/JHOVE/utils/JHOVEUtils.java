package org.roda.action.ingest.deepCharacterization.JHOVE.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.common.RodaCoreFactory;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.storage.Binary;
import org.roda.util.FileUtility;

import com.hp.hpl.jena.util.FileUtils;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;

public class JHOVEUtils {

  public static String runJHOVE(File f) throws Exception {
    App app = App.newAppWithName("Jhove");
    String saxClass = JhoveBase.getSaxClassFromProperties();
    JhoveBase je = new JhoveBase();
    je.setLogLevel("SEVERE");
    File configFile = File.createTempFile("jhove", "conf");
    FileOutputStream fos = new FileOutputStream(configFile);
    String jhoveConfigPath = RodaCoreFactory.getRodaConfigurationAsString("tools", "jhove", "config");
    IOUtils.copy(FileUtility.getConfigurationFile(RodaCoreFactory.getConfigPath(), jhoveConfigPath), fos);
    fos.close();
    je.init(configFile.getAbsolutePath(), saxClass);
    Module module = je.getModule(null);
    OutputHandler about = je.getHandler(null);
    OutputHandler handler = je.getHandler(null);

    String files[];
    files = new String[] {f.getAbsolutePath()};
    File jhoveOutput = File.createTempFile("jhove", ".txt");
    je.dispatch(app, module, about, handler, jhoveOutput.getAbsolutePath(), files);
    return "<jhove>" + FileUtils.readWholeFileAsUTF8(jhoveOutput.getAbsolutePath()) + "</jhove>";
  }

  public static RepresentationFilePreservationObject deepCharacterization(
    RepresentationFilePreservationObject premisObject, org.roda.model.File file, Binary binary,
    Map<String, String> parameterValues) throws Exception {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(binary.getContent().createInputStream(), fos);
    fos.close();
    String jhoveOutput = runJHOVE(f);
    premisObject.setObjectCharacteristicsExtension(jhoveOutput);
    return premisObject;
  }

}
