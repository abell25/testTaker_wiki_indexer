package testTaker_wiki_indexer;

import java.io.*;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * A memory efficient parser for easy access to Wikipedia XML dumps in XML formats.<br>
 *
 * Typical pattern of use:<p>
 * <code>
 * WikipediaParser wxp = new WikipediaParser("enwiki-latest-pages-articles.xml");<br>
 * wxp.setPageCallback(...);<br>
 * wxp.parse();<br>
 * </code><p>
 * or<p>
 * <code>
 * WikiXMLDOMParser wxp = new WikiXMLDOMParser("enwiki-latest-pages-articles.xml");<br>
 * wxp.parse();<br>
 * WikiPageIterator it = wxp.getIterator();<br>
 * ...
 * </code>
 * @author Delip Rao
 *
 */
public class WikipediaParser {

    private BufferedReader wikiXMLBufferedReader;
    private DOMParser domParser = new DOMParser();
    private XMLReader xmlReader;
    private static String FEATURE_URI = "http://apache.org/xml/features/dom/defer-node-expansion";
    private Vector<WikiPage> pageList = null;

    public WikipediaParser(String file) throws Exception {
        wikiXMLBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"), 2 ^ 20);

        try {
            xmlReader = XMLReaderFactory.createXMLReader();
        } catch(SAXException ex) {
            ex.printStackTrace();
        }
    }

    //public WikipediaParser(InputStream stream){
    //    wikiXMLBufferedReader = new BufferedReader(new InputStreamReader(stream), 10);
    //}

    public void Parse(PageCallbackHandler pageHandler) throws Exception {
        SaxParse(pageHandler);
    }


    public void SaxParse(PageCallbackHandler pageHandler)  throws Exception {
        xmlReader.setContentHandler(new SAXPageCallbackHandler(pageHandler));
        xmlReader.parse(new InputSource(wikiXMLBufferedReader));
    }


    public void DomParse(PageCallbackHandler pageHandler)  throws Exception  {

        domParser.setProperty("http://apache.org/xml/properties/dom/document-class-name",
                              "org.apache.xerces.dom.DocumentImpl");
        domParser.setFeature(FEATURE_URI, true);

        domParser.parse(new InputSource(wikiXMLBufferedReader));

        Document doc = domParser.getDocument();
        NodeList pages = doc.getElementsByTagName("page");

        for(int i = 0; i < pages.getLength(); i++) {
            WikiPage wikiPage = new WikiPage();
            Node pageNode = pages.item(i);
            NodeList childNodes = pageNode.getChildNodes();
            for(int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if(child.getNodeName().equals("title"))
                    wikiPage.setTitle(child.getFirstChild().getNodeValue());
                else if(child.getNodeName().equals("id"))
                    wikiPage.setId(child.getFirstChild().getNodeValue());
                else if(child.getNodeName().equals("revision")) {
                    NodeList revchilds = child.getChildNodes();
                    for(int k = 0; k < revchilds.getLength(); k++) {
                        Node rchild = revchilds.item(k);
                        if(rchild.getNodeName().equals("text"))
                            wikiPage.setWikiText(rchild.getFirstChild().getNodeValue());
                    }
                }
            }

            pageHandler.process(wikiPage);
        }
    }
}


