package testTaker_wiki_indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class TestIndexer {

	public TestIndexer() {}
	
	public void CreateIndex(String inDir, String indexDir) {
		
		try {
			Date start = new Date();
			System.out.println("Indexing directory: " + inDir);

			Path inPath = Paths.get(inDir);
			Path indexPath = Paths.get(indexDir);

			Directory dir = FSDirectory.open(indexPath);
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			//Create a new index
			iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);

			RecursivelyIndexFiles(writer, inPath);

			//This should only be called for static indexes
			//it's a large 1 time cost that speeds up searching performance
			writer.forceMerge(1);
			writer.close();

			Date end = new Date();
			System.out.println("Finished in " + (end.getTime() - start.getTime()) / 1000.0 + "seconds.");

		} catch(IOException e) {
			System.out.println("Index creation failed! Error occurred: " + e.getClass() + "\n  " + e.getMessage());
		}
	}

	public void RecursivelyIndexFiles(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						System.out.println("Visiting file: " + file.toString());
						IndexFile(writer, file);
					} catch (IOException ex) {
						System.out.println("File not indexed: " + file.toString());
						//don't index files that can't be read
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			//it's a file, so lets index it!
			IndexFile(writer, path);
		}
	}


	public void IndexFile(IndexWriter writer, Path file) throws IOException {
		System.out.println("Indexing " + file.toString());

		try(InputStream stream = Files.newInputStream(file)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

			Document doc = new Document();

			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
			doc.add(pathField);

			Field contentsField = new TextField("contents", reader);
			doc.add(contentsField);

			writer.addDocument(doc);
		}
	}


	public void SearchIndex(String queryString, String indexDir) throws IOException, ParseException {
		 SearchIndex(queryString, indexDir, null, 100);
	}

	public void SearchIndex(String queryString, String indexDir, String field, int resultsCount) throws IOException, ParseException {
		Path indexPath = Paths.get(indexDir);
		field = (field != null && !field.isEmpty()) ? field : "contents";

		IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser(field, analyzer);

		Query query = parser.parse(queryString);
		System.out.println("Searching for: " + query.toString(field));

		DoIndexQuery(searcher, query, resultsCount);

		reader.close();
	}

	public void DoIndexQuery(IndexSearcher searcher, Query query, int resultsLimit) throws IOException {
		TopDocs results = searcher.search(query, resultsLimit);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;

		int start = 0;
		int end = Math.min(numTotalHits, resultsLimit);

		for (int i = start; i < end; i++) {
			System.out.print(i + ": doc=" + hits[i].doc + " score=" + hits[i].score);

			Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("path");

			System.out.println(" path: " + path);
		}
	}


}
