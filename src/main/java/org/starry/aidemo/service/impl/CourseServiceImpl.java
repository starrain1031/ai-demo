package org.starry.aidemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.starry.aidemo.entity.po.Course;
import org.starry.aidemo.mapper.CourseMapper;
import org.starry.aidemo.service.ICourseService;

/**
 * Default MyBatis-Plus service implementation for courses.
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

}
