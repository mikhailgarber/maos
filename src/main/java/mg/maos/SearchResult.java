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
	
	public long getTotal() {
		return total;
	}

	public SearchResult(int howmany, int offset, List<StoredObject> results, Map<String, List<FacetCondition>> facets, long total) {
		super();
		this.howmany = howmany;
		this.offset = offset;
		this.results = results;
		this.facets = facets;
		this.total = total;
	}

	public int getHowmany() {
		return howmany;
	}

	public int getOffset() {
		return offset;
	}

	public List<StoredObject> getResults() {
		return Collections.unmodifiableList(results);
	}

	public Map<String, List<FacetCondition>> getFacets() {
		return Collections.unmodifiableMap(facets);
	}

	@Override
	public String toString() {
		return "SearchResult [howmany=" + howmany + ", offset=" + offset + ", results=" + results + ", facets=" + facets + ", total=" + total + "]";
	}
	
	
	
	
}
