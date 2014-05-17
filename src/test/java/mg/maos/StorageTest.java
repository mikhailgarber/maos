package mg.maos;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import mg.maos.SearchCondition.OPERATION;

import org.junit.Assert;
import org.junit.Test;

public abstract class StorageTest {

	
	protected abstract StorageServiceInterface getStorageWithFacets();
	protected abstract StorageServiceInterface getStorage();
	protected abstract StorageServiceInterface getStorageForFind() throws IOException;

	@Test
	public void testBulk() {
		StorageServiceInterface storage = getStorageWithFacets();
		ObjectAttributes attrs1 = new ObjectAttributes();
		attrs1.add("Name", "John Doe");
		attrs1.add("Age", new Long(43));
		attrs1.add("Weight", 235.5);
		
		
		ObjectAttributes attrs2 = new ObjectAttributes();
		attrs2.add("Name", "Jane Doe");
		attrs2.add("Age", new Long(40));
		attrs2.add("Weight", 225.5);
		
		List<StoredObject> list = storage.create(attrs1, attrs2);
		StoredObject john = list.get(0);
		StoredObject jane = list.get(1);
		Assert.assertEquals("John Doe", john.getAttributes().get("Name"));
		john.setAttributes(john.getAttributes().add("Age", 67L));
		jane.setAttributes(jane.getAttributes().add("Age", 17L));
		storage.update(john, jane);
		jane = storage.get(jane.getId());
		Assert.assertEquals(17L, jane.getAttributes().get("Age"));
	}
	
	
	

	@Test
	public void testCRUD() {

		StorageServiceInterface storage = getStorage();
		Assert.assertEquals(0, storage.getCount());

		
		ObjectAttributes attrs = new ObjectAttributes();
		attrs.add("Name", "Mikhail Garber");
		attrs.add("Age", new Long(43));
		attrs.add("Weight", 235.5);
		Date now = new Date();
		attrs.add("Now", now);
		StoredObject so = storage.create(attrs);
		Assert.assertNotNull(so);
		Assert.assertEquals(1, storage.getCount());


		StoredObject copy = storage.get(so.getId());
		Assert.assertNotNull(copy);

		Assert.assertEquals(so.getId(), copy.getId());
		Assert.assertEquals("Mikhail Garber", copy.getAttributes().get("Name"));
		Assert.assertEquals(43L, copy.getAttributes().get("Age"));
		Assert.assertEquals(235.5, copy.getAttributes().get("Weight"));
		Assert.assertEquals(now, copy.getAttributes().get("Now"));

		Assert.assertNull(storage.get("ddgff hfdh fdh gdh gh gh"));

		attrs.add("Weight", 260.7);
		attrs.remove("Age");
		copy.setAttributes(attrs);
		storage.update(copy);

		copy = storage.get(so.getId());
		Assert.assertNotNull(copy);

		Assert.assertEquals(so.getId(), copy.getId());
		Assert.assertEquals("Mikhail Garber", copy.getAttributes().get("Name"));
		Assert.assertNull(copy.getAttributes().get("Age"));
		Assert.assertEquals(260.7, copy.getAttributes().get("Weight"));
		Assert.assertEquals(now, copy.getAttributes().get("Now"));

		storage.delete(copy.getId());

		copy = storage.get(so.getId());
		Assert.assertNull(copy);
		Assert.assertEquals(0, storage.getCount());

	}

