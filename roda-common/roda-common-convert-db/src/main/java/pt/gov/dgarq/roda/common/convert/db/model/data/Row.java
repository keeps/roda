/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.data;

import java.util.List;
import java.util.Vector;

/**
 * A table data row container.
 * 
 * @author Luis Faria
 * 
 */
public class Row {
	private int index;

	private List<Cell> cells;
	
	/**
	 * Empty TableStructure data row constructor
	 * 
	 */
	public Row() {
		this.cells = new Vector<Cell>();
	}

	/**
	 * TableStructure data row constructor
	 * 
	 * @param index
	 *            the sequence number of the row in the table
	 * @param cells
	 *            the list of cell within this row
	 */
	public Row(int index, List<Cell> cells) {
		this.index = index;
		this.cells = cells;
	}

	/**
	 * @return the sequence number of the row in the table
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the sequence number of the row in the table
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the list of cell within this row
	 */
	public List<Cell> getCells() {
		return cells;
	}

	/**
	 * @param cells
	 *            the list of cell within this row
	 */
	public void setCells(List<Cell> cells) {
		this.cells = cells;
	}

}
