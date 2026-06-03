package com.wms.mapper;

import com.wms.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

public interface UserMapper {

    int insert(User record);

    int updateById(User record);

    User selectById(Long id);

    @Select("SELECT * FROM wms_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Update("UPDATE wms_user SET last_login_time = #{loginTime}, last_login_ip = #{loginIp} WHERE id = #{id}")
    int updateLoginInfo(@Param("id") Long id, @Param("loginTime") Date loginTime, @Param("loginIp") String loginIp);

    @Update("UPDATE wms_user SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_user")
    int selectCount();
}
