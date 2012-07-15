package mg.maos;

public class SortCondition {

	private final String name;
	private final boolean reversed;
	
	public SortCondition(String name, boolean reversed) {
		super();
		this.name = name;
		this.reversed = reversed;
	}

	public String getName() {
		return name;
	}

	public boolean isReversed() {
		return reversed;
	}
	
	
	
}
