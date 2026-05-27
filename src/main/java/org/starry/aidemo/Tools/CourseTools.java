package org.starry.aidemo.Tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.starry.aidemo.entity.po.Course;
import org.starry.aidemo.entity.po.CourseReservation;
import org.starry.aidemo.entity.po.School;
import org.starry.aidemo.entity.query.CourseQuery;
import org.starry.aidemo.service.ICourseReservationService;
import org.starry.aidemo.service.ICourseService;
import org.starry.aidemo.service.ISchoolService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {

    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService reservationService;

    @Tool(description="query courses based on conditions")
    public List<Course> queryCourses(CourseQuery query) {
        if (query == null){
            return List.of();
        }

        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());//edu<=2
        if (query.getSorts() != null && !query.getSorts().isEmpty()){
            for (CourseQuery.Sort sort: query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }
        return wrapper.list();
    }
    @Tool(description="query all schools")
    public List<School> queryAllSchools() {
        return schoolService.list();
    }

    @Tool(description="reserve a course")
    public Integer reserveCourse(@ToolParam(description = "course") String course,
                                 @ToolParam(description = "student name") String studentName,
                                 @ToolParam(description = "contact info") String contactInfo,
                                 @ToolParam(description = "school") String school,
                                 @ToolParam(description = "remark", required = false) String remark) {
        CourseReservation reservation = new CourseReservation();
        reservation.setCourse(course)
                .setStudentName(studentName)
                .setContactInfo(contactInfo)
                .setSchool(school)
                .setRemark(remark);
        reservationService.save(reservation);
        return reservation.getId();
    }
}
