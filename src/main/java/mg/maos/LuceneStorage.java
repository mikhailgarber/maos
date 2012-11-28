package mg.maos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.facet.index.CategoryDocumentBuilder;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneStorage implements StorageServiceInterface {

	private final Directory dir;
	private final Directory taxoDir;

	private final String ID_FIELD = "id";

	private final String TYPE_SUFFIX = "dfgdsgdg";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final int MAX_PAGE_SIZE = 500;

	private QueryParser parser = new QueryParser(Version.LUCENE_36, SearchCondition.DEFAULT_FIELD, new StandardAnalyzer(Version.LUCENE_36));

	@Override
	public StoredObject create(final ObjectAttributes attributes) {

		return (StoredObject) doWithWriter(new ActionInterface() {
			public Object proceed(Object... args) throws Exception {
				IndexWriter writer = (IndexWriter) args[0];
				TaxonomyWriter taxoWriter = (TaxonomyWriter) args[1];
				StoredObject so = new StoredObject(UUID.randomUUID().toString());
				addDocument(attributes, writer, taxoWriter, so);
				return so;
			}
		});

	}

	private boolean taxoEnabled() {
		return (taxoDir != null);
	}

	protected void addFacets(TaxonomyWriter taxoWriter, Document doc, ObjectAttributes attrs) throws IOException {

		if (attrs.getFacets().size() == 0)
			return;

		if (!taxoEnabled())
			return;

		List<CategoryPath> cats = new ArrayList<CategoryPath>();
		for (String facet : attrs.getFacets()) {
			cats.add(new CategoryPath(facet, attrs.get(facet).toString()));
		}

		CategoryDocumentBuilder b = new CategoryDocumentBuilder(taxoWriter);
		b.setCategoryPaths(cats).build(doc);

	}

	private Object doWithWriter(ActionInterface action) {
		IndexWriter writer = null;
		TaxonomyWriter taxoWriter = null;
		try {
			writer = getWriter();
			taxoWriter = getTaxoWriter();
			return action.proceed(writer, taxoWriter);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (taxoWriter != null)
					taxoWriter.close();
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Object doWithSearcher(ActionInterface action) {
		IndexSearcher searcher = null;
		TaxonomyReader taxoReader = null;
		try {
			searcher = getSearcher();
			taxoReader = getTaxoReader();
			return action.proceed(searcher, taxoReader);
		} catch (Exception e) {
			if (e instanceof IndexNotFoundException) {
				return null;
			}
			throw new RuntimeException(e);
		} finally {
			try {
				if (taxoReader != null)
					taxoReader.close();
				if (searcher != null)
					searcher.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private TaxonomyReader getTaxoReader() throws IOException {
		return (taxoEnabled()) ? new DirectoryTaxonomyReader(taxoDir) : null;
	}

	private IndexSearcher getSearcher() throws CorruptIndexException, IOException {
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
		return searcher;
	}

	private Document toDocument(StoredObject storedObject) {
		Document result = new Document();
		result.add(new Field(ID_FIELD, storedObject.getId(), Field.Store.YES, Field.Index.ANALYZED));
		ObjectAttributes attrs = storedObject.getAttributes();
		StringBuffer defalut = new StringBuffer();
		for (Entry<String, Object> entry : attrs.getValues().entrySet()) {
			String name = entry.getKey();
			String value = ObjectTypes.toStorable(entry.getValue());
			defalut.append(" ").append(value);
			String type = entry.getValue().getClass().getName();
			result.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED));
			result.add(new Field(name + TYPE_SUFFIX, type, Field.Store.YES, Field.Index.ANALYZED));
		}
		if (defalut.length() > 0)
			result.add(new Field(SearchCondition.DEFAULT_FIELD, defalut.toString(), Field.Store.NO, Field.Index.ANALYZED));

		return result;
	}

	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
		return new IndexWriter(dir, config);
	}

	private TaxonomyWriter getTaxoWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if (!taxoEnabled())
			return null;

		return new DirectoryTaxonomyWriter(taxoDir, OpenMode.CREATE_OR_APPEND);

	}

	public LuceneStorage(Directory dir) {
		super();
		this.dir = dir;
		this.taxoDir = null;
	}

	public LuceneStorage(Directory dir, Directory taxo) {
		super();
		this.dir = dir;
		this.taxoDir = taxo;
	}

	@Override
	public StoredObject get(String id) {
		final String query = idQuery(id);
		return (StoredObject) doWithSearcher(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexSearcher searcher = (IndexSearcher) args[0];
				SearchResult results = doSearch(0, 1, query, searcher, new Sort(), null, null);
				return (results.getResults().size() == 0) ? null : results.getResults().get(0);
			}
		});
	}

	@Override
	public void update(final StoredObject so) {
		doWithWriter(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexWriter writer = (IndexWriter) args[0];
				TaxonomyWriter taxoWriter = (TaxonomyWriter) args[1];
				updateOne(so, writer, taxoWriter);
				return null;
			}
		});

	}

	@Override
	public void delete(final String id) {
		doWithWriter(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexWriter writer = (IndexWriter) args[0];
				deleteOne(id, writer);
				return null;
			}
		});

	}

	private SearchResult doFind(final int offset, final int howmany, final SortCondition sort, List<SearchFilter> filters, final List<FacetCondition> facets) {
		final String query = buildQuery(filters);
		SearchResult result =  (SearchResult) doWithSearcher(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexSearcher searcher = (IndexSearcher) args[0];
				TaxonomyReader taxoReader = (TaxonomyReader) args[1];
				return doSearch(offset, howmany, query, searcher, getSort(sort), taxoReader, facets);
			}
		});
		if(result == null) {
			return new SearchResult(howmany, offset, new ArrayList<StoredObject>(), new HashMap<String, List<FacetCondition>>(), 0L);
		} else {
			return result;
		}
	}

	protected FacetSearchParams getFacets(List<FacetCondition> facets) {
		if (facets == null || facets.size() == 0 || !taxoEnabled())
			return null;
		FacetSearchParams params = new FacetSearchParams();
		for (FacetCondition facet : facets) {
			params.addFacetRequest(new CountFacetRequest(new CategoryPath(facet.getName()), facet.getCount()));
		}
		return params;
	}

	protected Sort getSort(SortCondition sort) {
		if (sort == null)
			return new Sort();
		return new Sort(new SortField(sort.getName(), SortField.STRING_VAL, sort.isReversed()));
	}

	private String buildQuery(List<SearchFilter> filters) {
		StringBuffer sb = new StringBuffer();
		for (SearchFilter filter : filters) {
			if (sb.length() > 0)
				sb.append(" OR ");
			sb.append("(").append(filter.toQuery()).append(")");
		}
		return (sb.length() == 0) ? ID_FIELD + ":0* 1* 2* 3* 4* 5* 6* 7* 8* 9* a* b* c* d* e* f*" : sb.toString();
	}

	private void deleteOne(final String id, IndexWriter writer) throws CorruptIndexException, IOException, ParseException {
		writer.deleteDocuments(parser.parse(idQuery(id)));
	}

	private String idQuery(final String id) {
		return ID_FIELD + ":\"" + id + "\"";
	}

	private SearchResult doSearch(int offset, int howmany, final String query, IndexSearcher searcher, Sort sort, TaxonomyReader taxoReader, List<FacetCondition> facets) throws ParseException, IOException {

		FacetSearchParams facetParams = getFacets(facets);
		logger.info("query:" + query);
		Query q = parser.parse(query);
		

		TopFieldCollector topCollector = TopFieldCollector.create(sort, offset + howmany, true, false, true, true);

		List<FacetResult> facetResults = null;
		if (facetParams != null) {
			IndexReader reader = searcher.getIndexReader();
			FacetsCollector facetCollector = new FacetsCollector(facetParams, reader, taxoReader);
			searcher.search(q, MultiCollector.wrap(topCollector, facetCollector));
			facetResults = facetCollector.getFacetResults();
		} else {
			searcher.search(q, topCollector);
		}

		List<StoredObject> result = new ArrayList<StoredObject>();
		int pos = 0;
		for (ScoreDoc doc : topCollector.topDocs().scoreDocs) {
			if (pos >= offset && pos <= offset + howmany) {
				result.add(fromDocument(searcher.doc(doc.doc)));
			}
			pos++;
		}

		Map<String, List<FacetCondition>> facetResult = new HashMap<String, List<FacetCondition>>();

		if (facetResults != null)
			for (FacetResult fr : facetResults) {
				FacetResultNode resultNode = fr.getFacetResultNode();
				if (resultNode.getNumSubResults() > 0) {
					List<FacetCondition> list = new ArrayList<FacetCondition>();
					for (FacetResultNode node : resultNode.getSubResults()) {
						list.add(new FacetCondition(node.getLabel().lastComponent(), (int) node.getValue()));
					}
					facetResult.put(resultNode.getLabel().lastComponent(), list);
				}
			}

		logger.info("results:" + result.size());
		return new SearchResult(howmany, offset, result, facetResult, topCollector.getTotalHits());

	}

	private StoredObject fromDocument(Document doc) {
		StoredObject result = null;
		ObjectAttributes attrs = new ObjectAttributes();
		for (Fieldable field : doc.getFields()) {
			String name = field.name();
			String value = field.stringValue();
			if (ID_FIELD.equals(name)) {
				result = new StoredObject(doc.get(ID_FIELD));
			} else if (!name.endsWith(TYPE_SUFFIX)) {
				String type = doc.get(name + TYPE_SUFFIX);
				attrs.add(name, ObjectTypes.fromStorable(value, type));
			}
		}
		result.setAttributes(attrs);
		return result;
	}

	@Override
	public void delete(final SearchFilter... filters) {
		doWithWriter(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexWriter writer = (IndexWriter) args[0];
				writer.deleteDocuments(parser.parse(buildQuery(Arrays.asList(filters))));
				return null;
			}
		});

	}

	@Override
	public SearchResult find(SearchRequest search) {
		return doFind(search.getOffset(), search.getHowmany(), search.getSort(), search.getFilters(), search.getFacets());
	}

	@Override
	public SearchResult find(int offset, int howmany, SortCondition sort, SearchFilter... filters) {
		return doFind(offset, howmany, sort, Arrays.asList(filters), null);
	}

	@Override
	public SearchResult find(SearchFilter... filters) {
		return doFind(0, MAX_PAGE_SIZE, null, Arrays.asList(filters), null);
	}

	@Override
	public SearchResult find(SortCondition sort, SearchFilter... filters) {
		return doFind(0, MAX_PAGE_SIZE, sort, Arrays.asList(filters), null);
	}

	@Override
	public boolean isFacetEnabled() {
		return taxoEnabled();
	}

	private void addDocument(final ObjectAttributes attributes, IndexWriter writer, TaxonomyWriter taxoWriter, StoredObject so) throws IOException, CorruptIndexException {
		so.setAttributes(attributes);
		Document doc = toDocument(so);
		addFacets(taxoWriter, doc, attributes);
		writer.addDocument(doc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StoredObject> create(final ObjectAttributes... attributes) {
		return (List<StoredObject>) doWithWriter(new ActionInterface() {
			public Object proceed(final Object... args) throws Exception {
				final IndexWriter writer = (IndexWriter) args[0];
				final TaxonomyWriter taxoWriter = (TaxonomyWriter) args[1];
				return addDocuments(attributes, writer, taxoWriter);
			}
		});
	}

	protected List<StoredObject> addDocuments(ObjectAttributes[] attributes, IndexWriter writer, TaxonomyWriter taxoWriter) throws CorruptIndexException, IOException {
		List<StoredObject> result = new ArrayList<StoredObject>();
		for (ObjectAttributes attr : attributes) {
			StoredObject so = new StoredObject(UUID.randomUUID().toString());
			addDocument(attr, writer, taxoWriter, so);
			result.add(so);
		}
		return result;
	}

	@Override
	public void update(final StoredObject... sos) {
		doWithWriter(new ActionInterface() {

			public Object proceed(Object... args) throws Exception {
				IndexWriter writer = (IndexWriter) args[0];
				TaxonomyWriter taxoWriter = (TaxonomyWriter) args[1];
				for (StoredObject so : sos) {
					updateOne(so, writer, taxoWriter);
				}
				return null;
			}
		});

	}

	private void updateOne(final StoredObject so, IndexWriter writer, TaxonomyWriter taxoWriter) throws CorruptIndexException, IOException, ParseException {
		deleteOne(so.getId(), writer);
		addDocument(so.getAttributes(), writer, taxoWriter, so);
		writer.addDocument(toDocument(so));
	}

	@Override
	public int getCount() {

		Integer result = (Integer) doWithSearcher(new ActionInterface() {

			@Override
			public Object proceed(Object... args) throws Exception {
				IndexSearcher searcher = (IndexSearcher) args[0];
				return searcher.getIndexReader().numDocs();
			}
		});
		if (result == null) {
			return 0;
		} else {
			return result;
		}
	}

}
