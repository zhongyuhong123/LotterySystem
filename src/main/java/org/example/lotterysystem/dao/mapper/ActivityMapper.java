package org.example.lotterysystem.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.lotterysystem.dao.dataobject.ActivityDO;

import java.util.List;

@Mapper
public interface ActivityMapper {

    @Insert("insert into activity (activity_name, description, status)" +
            " values (#{activityName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id",keyColumn = "id")
    int insert(ActivityDO activityDO);

    @Select("select count(1) from activity")
    int count();

    @Select(("select * from activity order by id desc limit #{offset}, #{pageSize}"))
    List<ActivityDO> selectActivityList(@Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);


    @Select("select  * from activity where id =#{id}")
    ActivityDO selectById(@Param("id") Long id);

    @Update("update activity set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") Long activityId,@Param("status") String name);
}
