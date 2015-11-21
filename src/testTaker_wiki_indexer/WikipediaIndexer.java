package testTaker_wiki_indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class WikipediaIndexer {
    public String indexPath;

    public WikipediaIndexer(String indexPath) {
        this.indexPath = indexPath;
    }

    public void CreateIndex(String wikiXmlFile) throws Exception {
        PageIndexer pageIndexer = new PageIndexer(this.indexPath);
        WikipediaParser wikipediaParser = new WikipediaParser(wikiXmlFile);

        PageCallbackHandler pageCallbackHandler = pageIndexer;
        wikipediaParser.Parse(pageCallbackHandler);

        pageIndexer.CloseIndex();
    }

    public void SearchIndex(String queryString) throws IOException, ParseException {
        SearchIndex(queryString, null, 100);
    }

    public void SearchIndex(String queryString, String field, int resultsCount) throws IOException, ParseException {
        Path indexPath = Paths.get(this.indexPath);
        field = (field != null && !field.isEmpty()) ? field : "Text";

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

            String title = doc.get("Title");
            String text  = doc.get("Text");

            System.out.println(" title: " + title);

            int stringLength = 72;
            String queryString = query.toString("Text");
            int index = text.indexOf(queryString);
            int startIndex = Math.max(0, index - (stringLength / 2));
            int endIndex   = Math.min(text.length(), index + (stringLength / 2));
            String textPart = text.substring(startIndex, endIndex);
            textPart = textPart.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');

            System.out.println("text: \"" + textPart + "\"");
        }
    }

}
