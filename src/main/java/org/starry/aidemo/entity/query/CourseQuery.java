package org.starry.aidemo.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * Query object used by AI tool calls to search courses.
 */
@Data
public class CourseQuery {

    /**
     * Course type filter.
     */
    @ToolParam(required = false, description = "Course type")
    private String type;

    /**
     * Maximum education requirement accepted by the user.
     */
    @ToolParam(required = false, description = "Education requirement: 0-None, 1-Junior High School, 2-Senior High School, 3-College, 4-Bachelor Degree or above")
    private Integer edu;

    /**
     * Sort conditions requested by the model.
     */
    @ToolParam(required = false, description = "Sorting")
    private List<Sort> sorts;

    /**
     * Sort condition for course search.
     */
    @Data
    public static class Sort {

        /**
         * Sort field. Only price and duration are accepted by the tool layer.
         */
        @ToolParam(required = false, description = "Sorting field: price or duration")
        private String field;

        /**
         * Sort direction. True means ascending; false means descending.
         */
        private Boolean asc;
    }
}