	@Test
	public void testFacets() throws ParseException {
		StorageServiceInterface storage = getStorageWithFacets();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		addPlace(storage, "Seattle", sdf.parse("2006/04/20"), 1224354L, 23.7);
		addPlace(storage, "Frankfurt", sdf.parse("1996/01/06"), 7224354L, 33.7);
		addPlace(storage, "Detroit", sdf.parse("1992/03/17"), 224354L, 43.7);
		addPlace(storage, "Dallas", sdf.parse("1994/08/21"), 7224354L, 73.7);	
		SearchResult results = storage.find(new SearchRequest());
		Assert.assertEquals(4, results.getTotal());
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals(0, results.getFacets().size());
		results = storage.find(new SearchRequest().withFacet(new FacetCondition("Name", 100)));
		Assert.assertEquals(4, results.getTotal());
		Assert.assertEquals(1, results.getFacets().size());
		Assert.assertEquals(4, results.getFacets().get("Name").size());
		
		addPlace(storage, "Detroit", sdf.parse("1992/03/17"), 224354L, 43.7);
		
		results = storage.find(new SearchRequest().withFacet(new FacetCondition("Name", 100)));
		Assert.assertEquals(5, results.getTotal());
		Assert.assertEquals(4, results.getFacets().get("Name").size());
		results = storage.find(new SearchRequest().withFacet(new FacetCondition("Name", 1)));
		Assert.assertEquals(5, results.getTotal());
		Assert.assertEquals(1, results.getFacets().get("Name").size());
		
		results = storage.find(new SearchRequest().withFacet(new FacetCondition("Name", 100)).withOffset(0).withHowmany(1));
		Assert.assertEquals(5, results.getTotal());
		Assert.assertEquals(1, results.getResults().size());
		Assert.assertEquals(4, results.getFacets().get("Name").size());
		
		System.out.println("fscets:" + results.getFacets());
	}
	
	
	
	
	@Test
	public void testFind() throws Exception {
		StorageServiceInterface storage = getStorageForFind();
		
		// empty directory
		Assert.assertEquals(0, storage.getCount());
		
		SearchResult results = storage.find();
		Assert.assertEquals(0, results.getResults().size());
		
		storage.find(new SearchFilter());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		addPlace(storage, "Seattle", sdf.parse("2006/04/20"), 1224354L, 23.7);
		addPlace(storage, "Frankfurt", sdf.parse("1996/01/06"), 7224354L, 33.7);
		addPlace(storage, "Detroit", sdf.parse("1992/03/17"), 224354L, 43.7);
		addPlace(storage, "Dallas", sdf.parse("1994/08/21"), 7224354L, 73.7);
		
		
		results = storage.find(new SearchFilter());
		Assert.assertEquals(4, storage.getCount());
		
		// four doc total
		Assert.assertEquals(4, storage.getCount());
		
		// exact match of one field
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.EQ)));
		Assert.assertEquals(1, results.getResults().size());
		
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seatt", OPERATION.LIKE)));
		Assert.assertEquals(1, results.getResults().size());
		
		// OR of two filters, search with out operation specified
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle")), new SearchFilter().add(new SearchCondition("Name", "Frankfurt", OPERATION.EQ)));
		Assert.assertEquals(2, results.getResults().size());
		
		// AND on the same field
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.EQ)).add(new SearchCondition("Name", "Frankfirt", OPERATION.EQ)));
		Assert.assertEquals(0, results.getResults().size());
		// AND on two fields
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.EQ)).add(new SearchCondition("Visited", sdf.parse("2006/04/20"), OPERATION.EQ)));
		Assert.assertEquals(1, results.getResults().size());
		// Less on string
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.LT)));
		Assert.assertEquals(results.toString(), 3, results.getResults().size());
		// Less or equal string
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.LE)));
		Assert.assertEquals(results.toString(), 4, results.getResults().size());
		// greater on string
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.GT)));
		Assert.assertEquals(results.toString(), 0, results.getResults().size());

		// greater or equal string
		results = storage.find(new SearchFilter().add(new SearchCondition("Name", "Seattle", OPERATION.GE)));
		Assert.assertEquals(results.toString(), 1, results.getResults().size());

		// greater or equal double
		results = storage.find(new SearchFilter().add(new SearchCondition("Distance", 33.7d, OPERATION.GE)));
		Assert.assertEquals(results.toString(), 3, results.getResults().size());

		// greater  double
		results = storage.find(new SearchFilter().add(new SearchCondition("Distance", 33.7d, OPERATION.GT)));
		Assert.assertEquals(results.toString(), 2, results.getResults().size());
		
		// default field
		results = storage.find(new SearchFilter().add(new SearchCondition("Seattle")));
		Assert.assertEquals(1, results.getResults().size());
		
		results = storage.find(new SearchFilter().add(new SearchCondition(7224354L)));
		Assert.assertEquals(2, results.getResults().size());
		
		// pagination
		
		results = storage.find();
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals(4L, results.getTotal());
		
		results = storage.find(0, 2, null);
		Assert.assertEquals(2, results.getResults().size());
		Assert.assertEquals(4, results.getTotal());
		
		
		results = storage.find(1, 3, null);
		Assert.assertEquals(3, results.getResults().size());
		
		// sort
		
		results = storage.find(new SortCondition("Name", true));
		System.out.println(results);
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals("Seattle", results.getResults().get(0).getAttributes().get("Name"));
		
		results = storage.find(new SortCondition("Name", false));
		System.out.println(results);
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals("Dallas", results.getResults().get(0).getAttributes().get("Name"));
		
		
		results = storage.find(new SortCondition("Visited", true));
		System.out.println(results);
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals("Seattle", results.getResults().get(0).getAttributes().get("Name"));
		
		results = storage.find(new SortCondition("Visited", false));
		System.out.println(results);
		Assert.assertEquals(4, results.getResults().size());
		Assert.assertEquals("Detroit", results.getResults().get(0).getAttributes().get("Name"));
		
		
		// delete
		
		storage.delete(new SearchFilter().add(new SearchCondition(7224354L)));
		results = storage.find(new SearchFilter().add(new SearchCondition(7224354L)));
		Assert.assertEquals(0, results.getResults().size());
		
	}

	private void addPlace(StorageServiceInterface storage, String name, Date visited, Long population, Double distance) {
		ObjectAttributes attrs = new ObjectAttributes();
		attrs.add("Name", name, storage.isFacetEnabled());
		attrs.add("Visited", visited);
		attrs.add("Population", population);
		attrs.add("Distance", distance);
		storage.create(attrs);
	}
}
