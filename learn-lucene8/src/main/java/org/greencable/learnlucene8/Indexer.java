package org.greencable.learnlucene8;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Indexer implements Closeable {
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            throw new IllegalArgumentException("Usage: java " +
                    Indexer.class.getName() + " <index dir> <data dir>");
        }

        String indexDir = args[0];
        String dataDir = args[1];


        int numIndexed;
        try (Indexer indexer = new Indexer(indexDir);) {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        }
    }

    final private IndexWriter writer;

    public Indexer(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(dir, config);
    }

    @Override
    public void close() throws IOException {
        long start = System.currentTimeMillis();
        writer.close();
        long end = System.currentTimeMillis();
        System.out.println("Indexing files took " + (end - start) + "ms");
    }

    public int index(String dataDir, final FileFilter filter) throws IOException {
        File[] files = new File(dataDir).listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No file found in " + dataDir);
            return 0;
        }

        Stream<File> stream = Stream.of(files)
                .filter(f -> !f.isDirectory())
                .filter(f -> !f.isHidden())
                .filter(File::exists)
                .filter(File::canRead);

        if (filter != null) {
            stream = stream.filter(filter::accept);
        }

        Set<Document> docs = stream
                .map(this::getDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        writer.addDocuments(docs);

        return docs.size();
    }

    private static class TextFilesFilter implements FileFilter {
        @Override
        public boolean accept(File path) {
            return path.getName().toLowerCase().endsWith(".txt");
        }
    }

    protected Document getDocument(File f) {
        try {
            System.out.println("Indexing " + f.getCanonicalPath());
            Document doc = new Document();
            doc.add(new TextField("contents", new FileReader(f)));
            doc.add(new TextField("filename", f.getName(), Field.Store.YES));
            doc.add(new TextField("fullpath", f.getCanonicalPath(), Field.Store.YES));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
