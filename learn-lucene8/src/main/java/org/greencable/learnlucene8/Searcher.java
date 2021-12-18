package org.greencable.learnlucene8;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    public static void main(String[] args) throws IOException, ParseException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java " + Searcher.class.getName()
                + " <index dir>");
        }
        String indexDir = args[0];
        String q = "+me -him";

        search(indexDir, q);
    }

    public static void search(String indexDir, String queryStr) throws IOException, ParseException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));

        QueryParser queryParser = new QueryParser("contents", new StandardAnalyzer());
        Query query = queryParser.parse(queryStr);

        long start = System.currentTimeMillis();
        TopDocs hits = is.search(query, 10);
        long end = System.currentTimeMillis();

        System.err.printf("Found %d document(s) (in %d ms) that matched query '%s':%n",
                hits.totalHits.value, end-start, queryStr);

        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullpath"));
        }
    }
}
