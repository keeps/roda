package org.roda.index;

import java.util.List;

public class IndexResult<T> {

	private final long offset;
	private final long limit;
	private final long totalCount;
	private final List<T> results;

	public IndexResult(long offset, long limit, long totalCount, List<T> results) {
		super();
		this.offset = offset;
		this.limit = limit;
		this.totalCount = totalCount;
		this.results = results;
	}

	/**
	 * @return the offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return the limit
	 */
	public long getLimit() {
		return limit;
	}

	/**
	 * @return the totalCount
	 */
	public long getTotalCount() {
		return totalCount;
	}

	/**
	 * @return the results
	 */
	public List<T> getResults() {
		return results;
	}

}
