package mg.maos;

import java.util.ArrayList;
import java.util.List;

public class SearchFilter {
	private List<SearchCondition> conditions = new ArrayList<SearchCondition>();
	
	public SearchFilter add(SearchCondition condition) {
		conditions.add(condition);
		return this;
	}
	public SearchFilter remove(SearchCondition condition) {
		conditions.remove(condition);
		return this;
	}
	
	
	public List<SearchCondition> getConditions() {
		return conditions;
	}
	
	
	public boolean isEmpty() {
		return conditions.isEmpty();
	}
	@Override
	public String toString() {
		return "SearchFilter [conditions=" + conditions + "]";
	}
	
	
}
