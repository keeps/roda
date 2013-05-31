package pt.gov.dgarq.roda.plugins.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.stubs.Browser;

/**
 * @author Miguel Ferreira
 * @author Rui Castro
 */
abstract public class DescriptionTraverserPlugin<A extends DescriptionTraverserAgent<TR>, TR extends DescriptionTraverserResult>
		extends AbstractPlugin {

	/**
	 * Constructs a new {@link DescriptionTraverserPlugin}
	 * 
	 * @param parameters
	 *            the plugin parameters, set with default values or null
	 */
	// public DescriptionTraverserPlugin(PluginParameter... parameters) {
	// super(parameters);
	// }
	abstract protected Browser getBrowserService();

	abstract protected A getDescriptionTraverserAgent();

	protected TR traverseSDO(SimpleDescriptionObject simpleDO) throws Exception {

		List<SimpleDescriptionObject> childSDOs = getChildSDOs(simpleDO
				.getPid());

		List<TR> childResults = new ArrayList<TR>();

		for (SimpleDescriptionObject childSDO : childSDOs) {
			childResults.add(traverseSDO(childSDO));
		}

		return getDescriptionTraverserAgent().apply(simpleDO, childResults);
	}

	private List<SimpleDescriptionObject> getChildSDOs(String sdoPID)
			throws Exception {

		SimpleDescriptionObject[] children = getBrowserService()
				.getSimpleDescriptionObjects(
						new ContentAdapter(
								new Filter(new SimpleFilterParameter(
										"parentPID", sdoPID)), null, null));

		List<SimpleDescriptionObject> childSDOs = new ArrayList<SimpleDescriptionObject>();

		if (children != null) {
			childSDOs.addAll(Arrays.asList(children));
		}

		return childSDOs;
	}

}
