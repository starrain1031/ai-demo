package org.starry.aidemo.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple API result wrapper used by non-streaming endpoints.
 */
@Data
@NoArgsConstructor
public class Result {

    /**
     * Operation status: 1 for success, 0 for failure.
     */
    private Integer ok;

    /**
     * Human-readable result message.
     */
    private String msg;

    private Result(Integer ok, String msg) {
        this.ok = ok;
        this.msg = msg;
    }

    /**
     * Creates a successful result.
     *
     * @return success result
     */
    public static Result ok() {
        return new Result(1, "ok");
    }

    /**
     * Creates a failed result with a message.
     *
     * @param msg failure message
     * @return failed result
     */
    public static Result fail(String msg) {
        return new Result(0, msg);
    }
}
