package com.whj.Mapper;

import com.whj.Pojo.Permissions;
import com.whj.Pojo.Role;
import com.whj.Pojo.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface UserMapper {


    //根据手机号查询用户信息
    @Select("select * from user where tel = #{tel}")
    public User findUser(String tel);

    //根据手机号查询用户信息，角色，权限
    @Select("select * from user where tel = #{tel}")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="username",property="username"),
            @Result(column="tel",property="tel"),
            @Result(column="password",property="password"),
            @Result(column="gender",property="gender"),
            @Result(column="age",property="age"),
            @Result(column="per_signature",property="per_signature"),
            @Result(column="address",property="address"),
            @Result(column="locked",property="locked"),
            @Result(column="create_time",property="create_time"),
            //查询角色
            @Result(column="id",property="roles",
                    many=@Many(select="findRoleByUser",
                            fetchType= FetchType.EAGER))
    })
    public User findUserByRoleByPer(String tel);

    //通过用户id查询所拥有角色
    @Select("select * from role where id in " +
            "(select role_id from user_role where user_id = #{id} )")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "role",property = "role"),
            @Result(column = "description",property = "description"),
            //查询权限
            @Result(column="id",property="permissions",
                    many=@Many(select="findPerByRole",
                            fetchType= FetchType.EAGER))
    })
    public List<Role> findRoleByUser(Integer id);

    //通过角色id查询所拥有权限
    @Select("select * from permissions where id in " +
            "(select permission_id from role_permission where role_id = #{id})")
    @Results({
            @Result(id = true,column = "id",property = "id"),
            @Result(column = "permission",property = "permission"),
            @Result(column = "description",property = "description")
    })
    public List<Permissions> findPerByRole(Integer id);

    //注册账号
    @Insert("insert into user(username,tel,password,gender,age," +
            "locked,create_time) values(#{username},#{tel},#{password},#{gender}," +
            "#{age},#{locked},#{create_time})")
    public Integer registerUser(User user);
    //注册时插入普通用户权限
    @Insert("insert into user_role values (#{id},#{role_id})")
    public Integer insertRole(@Param("id") Integer id, @Param("role_id") Integer role_id);

    //修改账号的状态
    @Update("update user set locked = #{locked} where tel = #{tel}")
    public Integer updateUserLock(User user);

    //重置密码
    @Update("update user set password = #{password} where tel = #{tel}")
    public Integer alterPWD(User user);

    //修改用户个人信息
    @Update("<script>" +
            "update user set username = #{username}  , age = #{age}, gender = #{gender}" +
            "<if test='per_signature != null'> , per_signature = #{per_signature}</if>" +
            "<if test='per_signature == null'> , per_signature = ''</if>" +
            "<if test='address != null'> , address = #{address}</if>" +
            "<if test='address == null'> , address = ''</if>" +
            "where id = #{id}" +
            "</script>")
    public Integer updateUser(User user);

    //查看用户信息
    @Select("select * from user where id = #{id}")
    public User user(int id);

    //修改用户地址
    @Update("update user set address =#{address} where id = #{id}")
    public int ModifyADS(User user);

}
