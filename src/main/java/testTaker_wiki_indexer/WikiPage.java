package testTaker_wiki_indexer;

public class WikiPage {
    public String Title;
    public String Id;

    public String Text;

    private WikiTextParser wikiTextParser;

    public void setTitle(String title) {
        this.Title = title;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public void setWikiText(String text) {
        this.Text = text;
        wikiTextParser = new WikiTextParser(text);
    }

    public String getCleanedText() {
        return wikiTextParser.getPlainText();
    }

    public Boolean isContentPage() { return !wikiTextParser.isRedirect() &&
                                            !wikiTextParser.isDisambiguationPage() &&
                                            !wikiTextParser.isStub();
    }

}
