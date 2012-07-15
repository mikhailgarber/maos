package mg.maos;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchResult {

	private final int howmany;
	private final int offset;
	private final List<StoredObject> results;
	private final Map<String, List<FacetCondition>> facets;
	private final long total;
	
	protected long getTotal() {
		return total;
	}

	protected SearchResult(int howmany, int offset, List<StoredObject> results, Map<String, List<FacetCondition>> facets, long total) {
		super();
		this.howmany = howmany;
		this.offset = offset;
		this.results = results;
		this.facets = facets;
		this.total = total;
	}

	protected int getHowmany() {
		return howmany;
	}

	protected int getOffset() {
		return offset;
	}

	protected List<StoredObject> getResults() {
		return Collections.unmodifiableList(results);
	}

	protected Map<String, List<FacetCondition>> getFacets() {
		return Collections.unmodifiableMap(facets);
	}

	@Override
	public String toString() {
		return "SearchResult [howmany=" + howmany + ", offset=" + offset + ", results=" + results + ", facets=" + facets + ", total=" + total + "]";
	}
	
	
	
	
}
