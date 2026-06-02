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
import redis.clients.jedis.JedisPooled;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }
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
    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }

    @Bean
    public RedisVectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
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