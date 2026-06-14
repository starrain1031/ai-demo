package org.starry.aidemo.entity.vo;

import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import lombok.Data;

/**
 * Frontend-facing representation of a chat memory message.
 */
@NoArgsConstructor
@Data
public class MessageVO {

    /**
     * Message role expected by the frontend, such as user or assistant.
     */
    private String role;

    /**
     * Message text content.
     */
    private String content;

    /**
     * Converts a Spring AI message to a frontend view object.
     *
     * @param message Spring AI chat message
     */
    public MessageVO(Message message) {
        switch (message.getMessageType()){
            case USER:
                this.role = "user";
                break;
            case ASSISTANT:
                this.role = "assistant";
                break;
            default:
                this.role = "";
                break;
        }
        this.content = message.getText();
    }
}
