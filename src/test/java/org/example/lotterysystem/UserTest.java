package org.example.lotterysystem;

import org.example.lotterysystem.service.UserService;
import org.example.lotterysystem.service.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Test
    void findBaseUserList(){
        List<UserDTO> userDTOList = userService.findUserInfo(null);
        for(UserDTO userDTO : userDTOList){
            System.out.println(userDTO.getUserId());
            System.out.println(userDTO.getUserName());
            System.out.println(userDTO.getIdentity().name());
        }
    }
}
