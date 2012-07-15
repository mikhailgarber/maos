package mg.maos;

import java.util.List;


public interface StorageServiceInterface {
	public StoredObject create(ObjectAttributes attributes);
	public List<StoredObject> create(ObjectAttributes ... attributes);
	public StoredObject get(String id);
	public void update(StoredObject so);
	public void update(StoredObject ... so);
	public void delete(String id);
	public SearchResult find(int offset, int howmany, SortCondition sort, SearchFilter ... filters);
	public SearchResult find(SearchFilter ... filters);
	public SearchResult find(SortCondition sort, SearchFilter ... filters);
	public SearchResult find(SearchRequest search);
	public void delete(SearchFilter ... filters);
	public boolean isFacetEnabled();
}
