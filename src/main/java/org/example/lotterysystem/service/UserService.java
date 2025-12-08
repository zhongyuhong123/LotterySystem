package org.example.lotterysystem.service;

import org.example.lotterysystem.controller.param.UserLoginParam;
import org.example.lotterysystem.controller.param.UserRegisterParam;
import org.example.lotterysystem.service.dto.UserDTO;
import org.example.lotterysystem.service.dto.UserLoginDTO;
import org.example.lotterysystem.service.dto.UserRegisterDTO;
import org.example.lotterysystem.service.enums.UserIdentityEnum;

import java.util.List;

public interface UserService {

    //注册
    UserRegisterDTO register(UserRegisterParam param);

    /**
     * 用户登录  service层用DTO结尾
     * @param param
     * @return
     */
    UserLoginDTO login(UserLoginParam param);

    /**
     *  根据身份查询人员列表
     * @param forName: 如果为空，查询各个身份人员列表
     * @return
     */
    List<UserDTO> findUserInfo(UserIdentityEnum identity);
}
