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
	//TODO: set these to the locations on your computer
	public static String indexPath = "C:/Users/anthony/PycharmProjects/testTaker_wiki_indexer/indexes/wikipedia";
	public static String wikipediaXmlDump = "C:/Users/anthony/PycharmProjects/testTaker_wiki_indexer/data/wikipedia_first100k_entries_only.xml";

	public static void main(String[] args) throws Exception {
		System.out.println("Started!");
		
		CreateWikipediaFilesIndex();
		
		System.out.println("Testing that the index is working property by querying it..");
		SearchWikipediaFilesIndex("cell");

		System.out.println("Completed!");

	}

	public static void SearchWikipediaFilesIndex(String queryString) throws Exception {
		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.SearchIndex(queryString);
	}

	public static void CreateWikipediaFilesIndex() throws Exception {
		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(indexPath);
		wikipediaIndexer.CreateIndex(wikipediaXmlDump);
	}

}


