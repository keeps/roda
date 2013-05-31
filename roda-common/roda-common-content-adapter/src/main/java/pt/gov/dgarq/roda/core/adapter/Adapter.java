package pt.gov.dgarq.roda.core.adapter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the type of the entity adapter for this {@link Adapter}. The
 *            entity adapter should provide methods to adapt the attributes of
 *            the entities to the needs of the {@link Adapter}.
 */
public class Adapter<EA> {

	private EA entityAdapter = null;

	/**
	 * Constructs an empty {@link Adapter}.
	 * 
	 * @param entityAdapter
	 *            the entity adapter.
	 */
	public Adapter(EA entityAdapter) {
		setEntityAdapter(entityAdapter);
	}

	/**
	 * Returns the entity adapter.
	 * 
	 * @return the entity adapter.
	 */
	public EA getEntityAdapter() {
		return entityAdapter;
	}

	/**
	 * Sets the entity adapter.
	 * 
	 * @param entityAdapter
	 *            the entity adapter to set.
	 */
	public void setEntityAdapter(EA entityAdapter) {
		if (entityAdapter == null) {
			throw new NullPointerException("entityAdapter cannot be null");
		}
		this.entityAdapter = entityAdapter;
	}

}
