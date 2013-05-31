package pt.gov.dgarq.roda.sipcreator;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.ClassificationPlanHelper;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;

/**
 * 
 * @author Luis Faria
 * 
 */
public class MyClassificationPlanHelper extends ClassificationPlanHelper {

	private static final Logger logger = Logger
			.getLogger(MyClassificationPlanHelper.class);

	private static MyClassificationPlanHelper instance = null;

	/**
	 * Get Classification Plan Helper Singleton
	 * 
	 * @return the singleton
	 */
	public static MyClassificationPlanHelper getInstance() {
		if (instance == null) {
			instance = new MyClassificationPlanHelper();
		}
		return instance;
	}

	private MyClassificationPlanHelper() {
		super(SIPCreatorConfig.getInstance().getEadDir(), SIPCreatorConfig
				.getInstance().getTmpDir());
		addClassificationPlanListener(new ClassificationPlanListener() {

			public void onUpdate(String id) {
				Loading.setMessage(Messages.getString(
						"ClassificationPlanHelper.loading.UPDATE", id));
			}

		});
	}

	/**
	 * Get complete reference of a description object, as part of classification
	 * plan.
	 * 
	 * @param obj
	 * @return the complete reference
	 * @throws EadCMetadataException
	 * @throws IOException
	 */
	public String getCompleteReference(DescriptionObject obj)
			throws EadCMetadataException, IOException {
		return getCompleteReference(obj.getPid());
	}

	/**
	 * Get a SIP description object complete reference
	 * 
	 * @param sdObj
	 * @param sip
	 * 
	 * @return the complete reference
	 */
	public String getCompleteReference(SIPDescriptionObject sdObj, SIP sip) {
		String label = "";
		logger.debug("complete ref of " + sdObj);
		try {
			String baseRef = MyClassificationPlanHelper.getInstance()
					.getCompleteReference(sip.getParentPID());
			logger.debug("base ref=" + baseRef);
			if (baseRef != null) {
				label += baseRef;
			}
		} catch (IOException e1) {
			logger.error("Error getting SIP parent complete reference", e1);
		} catch (EadCMetadataException e1) {
			logger.error("Error getting SIP parent complete reference", e1);
		}

		SIPDescriptionObject root = sip.getDescriptionObject();

		if (label.length() > 0) {
			label += "/";
		}

		if (root == sdObj) {
			logger.debug("ref from root=" + sdObj.getId());
			label += sdObj.getId();
		} else {
			String relativeRef = getRelativeReference(root.getChildren(), sdObj);
			logger.debug("relative ref=" + relativeRef);
			if (relativeRef != null) {
				label += root.getId() + "/" + relativeRef;
			} else {
				label += root.getId() + "/.../" + sdObj.getId();
				logger.error("Broken hierarchy between a "
						+ "SIP Description Object and its "
						+ "SIP root Description Object ");
			}

		}

		logger.debug("ref=" + label);

		return label;
	}

	private String getRelativeReference(List<SIPDescriptionObject> sources,
			SIPDescriptionObject target) {
		String ref = null;
		for (SIPDescriptionObject sipdo : sources) {
			if (sipdo == target) {
				ref = sipdo.getId();
				break;
			} else {
				String subRef = getRelativeReference(sipdo.getChildren(),
						target);
				if (subRef != null) {
					ref = sipdo.getId() + "/" + subRef;
					break;
				}
			}
		}

		return ref;
	}

}
