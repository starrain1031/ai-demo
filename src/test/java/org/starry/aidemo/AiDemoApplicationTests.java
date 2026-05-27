package org.starry.aidemo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class AiDemoApplicationTests {

    @Autowired
    private OpenAiEmbeddingModel embeddingModel;

    @Test
    void contextLoads() {
        float[] embeddedMsg = embeddingModel.embed("Keep coding, keep learning");
        System.out.println(Arrays.toString(embeddedMsg));
    }

}
