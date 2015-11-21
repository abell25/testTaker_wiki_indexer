package testTaker_wiki_indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class PageIndexer implements PageCallbackHandler {
    private IndexWriter writer;
    private Date start;
    private int count = 0;

    public PageIndexer(String indexDir) throws Exception {
        this.start = new Date();
        System.out.println("Starting indexing of wikipedia dump!");

        Path indexPath = Paths.get(indexDir);
        Directory dir = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        //Create a new index
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        this.writer = new IndexWriter(dir, iwc);
    }

    @Override
    public void process(WikiPage page) {
        try {
            if (!page.isReference()) {
                Document doc = new Document();

                Field titleField = new StringField("Title", page.Title, Field.Store.YES);
                Field textField = new TextField("Text", page.getCleanedText(), Field.Store.YES);

                doc.add(titleField);
                doc.add(textField);

                this.writer.addDocument(doc);

                this.count++;
                if(this.count % 1000 == 0){
                    Double rawSeconds = (new Date().getTime() - start.getTime()) / 1000.0;
                    int totalSeconds = rawSeconds.intValue();
                    int totalMinutes = totalSeconds / 60;
                    int totalHours = totalMinutes / 60;
                    String timeElapsed = String.format("%3d:%02d:%02d", totalHours, totalMinutes % 60, totalSeconds % 60);

                    System.out.println(String.format(timeElapsed + " %8d-15,901,127 pages processed!", this.count));
                }
            }
        }catch (Exception ex) {
            System.out.println("Processing wikipedia page failed!: " + ex.getClass());
            System.out.println("WikiPage: " + page.Title + ", Message: " + ex.getMessage());
        }
    }

    public void CloseIndex() throws Exception {
        //This should only be called for static indexes
        //it's a large 1 time cost that speeds up searching performance
        this.writer.forceMerge(1);

        this.writer.close();
        System.out.println("Completed index of wikipedia dump in " + (new Date().getTime() - this.start.getTime()) / 1000.0 + " seconds.");
    }
}
