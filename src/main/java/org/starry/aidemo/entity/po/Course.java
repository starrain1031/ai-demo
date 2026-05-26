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
 * <p>
 * Course table
 * </p>
 *
 * @author author
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Course name
     */
    @TableField("name")
    private String name;

    /**
     * Education requirement: 0-None, 1-Junior High School, 2-Senior High School, 3-College, 4-Bachelor Degree or above
     */
    @TableField("edu")
    private Integer edu;

    /**
     * Course type: Programming, Design, New Media, Other
     */
    @TableField("type")
    private String type;

    /**
     * Course price
     */
    @TableField("price")
    private Long price;

    /**
     * Study duration, unit: days
     */
    @TableField("duration")
    private Integer duration;


}
