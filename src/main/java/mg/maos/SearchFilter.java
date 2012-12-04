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
	
	protected String toQuery() {
		StringBuffer sb = new StringBuffer();
		for(SearchCondition condition : conditions) {
			if(sb.length() > 0) sb.append(" AND ");
			sb.append(" (").append(condition.toQuery()).append(") ");
		}
		return sb.toString();
	}
	
	public boolean isEmpty() {
		return conditions.isEmpty();
	}
	@Override
	public String toString() {
		return "SearchFilter [conditions=" + conditions + "]";
	}
	
	
}
