package mg.maos;

public class FacetCondition {

	private final String name;
	private final int count;
	
	public FacetCondition(String name, int maxCount) {
		super();
		this.name = name;
		this.count = maxCount;
	}

	public String getName() {
		return name;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "FacetCondition [name=" + name + ", count=" + count + "]";
	}
	
	
	
}
