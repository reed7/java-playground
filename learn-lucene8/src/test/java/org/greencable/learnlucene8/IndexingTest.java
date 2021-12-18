package org.greencable.learnlucene8;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IndexingTest {

    protected String[] ids = {"1", "2"};
    protected String[] unindexed = {"Netherlands", "Italy"};
    protected String[] unstored = {"Amsterdam has lots of bridges",
                                   "Venice has lots of canals"};
    protected String[] text = {"Amsterdam", "Venice"};

    private Directory directory;

    @BeforeEach
    protected void setUp() throws Exception {
        directory = new ByteBuffersDirectory();

        try(IndexWriter writer = getWriter()) {
            for (var i = 0; i < ids.length; i++) {
                Document doc = new Document();
                doc.add(new StringField("id", ids[i], Field.Store.YES));
                doc.add(new StringField("country", unindexed[i], Field.Store.YES));
                doc.add(new TextField("contents", unstored[i], Field.Store.NO));
                doc.add(new TextField("city", text[i], Field.Store.YES));
                writer.addDocument(doc);
            }
        }
    }

    @Test
    public void testIndexWriter() throws IOException {
        IndexWriter writer = getWriter();
        Assertions.assertEquals(ids.length, writer.getPendingNumDocs());
    }

    @Test
    public void testIndexReader() throws IOException {
        IndexReader reader = DirectoryReader.open(directory);
        Assertions.assertEquals(ids.length, reader.maxDoc());
        Assertions.assertEquals(ids.length, reader.numDocs());
        reader.close();
    }

    @Test
    public void testTermQuery() throws Exception {
        Assertions.assertEquals(1, getTermQueryHitCount("id", "1"));
        Assertions.assertEquals(1, getTermQueryHitCount("country", "Italy"));
    }

    @Test
    public void testQuery() throws Exception {
        Assertions.assertEquals(1, getHitCount("contents", "Amsterdam AND (has lots of)"));
        Assertions.assertEquals(1, getHitCount("id", "1"));
    }

    @Test
    public void testDelB4Optimize() throws Exception {
        try(IndexWriter writer = getWriter()) {
            writer.deleteDocuments(new Term("id", "1"));
            Assertions.assertTrue(writer.hasDeletions());
            Assertions.assertEquals(1, getTermQueryHitCount("id", "1"));
        }

        Assertions.assertEquals(0, getTermQueryHitCount("id", "1"));
        Assertions.assertEquals(1, getTermQueryHitCount("id", "2"));
    }

    @Test
    public void testUpdateDoc() throws Exception {
        try(IndexWriter writer = getWriter()) {
            Document doc = new Document();
            doc.add(new StringField("id", "1", Field.Store.YES));
            doc.add(new TextField("new_field", "Reed", Field.Store.YES));
            writer.updateDocument(new Term("id", "1"), doc);
            writer.commit();

            Assertions.assertEquals(1, getTermQueryHitCount("id", "1"));
            Assertions.assertEquals(1, getHitCount("new_field", "Reed"));
            Assertions.assertEquals(0, getHitCount("contents", "Amsterdam AND (has lots of)"));
            Assertions.assertEquals(1, getHitCount("city", "Amsterdam"));
        }
    }

    private IndexWriter getWriter() throws IOException {
        return new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
    }

    protected long getTermQueryHitCount(String fieldName, String searchString) throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        Query query = new TermQuery(new Term(fieldName, searchString));

        return searcher.search(query, 1).totalHits.value;
    }

    protected long getHitCount(String fieldName, String searchString) throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        QueryParser queryParser = new QueryParser(fieldName, new StandardAnalyzer());
        Query query = queryParser.parse(searchString);

        return searcher.search(query, 1).totalHits.value;
    }
}
