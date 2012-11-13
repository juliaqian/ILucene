package com.ilucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * index the properties of a files:
 * content,
 * fullPath
 * fileName
 * @author JohnLiu
 *
 */
public class OneIndex {
	
	private static Logger logger = LoggerFactory.getLogger(OneIndex.class);
	private IndexWriter writer;
	static String indexDir ;
	static String dataDir;
	static{
		indexDir = OneIndex.class.getClassLoader().getResource("index").getPath();
		dataDir = OneIndex.class.getClassLoader().getResource("index/data").getPath();
		if(!System.getProperty("file.separator").equals("/")){ // windows os
			indexDir = indexDir.substring(1).replaceAll("/", "\\\\\\\\");
			dataDir = dataDir.substring(1).replaceAll("/", "\\\\\\\\");
		}
		logger.info("---index directory location: " + indexDir);
	}
	public OneIndex(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir, 
								 new StandardAnalyzer(Version.LUCENE_30),
							     true,
								 IndexWriter.MaxFieldLength.UNLIMITED
								);
	}
	
	
	public int index(String dataDir, FileFilter filter)throws Exception {
		File[] files = new File(dataDir).listFiles();
			for (File f: files) {
				if (!f.isDirectory() && !f.isHidden() &&
				    f.exists() && f.canRead() &&
				    (filter == null || filter.accept(f))) {
						indexFile(f);
				 }
			}
			return writer.numDocs();
	}
	
	public void close() throws IOException {
		writer.close();
	}
	
	private void indexFile(File f) throws Exception {
		logger.info("Indexing " + f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);
	}
	
	private static class TextFilesFilter implements FileFilter {
		public boolean accept(File path) {
			return path.getName().toLowerCase().endsWith(".txt");
		}
	}
		
	protected Document getDocument(File f) throws Exception {
			Document doc = new Document();
			doc.add(new Field("contents",  new FileReader(f)));
			doc.add(new Field("fileName",  f.getName(),
							   Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("fullPath", f.getCanonicalPath(),
							   Field.Store.YES, Field.Index.NOT_ANALYZED));
			return doc;
    }
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/**
		 * It is a option to run the code with ant command or 
		 * directly java command.
		 */
//			if (args.length != 2) {
//				throw new IllegalArgumentException("Usage: java " +
//									OneIndex.class.getName()
//									+ " <index dir> <data dir>");
//			}
//			String indexDir = args[0];
//			String dataDir = args[1];

		long start = System.currentTimeMillis();
		OneIndex indexer = new OneIndex(indexDir);
		int numIndexed;
		try {
			numIndexed = indexer.index(dataDir, new TextFilesFilter());
		} finally {
			indexer.close();
		}
		logger.info("Indexing " + numIndexed + " files took "
								+ (System.currentTimeMillis() - start) +
								" milliseconds");
	}

}
