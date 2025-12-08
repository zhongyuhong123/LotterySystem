package org.example.java_gobang.api;

import org.example.java_gobang.model.User;
import org.example.java_gobang.model.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserAPI {

    @Resource
    public UserMapper userMapper;

    @PostMapping("/login")
    @ResponseBody
    public Object login(String username, String password, HttpServletRequest req) {
        //根据username去数据库中查找，如果能查到并密码一致则成功登入
        User user = userMapper.selectByName(username);
        System.out.println("[login] user="+user);
//        System.out.println("找到的username："+user.getUserName());
//        System.out.println("找到的password："+user.getPassword());
//        System.out.println("输入的："+password);
        if(user==null || !user.getPassword().equals(password)){
            System.out.println("登入失败!");
            return new User();//登入失败
        }
        //这里true含义：如果为true，会话存在直接返回，不存在则创建一个
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        return user;
    }

    @PostMapping("/register")
    @ResponseBody
    public Object register(String username, String password) {
        try{
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            return user;
        }catch (org.springframework.dao.DuplicateKeyException e){
            return new User();
        }
    }

    @GetMapping("/userInfo")
    @ResponseBody
    public Object getUserInfo(HttpServletRequest req) {
        try{
            HttpSession httpSession = req.getSession(false);
            User user = (User) httpSession.getAttribute("user");
            //拿着这个user对象，去数据库中找，找到最新的数据
            User newUser = userMapper.selectByName(user.getUsername());
            return newUser;
        }catch (NullPointerException e){
            return new User();
        }
    }
}
