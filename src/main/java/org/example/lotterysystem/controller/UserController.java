package org.example.lotterysystem.controller;


import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import org.example.lotterysystem.common.exception.ControllerException;
import org.example.lotterysystem.common.pojo.CommonResult;
import org.example.lotterysystem.controller.param.ShortMessageLoginParam;
import org.example.lotterysystem.controller.param.UserPasswordLoginParam;
import org.example.lotterysystem.controller.param.UserRegisterParam;
import org.example.lotterysystem.controller.result.BaseUserInfoResult;
import org.example.lotterysystem.controller.result.UserLoginResult;
import org.example.lotterysystem.controller.result.UserRegisterResult;
import org.example.lotterysystem.service.UserService;
import org.example.lotterysystem.service.VerificationCodeService;
import org.example.lotterysystem.service.dto.UserDTO;
import org.example.lotterysystem.service.dto.UserLoginDTO;
import org.example.lotterysystem.service.dto.UserRegisterDTO;
import org.example.lotterysystem.service.enums.UserIdentityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 注册
     * @param param
     * @return
     */
    @RequestMapping("/register")
    public CommonResult<UserRegisterResult> userRegister(
            @Validated @RequestBody UserRegisterParam param){
        //日志打印
        logger.info("userRegister UserRegisterParam{}", JacksonUtil.writeValueAsString(param));
        //调用Service层的服务
        UserRegisterDTO userRegisterDTO = userService.register(param);
        return CommonResult.success(convertToUserRegisterResult(userRegisterDTO));
    }

    /**
     *
     * @param param
     * @return
     */
    @RequestMapping("/password/login")
    public CommonResult<UserLoginResult> userPasswordLogin(
            @Validated @RequestBody UserPasswordLoginParam param){
        logger.info("userPasswordLogin UserPasswordLoginParam{}", JacksonUtil.writeValueAsString(param));

        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(conVertToUserLoginResult(userLoginDTO));

    }

    @RequestMapping("/mail/login")
    public CommonResult<UserLoginResult> shortMessageLogin(
            @Validated @RequestBody ShortMessageLoginParam param){
        logger.info("shortMessageLogin ShortMessageLoginParam{}", JacksonUtil.writeValueAsString(param));

        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(conVertToUserLoginResult(userLoginDTO));
    }

    @RequestMapping("/verification-code/send")
    public CommonResult<Boolean> sendVerificationCode(String email){
        logger.info("sendVerificationCode Email{}", email);
        verificationCodeService.sendVerificationCode(email);
        return CommonResult.success(Boolean.TRUE);
    }

    @RequestMapping("/base-user/find-list")
    public CommonResult<List<BaseUserInfoResult>> findBaseUserInfo(String identity){
        logger.info("findBaseUserInfo identity: {}", identity);
        List<UserDTO> userDTOList = userService.findUserInfo(
                UserIdentityEnum.forName(identity));
        return CommonResult.success(conVertToList(userDTOList));
    }

    private List<BaseUserInfoResult> conVertToList(List<UserDTO> userDTOList) {
        if(CollectionUtils.isEmpty(userDTOList)){
            return Arrays.asList();
        }

        return userDTOList.stream()
                .map(userDTO -> {
                    BaseUserInfoResult result = new BaseUserInfoResult();
                    result.setUserId(userDTO.getUserId());
                    result.setUserName(userDTO.getUserName());
                    result.setIdentity(userDTO.getIdentity().name());
                    return result;
                }).collect(Collectors.toList());
    }

    private UserRegisterResult convertToUserRegisterResult(UserRegisterDTO userRegisterDTO) {
        UserRegisterResult result = new UserRegisterResult();
        if(null == userRegisterDTO){
            throw new ControllerException(ControllerErrorCodeConstants.RiGISTER_ERROR);
        }
        result.setUserId(userRegisterDTO.getUserId());
        return result;
    }

    private UserLoginResult conVertToUserLoginResult(UserLoginDTO userLoginDTO) {
        if(null == userLoginDTO){
            throw new ControllerException(ControllerErrorCodeConstants.LOGIN_ERROR);
        }

        UserLoginResult result = new UserLoginResult();
        result.setToken(userLoginDTO.getToken());
        result.setIdentity(userLoginDTO.getIdentity().name());
        return result;
    }

}
