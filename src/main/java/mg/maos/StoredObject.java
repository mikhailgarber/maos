package mg.maos;

public class StoredObject {
	
	
	protected StoredObject(String id) {
		super();
		this.id = id;
	}
	private String id;
	private ObjectAttributes attributes = new ObjectAttributes();
	
	public String getId() {
		return id;
	}
	public ObjectAttributes getAttributes() {
		return attributes;
	}
	public void setAttributes(ObjectAttributes attributes) {
		this.attributes = attributes;
	}
	@Override
	public String toString() {
		return "StoredObject [id=" + id + ", attributes=" + attributes + "]";
	}
	
	
	
}
