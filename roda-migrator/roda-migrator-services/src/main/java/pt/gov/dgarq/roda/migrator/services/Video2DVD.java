package pt.gov.dgarq.roda.migrator.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * Convert video to the preservation format based on DVD
 *
 * mencoder <souce> -o <target>.mpeg -oac lavc -ovc lavc -of mpeg -mpegopts
 * format=dvd \ -lavcopts
 * vcodec=mpeg2video:vbitrate=5000:acodec=ac3:abitrate=192
 *
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class Video2DVD extends MencoderConverter {

    /**
     * Create a new video2flv convertion service
     *
     * @throws RODAServiceException
     */
    public Video2DVD() throws RODAServiceException {
        targetExtension = ".mpeg";
    }

    @Override
    protected List<String> getOptions(RepresentationObject representation) {
        List<String> options;
        if (representation.getSubType().equals("video/x-ms-wmv")) {
            options = Arrays.asList(new String[]{"-oac", "lavc", "-ovc",
                "lavc", "-of", "mpeg", "-mpegopts", "format=dvd:tsaf",
                "-lavcopts",
                "vcodec=mpeg2video:vbitrate=5000:acodec=ac3:abitrate=192",
                "-mc", "0", "-ofps", "25.000", "-msglevel", "all=4",
                "-quiet"});

        } else {
            options = Arrays.asList(new String[]{"-oac", "lavc", "-ovc",
                "lavc", "-of", "mpeg", "-mpegopts", "format=dvd:tsaf",
                "-lavcopts",
                "vcodec=mpeg2video:vbitrate=5000:acodec=ac3:abitrate=192",
                "-ofps", "25.000", "-msglevel", "all=4", "-quiet"});
        }

        return options;
    }

    @Override
    protected FileFormat getFileFormat() {
        FileFormat fileFormat = new FileFormat();
        fileFormat.setMimetype("video/mpeg");
        fileFormat.setPuid("x-fmt/386");
        fileFormat.setName("MPEG-2 Video Format");
        fileFormat.setFormatRegistryName("PRONOM http://www.nationalarchives.gov.uk/PRONOM");
        String[] extensions = new String[2];
        fileFormat.setExtensions(extensions);
        extensions[0] = "mpg";
        extensions[1] = "mpg";
        return fileFormat;
    }
}
