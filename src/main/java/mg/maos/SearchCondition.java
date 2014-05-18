package mg.maos;

public class SearchCondition {

	public enum OPERATION {EQ, LT, LE, GT, GE, NEQ, LIKE};
	
	private final String name;
	private final Object value;
	private final OPERATION operation;
	public static final String DEFAULT_FIELD = "alltext";
	
	public SearchCondition(String name, Object value, OPERATION operation) {
		super();
		ObjectTypes.validateType(value);
		this.name = name;
		this.value = value;
		this.operation = operation;
	}
	
	public SearchCondition(String name, Object value) {
		this(name, value, OPERATION.EQ);
	}
	
	public SearchCondition(Object value) {
		this(DEFAULT_FIELD, value, OPERATION.EQ);
	}
	

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public OPERATION getOperation() {
		return operation;
	}
	
	

	@Override
	public String toString() {
		return "SearchCondition [name=" + name + ", value=" + value + ", operation=" + operation + "]";
	}
	
	
}

