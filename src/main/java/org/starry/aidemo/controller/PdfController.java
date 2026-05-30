package org.starry.aidemo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.starry.aidemo.Repository.ChatHistoryRepository;
import org.starry.aidemo.Repository.FileRepository;
import org.starry.aidemo.entity.vo.Result;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/pdf")
public class PdfController {

    private final FileRepository fileRepository;

    private final ChatClient pdfChatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/plain;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        if (!StringUtils.hasText(prompt)) {
            throw new ResponseStatusException
                    (HttpStatus.BAD_REQUEST, "Sorry, the prompt can not be null");
        }

        chatHistoryRepository.save("pdf", chatId);

        String fileKey = fileRepository.getFileKey(chatId);
        if (!StringUtils.hasText(fileKey)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please upload a PDF first");
        }

        chatHistoryRepository.save("pdf", chatId);

        return pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a
                        .param(CONVERSATION_ID, chatId)
                        .param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                                "file_key == '" + fileKey + "'"))
                .stream()
                .content();
    }

    @RequestMapping("/upload/{chatId}")
    public Result uploadPdf(@PathVariable String chatId, @RequestParam("file") MultipartFile file) {
        try {
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("Only PDF can be uploaded");
            }
            boolean success = fileRepository.save(chatId, file.getResource());
            if(! success) {
                return Result.fail("Failed to save the file");
            }
            return Result.ok();
        } catch (Exception e) {
            log.error("Failed to upload PDF.", e);
            return Result.fail("Failed to upload！");
        }
    }

    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) {

        Resource resource = fileRepository.getFile(chatId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}