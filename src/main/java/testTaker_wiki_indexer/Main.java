package testTaker_wiki_indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;


public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("Started!");
		
		CreateWikipediaFilesIndex();
		SearchWikipediaFilesIndex("cell");

		System.out.println("Completed!");

	}

	public static void SearchWikipediaFilesIndex(String queryString) throws Exception {
		String indexPath        = "/media/tony/ssd/TestTakerData/wikipedia_index";

		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.SearchIndex(queryString);
	}

	public static void CreateWikipediaFilesIndex() throws Exception {
		String wikipediaXmlDump = "/media/tony/ssd/TestTakerData/enwiki-20150901-pages-articles.xml";
		String indexPath        = "/media/tony/ssd/TestTakerData/wikipedia_index";
		//wikipediaXmlDump        = "/media/tony/ssd/temp/first_100k.xml";

		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.CreateIndex(wikipediaXmlDump);
	}

	public static void SearchWikipediaBooksIndex(String queryString) throws Exception {
		String indexPath = "/media/tony/ssd/TestTakerData/wikipedia_books_index";

		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.SearchIndex(queryString);
	}

	public static void CreateWikipediaBooksIndex() throws Exception {
		String wikipediaXmlDump = "/media/tony/ssd/TestTakerData/enwikibooks-20151102-pages-articles.xml";
		String indexPath        = "/media/tony/ssd/TestTakerData/wikipedia_books_index";

		//wikipediaXmlDump = "/home/tony/code/python/notebooks/MyLib/TestTaker/enwikibooks-20151102-pages-articles.xml";
		//indexPath = "/home/tony/code/python/notebooks/MyLib/TestTaker/wikipedia_books_index";

		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.CreateIndex(wikipediaXmlDump);
	}


	public static void CreateSimpleFilesIndex() {
		String inDir = "/home/tony/temp/some_files/";
		String indexDir = "/home/tony/temp/index";
		String query = "simple";

		inDir = "/media/tony/ssd/temp/some_files";
		indexDir = "/media/tony/ssd/temp/index";

		try {
			Files.walkFileTree(Paths.get(inDir), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					//System.out.println("Visiting file: " + file.toString());
					return FileVisitResult.CONTINUE;
				}
			});

		}catch(IOException ex) {

		}

		TestIndexer testIndexer = new TestIndexer();
		testIndexer.CreateIndex(inDir, indexDir);
		System.out.println("****************** index created! *****************");

		try {
			testIndexer.SearchIndex(query, indexDir);
		} catch (Exception e) {
			System.out.println("Exception occurred!: " + e.getClass() + "\n    message: " + e.getMessage());
		}

	}
}


