/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javafx.scene.control.TextArea;

public class mediaIndexer extends mediaPlayerApp {
	protected static ArrayList<File> playlist = new ArrayList<>();

	public static void IndexFiles(String index, String docsPath, TextArea results, boolean CreateOrUpdate,
			boolean removeFiles) throws IOException {
		String indexPath = index;
		boolean create = CreateOrUpdate;
		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir))
			results.appendText("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path" + "\n");
		Date start = new Date();
		try {
			if (removeFiles)
				results.appendText("Deleting '" + docsPath + "' from directory '" + indexPath + "'..." + "\n");
			// else results.appendText("results '" + docsPath + "' to directory
			// '" + indexPath + "'..." + "\n");
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			if (create)
				iwc.setOpenMode(OpenMode.CREATE);
			else
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			// Optional: for better results performance, if you are results many
			// documents, increase the RAM buffer.
			// But if you do this, increase the max heap size to the JVM (eg add
			// -Xmx512m or -Xmx1g):
			// iwc.setRAMBufferSizeMB(2048.0);
			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir, results, removeFiles);
			writer.close();
			Date end = new Date();
			long diffInSeconds = (end.getTime() - start.getTime()) / 1000;
			results.appendText(diffInSeconds + " total seconds" + "\n");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
	}

	public static void indexDocs(final IndexWriter writer, Path path, TextArea results, boolean removeFiles)
			throws IOException, SAXException, TikaException {
		boolean delFiles = removeFiles;
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						if (delFiles) {
							writer.deleteDocuments(new Term("path", file.toString()));
						} else {
							indexDoc(writer, file, results, attrs.lastModifiedTime().toMillis());
						}
					} catch (IOException ignore) {
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (TikaException e) {
						e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else if (delFiles) {
			writer.deleteDocuments(new Term("path", path.toString()));
		} else {
			indexDoc(writer, path, results, Files.getLastModifiedTime(path).toMillis());
		}
	}

	/**
	 * Indexes a single document
	 * 
	 * @throws TikaException
	 * @throws SAXException
	 */
	public static void indexDoc(IndexWriter writer, Path file, TextArea results, long lastModified)
			throws IOException, SAXException, TikaException {
		AutoDetectParser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		try (InputStream stream = Files.newInputStream(file)) {
			parser.parse(stream, handler, metadata);
			Document doc = new Document();
			String[] metadataNames = metadata.names();
			for (String name : metadataNames)
				doc.add(new TextField(name, metadata.get(name), Field.Store.YES));
			doc.add(new StringField("path", file.toString(), Field.Store.YES));
			doc.add(new LongPoint("modified", lastModified));
			results.appendText("Title: " + metadata.get("title") + "\n");
			results.appendText("Artists: " + metadata.get("xmpDM:artist") + "\n");
			results.appendText("Genre: " + metadata.get("xmpDM:genre") + "\n");
			results.appendText("Year: " + metadata.get("xmpDM:releaseDate") + "\n");
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can
				// be there):
				results.appendText("adding " + file + "\n");
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been
				// indexed):
				results.appendText("updating " + file);
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
		}
	}

	public static void SearchFiles(String index, String queryString, String selected, TextArea results)
			throws IOException, ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		QueryParser parser = new QueryParser(selected, analyzer);
		String line = queryString != null ? queryString : in.readLine();
		line = line.trim();
		Query query = parser.parse(line);
		int maxHits = 100;
		TopDocs docsResults = searcher.search(query, maxHits);
		ScoreDoc[] hits = docsResults.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			results.appendText("Title: " + doc.get("title") + "\n");
			results.appendText("Artists: " + doc.get("xmpDM:artist") + "\n");
			results.appendText("Genre: " + doc.get("xmpDM:genre") + "\n");
			results.appendText("Year: " + doc.get("xmpDM:releaseDate") + "\n");
		}
		// Playlist.
		playlist.clear();
		for (int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("path");
			if (path != null)
				playlist.add(new File(path));
		}
		reader.close();
	}
}