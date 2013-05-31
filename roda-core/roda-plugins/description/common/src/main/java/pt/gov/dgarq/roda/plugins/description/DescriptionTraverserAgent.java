package pt.gov.dgarq.roda.plugins.description;

import java.util.List;

import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;

/**
 * @author Rui Castro
 */
public interface DescriptionTraverserAgent<TR extends DescriptionTraverserResult> {

	public TR apply(SimpleDescriptionObject sdo, List<TR> childResults)
			throws Exception;

}
