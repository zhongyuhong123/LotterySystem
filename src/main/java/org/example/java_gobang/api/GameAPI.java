package org.example.java_gobang.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.java_gobang.game.*;
import org.example.java_gobang.model.User;
import org.example.java_gobang.model.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class GameAPI extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Resource
    private UserMapper userMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();

        //1.先获取到用户的身份信息。(从HttpSession里拿到)
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            resp.setOk(false);
            resp.setMessage("用户尚未登录！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        //2.判定当前用户是否已经进入房间，改玩家还没有匹配到。
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            //如果为null， 当前没有找到对应的房间，该玩家还没有匹配到。
            resp.setOk(false);
            resp.setReason("用户尚未匹配到！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        //3.判定当前是不是多开（该用户是否已经在其他地方进入游戏了）
        if(onlineUserManager.getFromGameHall(user.getUserId()) != null
                || onlineUserManager.getFromGameRoom(user.getUserId()) != null){
            //一个账号一边在游戏大厅，一边在游戏房间，也视为多开~~
            resp.setOk(true);
            resp.setReason("禁止多开游戏页面");
            resp.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        //4.设置当前玩家上线！
        onlineUserManager.enterGameRoom(user.getUserId(), session);

        //5.把两个玩家加入到游戏房间中
        //   前面创建房间/匹配过程，是在 game_hall.html 页面中完成的。
        //   当匹配到对手之后，就要经过页面跳转，来到 game_room.html 中才算真正进入房间。
        //    页面跳转可能失败的！！
        //    当执行到这个逻辑的时候，说明玩家已经进入到 game_room.html 页面。
        synchronized (room){
            if(room.getUser1() == null){
                //第一个玩家还尚未进入房间
                //就把当前连上 websocket 的玩家作为 user1, 加入房间中。
                room.setUser1(user);
                //将第一个进入房间的玩家设置为先手
                room.setWhiteUser(user);
                System.out.println("玩家 "+user.getUsername() + "已经准备就绪");
                return;
            }
            if(room.getUser2() == null){
                //进入这个逻辑，说明玩家1已经进入房间，现在要给当前玩家作为玩家2，加入房间
                room.setUser2(user);
                System.out.println("玩家 "+user.getUsername() + "已经准备就绪");

                //当前两个玩家都加入成功之后，就要让服务器，给这两个玩家都返回 websocket 响应。
                //通知这两个玩家说，游戏双方已经准备好了。

                //通知玩家1
                noticeGameReady(room, room.getUser1(), room.getUser2());
                //通知玩家2
                noticeGameReady(room, room.getUser2(), room.getUser1());

                return;
            }
        }

        //6.此处若还有玩家测试连接同一个房间，就提示报错。
        //  理论上不存在，为增强代码的健壮性，仍做一个判定。
        resp.setOk(false);
        resp.setReason("当前房间已满，您不能加入房间！");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    private void noticeGameReady(Room room, User thisUser, User thatUser) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();
        resp.setMessage("gameReady");
        resp.setOk(true);
        resp.setReason("");
        resp.setRoomId(room.getRoomId());
        resp.setThisUserId(thisUser.getUserId());
        resp.setThatUserId(thatUser.getUserId());
        resp.setWhiteUser(room.getWhiteUser().getUserId());
        // 关键修复：判断 session 是否为 null
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thisUser.getUserId());
        if (webSocketSession != null && webSocketSession.isOpen()) { // 同时判断连接是否打开
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        } else {
            System.err.println("用户 " + thisUser.getUsername() + " 的游戏房间连接未建立！");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //1.先从会话中拿到当前用户的身份信息
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            System.out.println("[handleTextMessage] 当前用户尚未登录！");
            return;
        }
        //2.根据玩家id ，获取到游戏房间
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            return;
        }
        //3.通过 room 对象具体处理这次请求
        room.putChess(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //1.从会话中拿到用户信息。
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            //简单处理，不给客户端返回响应了。
            return;
        }
        //2.判定用户是否在线，在线就把用户从在线用户列表中删除掉。
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if(session==exitSession){
            //通过这个判定，避免在多开的情况下，第二个用户的退出连接动作，把第一个用户的在线状态影响到。
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 "+user.getUsername() + " 游戏房间连接异常！");
        //3.通知对手获胜
        noticeThatUserWin(user);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //1.从会话中拿到用户信息。
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            //简单处理，不给客户端返回响应了。
            return;
        }
        //2.判定用户是否在线，在线就把用户从在线用户列表中删除掉。
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if(session==exitSession){
            //通过这个判定，避免在多开的情况下，第二个用户的退出连接动作，把第一个用户的在线状态影响到。
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 "+user.getUsername() + " 离开游戏房间！");
        //3.通知对手获胜
        noticeThatUserWin(user);
    }

    private void noticeThatUserWin(User user) throws IOException {
        //1.根据userid找房间
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            System.out.println("当前房间已经被释放!");
            return;
        }
        //2.在房间里面找对手
        User thatUser = (user == room.getUser1()) ? room.getUser2() : room.getUser1();
        //3.找到对手的在线状态
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thatUser.getUserId());
        if(webSocketSession == null){
            System.out.println("双方都掉线！！");
            return;
        }
        //4.构造响应，通过获胜方
        GameResponse resp = new GameResponse();
        resp.setMessage("putChess");
        resp.setUserId(thatUser.getUserId());
        resp.setWinner(thatUser.getUserId());
        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        //5.更新玩家信息
        int winUserId = thatUser.getUserId();
        int loseUserId = user.getUserId();
        userMapper.userWin(winUserId);
        userMapper.userLose(loseUserId);
        //6.释放房间
        roomManager.remove(room,room.getUser1().getUserId(),room.getUser2().getUserId());
    }
}
