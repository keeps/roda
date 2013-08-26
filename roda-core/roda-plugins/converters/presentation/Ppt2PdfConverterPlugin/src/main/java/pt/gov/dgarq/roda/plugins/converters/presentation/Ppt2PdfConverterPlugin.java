package pt.gov.dgarq.roda.plugins.converters.presentation;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;

/**
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class Ppt2PdfConverterPlugin extends AbstractRodaMigratorPlugin {

    private static Logger logger = Logger
            .getLogger(Ppt2PdfConverterPlugin.class);
    private final String name = "Converter/PPT to PDF/A";
    private final float version = 1.0f;
    private final String description = "Presentation PPT representations normalizer plugin. "
            + "Convert Presentation representations from PPT to PDF using RODA Migrator service ST2Pdf.";

    /**
     *
     * @see Plugin#getName()
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @see Plugin#getVersion()
     */
    public float getVersion() {
        return version;
    }

    /**
     * @see Plugin#getDescription()
     */
    public String getDescription() {
        return description;
    }

    @Override
    protected String getConfigurationFile() {
        return "ppt2pdf-converter.properties";
    }

    /**
     *
     * @see AbstractRodaMigratorPlugin#getRepresentationType ()
     */
    @Override
    protected String getRepresentationType() {
        return RepresentationObject.PRESENTATION;
    }

    /**
     *
     * @see AbstractRodaMigratorPlugin#isNormalization()
     */
    @Override
    protected boolean isNormalization() {
        return true;
    }
}
