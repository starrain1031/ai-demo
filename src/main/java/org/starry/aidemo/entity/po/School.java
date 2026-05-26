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
 * Campus table
 * </p>
 *
 * @author author
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("school")
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Campus name
     */
    @TableField("name")
    private String name;

    /**
     * City where the campus is located
     */
    @TableField("city")
    private String city;


}
