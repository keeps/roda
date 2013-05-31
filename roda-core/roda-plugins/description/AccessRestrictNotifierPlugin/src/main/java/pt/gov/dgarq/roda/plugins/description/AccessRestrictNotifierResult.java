package pt.gov.dgarq.roda.plugins.description;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.plugins.description.DescriptionTraverserResult;

/**
 * @author Miguel Ferreira
 * @author Rui Castro
 */
public class AccessRestrictNotifierResult implements DescriptionTraverserResult {

	private List<DescriptionObject> descriptionObjects = new ArrayList<DescriptionObject>();

	/**
	 * Construct a new {@link AccessRestrictNotifierAgent}.
	 */
	public AccessRestrictNotifierResult() {
	}

	/**
	 * Construct a new {@link AccessRestrictNotifierAgent}.
	 * 
	 * @param descriptionObject
	 */
	public AccessRestrictNotifierResult(DescriptionObject descriptionObject) {
	}

	/**
	 * @return the descriptionObjects
	 */
	public List<DescriptionObject> getDescriptionObjects() {
		return descriptionObjects;
	}

	/**
	 * @param descriptionObjects
	 *            the descriptionObjects to set
	 */
	public void setDescriptionObjects(List<DescriptionObject> descriptionObjects) {
		this.descriptionObjects.clear();
		if (descriptionObjects != null) {
			this.descriptionObjects.addAll(descriptionObjects);
		}
	}

	/**
	 * @param descriptionObject
	 *            the descriptionObject to add
	 */
	public void addDescriptionObject(DescriptionObject descriptionObject) {
		if (descriptionObject != null) {
			this.descriptionObjects.add(descriptionObject);
		}
	}

	/**
	 * @param descriptionObjects
	 */
	public void addDescriptionObjects(List<DescriptionObject> descriptionObjects) {
		if (descriptionObjects != null) {
			this.descriptionObjects.addAll(descriptionObjects);
		}
	}

}
