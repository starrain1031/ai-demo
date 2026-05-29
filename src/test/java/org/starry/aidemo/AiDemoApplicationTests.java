package org.starry.aidemo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


import java.util.Arrays;
import java.util.List;

@SpringBootTest
class AiDemoApplicationTests {

    @Autowired
    private OpenAiEmbeddingModel embeddingModel;
    @Autowired
    private VectorStore vectorStore;

    @Test
    void contextLoads() {
        float[] embeddedMsg = embeddingModel.embed("Keep coding, keep learning");
        System.out.println(Arrays.toString(embeddedMsg));
    }

    @Test
    public void testVectorStore() {
        Resource resource = new FileSystemResource("javaTips.pdf");
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documents = reader.read();
        documents.forEach(doc -> {
            doc.getMetadata().put("file_key", "javaTips_pdf");
        });
        vectorStore.add(documents);
        SearchRequest request = SearchRequest.builder()
                .query("接口和实现类的命名有哪两套规则")
                .topK(5)
                .similarityThreshold(0.6)
                .filterExpression("file_key == 'javaTips_pdf'")
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);
        for (Document doc : docs) {
            System.out.println(doc.getId());
            System.out.println(doc.getScore());
            System.out.println(doc.getText());
        }
    }
}
