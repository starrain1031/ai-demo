package org.starry.aidemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.starry.aidemo.entity.po.CourseReservation;
import org.starry.aidemo.mapper.CourseReservationMapper;
import org.starry.aidemo.service.ICourseReservationService;

/**
 * Default MyBatis-Plus service implementation for course reservations.
 */
@Service
public class CourseReservationServiceImpl extends ServiceImpl<CourseReservationMapper, CourseReservation> implements ICourseReservationService {

}
