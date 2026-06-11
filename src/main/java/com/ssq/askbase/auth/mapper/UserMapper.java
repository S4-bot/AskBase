package com.ssq.askbase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ssq.askbase.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
