package com.assistant.core.mapper;

import com.assistant.core.entity.UserConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户配置Mapper
 */
@Mapper
public interface UserConfigMapper extends BaseMapper<UserConfig> {
}
