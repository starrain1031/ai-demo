package org.starry.aidemo.config;


import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.starry.aidemo.Tools.CourseTools;
import org.starry.aidemo.constants.SystemConstants;
import redis.clients.jedis.RedisClient;

/**
 * Defines shared Spring AI clients, chat memory, and vector-store infrastructure.
 */
@Configuration
public class CommonConfiguration {

    /**
     * Creates the in-memory message window used by all chat clients.
     *
     * @return chat memory that keeps the latest conversation messages
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    /**
     * Creates the default chat client for general chat and multimodal chat.
     *
     * @param model auto-configured chat model
     * @param chatMemory shared chat memory
     * @return default chat client
     */
    @Bean
    public ChatClient chatClient(ChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                //OpenAiChatOptions is necessary
                .defaultOptions(OpenAiChatOptions.builder().model("qwen3.5-omni-flash"))
//                .defaultSystem("")
                .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * Creates the role-play game chat client with a fixed system prompt.
     *
     * @param model auto-configured chat model
     * @param chatMemory shared chat memory
     * @return game chat client
     */
    @Bean
    public ChatClient gameChatClient(ChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * Creates the customer-service chat client with course consultation tools.
     *
     * @param model auto-configured chat model
     * @param chatMemory shared chat memory
     * @param courseTools Spring AI tools for course search and reservation
     * @return customer-service chat client
     */
    @Bean
    public ChatClient serviceChatClient(ChatModel model, ChatMemory chatMemory, CourseTools courseTools) {
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(courseTools)
                .build();
    }

    /**
     * Creates the Redis client used by the vector store.
     *
     * @return pooled Redis client
     */
    @Bean
    public RedisClient redisClient() {
        return RedisClient.builder()
                .hostAndPort("localhost", 6379)
                .build();
    }

    /**
     * Creates the Redis vector store for PDF embeddings.
     *
     * @param redisClient Redis client
     * @param embeddingModel embedding model used to vectorize documents
     * @return Redis-backed vector store
     */
    @Bean
    public RedisVectorStore vectorStore(RedisClient redisClient, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(redisClient, embeddingModel)
                .indexName("spring-ai-index")
                .prefix("doc")
                .initializeSchema(true)
                // Because RedisVectorStore save tags like this: @file_name:{javaTips\.pdf}
                // we need to create a file_key instead
//                .metadataFields(
//                        RedisVectorStore.MetadataField.tag("file_name")
//                )
                .metadataFields(
                        RedisVectorStore.MetadataField.tag("file_key")
                )
                .build();
    }

    /**
     * Creates the PDF chat client with retrieval-augmented generation.
     *
     * @param model auto-configured chat model
     * @param chatMemory shared chat memory
     * @param vectorStore vector store containing indexed PDF pages
     * @return PDF RAG chat client
     */
    @Bean
    public ChatClient pdfChatClient(ChatModel model, ChatMemory chatMemory, RedisVectorStore vectorStore) {
        return ChatClient
                .builder(model)
//                .defaultSystem(SystemConstants.PDF_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(),
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.6)
                                        .topK(2)
                                        .build())
                                .build()

                )
                .build();
    }
}
