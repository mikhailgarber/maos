package mg.maos;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectAttributes {
	
	
	
	private Map<String, Object> values = new HashMap<String, Object>();
	private Set<String> facets = new HashSet<String>();
	
	public ObjectAttributes add(String name, Object value) {
		return add(name, value, false);
	}
	
	public ObjectAttributes add(String name, Object value, boolean facet) {
		ObjectTypes.validateType(value, facet);
		values.put(name, value);
		if(facet) facets.add(name);
		return this;
	}
	
	public void remove(String name) {
		values.remove(name);
		facets.remove(name);
	}

	public Map<String, Object> getValues() {
		return Collections.unmodifiableMap(values);
	}

	public Object get(String name) {
		return values.get(name);
	}

	@Override
	public String toString() {
		return "ObjectAttributes [values=" + values + ", facets=" + facets + "]";
	}

	public Set<String> getFacets() {
		return Collections.unmodifiableSet(facets);
	}

	
	
	
	
}