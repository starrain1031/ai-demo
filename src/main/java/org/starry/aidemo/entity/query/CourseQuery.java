package org.starry.aidemo.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class CourseQuery {
    @ToolParam(required = false, description = "Course type")
    private String type;

    @ToolParam(required = false, description = "Education requirement: 0-None, 1-Junior High School, 2-Senior High School, 3-College, 4-Bachelor Degree or above")
    private Integer edu;

    @ToolParam(required = false, description = "Sorting")
    private List<Sort> sorts;

    @Data
    public static class Sort {
        @ToolParam(required = false, description = "Sorting field: price or duration")
        private String field;
        private Boolean asc;
    }
}
