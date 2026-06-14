package org.starry.aidemo.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Course reservation entity mapped to the course_reservation table.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course_reservation")
public class CourseReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Reserved course.
     */
    @TableField("course")
    private String course;

    /**
     * Student name.
     */
    @TableField("student_name")
    private String studentName;

    /**
     * Contact information.
     */
    @TableField("contact_info")
    private String contactInfo;

    /**
     * Reserved campus.
     */
    @TableField("school")
    private String school;

    /**
     * Additional reservation remark.
     */
    @TableField("remark")
    private String remark;


}
