package mg.maos;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
	
	private  int offset = 0;
	private  int howmany = 200;
	private  SortCondition sort = null;
	private  List<SearchFilter> filters = new ArrayList<SearchFilter>();
	private  List<FacetCondition> facets = new ArrayList<FacetCondition>();
	
	public SearchRequest withOffset(int offset) {
		this.offset = offset;
		return this;
	}
	
	public SearchRequest withHowmany(int howmany) {
		this.howmany = howmany;
		return this;
	}
	
	public SearchRequest withSort(SortCondition sort) {
		this.sort = sort;
		return this;
	}
	
	public SearchRequest withFilter(SearchFilter filter) {
		this.filters.add(filter);
		return this;
	}
	
	public SearchRequest withFacet(FacetCondition facet) {
		this.facets.add(facet);
		return this;
	}

	protected int getOffset() {
		return offset;
	}

	protected int getHowmany() {
		return howmany;
	}

	protected SortCondition getSort() {
		return sort;
	}

	protected List<SearchFilter> getFilters() {
		return filters;
	}

	protected List<FacetCondition> getFacets() {
		return facets;
	}
	
	
}
