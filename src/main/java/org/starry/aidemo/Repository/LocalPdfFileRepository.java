package org.starry.aidemo.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Local PDF repository that persists uploaded files and indexes their pages in a vector store.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPdfFileRepository implements FileRepository{

    private final VectorStore vectorStore;
    private static final String FILE_KEY_SUFFIX = ".file_key";
    //id - file,id - file_key
    private final Properties chatFiles = new Properties();

    @Value("${app.pdf.upload-dir:./data/pdf}")
    private String uploadDir;

    /**
     * Saves a PDF, records its mapping, and refreshes its vector-store documents.
     *
     * @param chatId conversation identifier that owns the PDF
     * @param resource uploaded PDF resource
     * @return true if the file and vectors were saved successfully
     */
    @Override
    public boolean save(String chatId, Resource resource) {
        String filename = resource.getFilename();
        String fileKey = fileKey(chatId, Objects.requireNonNull(filename));
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = uploadPath.resolve(fileKey + ".pdf").normalize();

        if (!target.startsWith(uploadPath)) {
            log.error("Invalid PDF target path: {}", target);
            return false;
        }

        try {
            Files.createDirectories(uploadPath);
            Files.copy(resource.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save PDF resource.", e);
            return false;
        }

        // Save mapping in properties
        chatFiles.put(chatId, target.toString());
        chatFiles.put(chatId + FILE_KEY_SUFFIX, fileKey);

        // attach fileKey to the document
        List<Document> documents = readPdf(target.toFile());
        documents.forEach(doc -> doc.getMetadata().put("file_key", fileKey));

        //add to vector store
        vectorStore.delete("file_key == '" + fileKey + "'");
        vectorStore.add(documents);
        return true;
    }

    /**
     * Resolves the local PDF file associated with the conversation.
     *
     * @param chatId conversation identifier
     * @return local PDF resource, or a missing resource placeholder
     */
    @Override
    public Resource getFile(String chatId) {
        String filePath = chatFiles.getProperty(chatId);
        if (filePath == null) {
            return new FileSystemResource("__missing_pdf__");
        }
        return new FileSystemResource(filePath);
    }

    /**
     * Returns the metadata key used to filter this conversation's PDF chunks.
     */
    @Override
    public String getFileKey(String chatId) {
        return chatFiles.getProperty(chatId + FILE_KEY_SUFFIX);
    }

    /**
     * Reads a PDF into one document per page for vector indexing.
     */
    private List<Document> readPdf(File file) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                new FileSystemResource(file),
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        return reader.read();
    }

    /**
     * Creates a stable, safe key from the conversation id and original filename.
     */
    private String fileKey(String chatId, String filename) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((chatId + ":" + filename).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    /**
     * Loads persisted chat-to-file mappings when the application starts.
     */
    @PostConstruct
    private void init() {
        FileSystemResource pdfResource = new FileSystemResource("chat-pdf.properties");
        if (pdfResource.exists()) {
            try {
                chatFiles.load(new BufferedReader(new InputStreamReader(pdfResource.getInputStream(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Persists chat-to-file mappings before the application shuts down.
     */
    @PreDestroy
    private void persistent() {
        try {
            chatFiles.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
