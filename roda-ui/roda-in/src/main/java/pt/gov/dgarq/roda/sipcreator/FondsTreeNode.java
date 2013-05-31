/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Luis Faria
 * 
 */
public class FondsTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 5083545590616242516L;

	private boolean dragOver = false;

	/**
	 * Create a new Fonds Tree node
	 * 
	 * @param userObject
	 */
	public FondsTreeNode(Object userObject) {
		super(userObject);
	}

	/**
	 * Is drag over
	 * 
	 * @return true if drag is over
	 */
	public boolean isDragOver() {
		return dragOver;
	}

	/**
	 * Set drag over
	 * 
	 * @param dragOver
	 */
	public void setDragOver(boolean dragOver) {
		this.dragOver = dragOver;
	}

}
