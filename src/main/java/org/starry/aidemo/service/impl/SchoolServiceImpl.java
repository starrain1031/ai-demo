package org.starry.aidemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.starry.aidemo.entity.po.School;
import org.starry.aidemo.mapper.SchoolMapper;
import org.starry.aidemo.service.ISchoolService;

/**
 * Default MyBatis-Plus service implementation for school campuses.
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements ISchoolService {

}
