package mg.maos;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.junit.After;

public class LuceneStorageTest extends StorageTest {

	@Override
	protected StorageServiceInterface getStorageWithFacets() {
		return new LuceneStorage(new RAMDirectory(), new RAMDirectory());
	}

	@Override
	protected StorageServiceInterface getStorage() {
		return new LuceneStorage(new RAMDirectory());
	}

	@Override
	protected LuceneStorage getStorageForFind() throws IOException {
		return new LuceneStorage(new SimpleFSDirectory(getTestDirLocation()));
	}

	private File getTestDirLocation() {
		return new File(System.getProperty("temp.dir") + "/dir");
	}

	private void deleteDir(File dir) {
		if(dir.isDirectory()) {
			for(File f : dir.listFiles()) {
				deleteDir(f);
			}
		}
		dir.delete();
	}
	
	@After
	public void after() {
		deleteDir(getTestDirLocation());
	}

}
