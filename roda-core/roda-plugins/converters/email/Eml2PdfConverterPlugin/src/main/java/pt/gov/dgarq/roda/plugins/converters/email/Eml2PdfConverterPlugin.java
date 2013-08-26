package pt.gov.dgarq.roda.plugins.converters.email;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;

/**
 * @author Rui Castro
 */
public class Eml2PdfConverterPlugin extends AbstractRodaMigratorPlugin {

    private static Logger logger = Logger
            .getLogger(Eml2PdfConverterPlugin.class);
    private final String name = "Converter/EML to PDF/A";
    private final float version = 1.0f;
    private final String description = "Email EML representations normalizer plugin. "
            + "Convert Email representations from EML to PDF using RODA Migrator service EML2Pdf.";

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
        return "eml2pdf-converter.properties";
    }

    /**
     *
     * @see AbstractRodaMigratorPlugin#getRepresentationType ()
     */
    @Override
    protected String getRepresentationType() {
        return RepresentationObject.EMAIL;
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
