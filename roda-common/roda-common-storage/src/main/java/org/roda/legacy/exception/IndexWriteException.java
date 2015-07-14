/**
 * 
 */
package org.roda.legacy.exception;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IndexWriteException extends IndexException {
	private static final long serialVersionUID = 1021331034356369665L;

	public IndexWriteException(String message, int code) {
		super(message, code);
	}
}
