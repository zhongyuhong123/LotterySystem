package org.example.java_gobang.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.java_gobang.JavaGobangApplication;
import org.example.java_gobang.model.User;
import org.example.java_gobang.model.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

//这个类表示一个游戏房间
public class Room {
    //使用字符串类型来表示，方便生成唯一值。final
    private String roomId;

    private User user1;
    private User user2;

    //先手的玩家（白子）
    private User whiteUser;


    private OnlineUserManager onlineUserManager;
    private RoomManager roomManager;
    private UserMapper userMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_COL = 15;
    private static final int MAX_ROW = 15;

    //添加一个棋盘，使用二维数组来表示。
    //此处约定：
    //  1）使用 0 表示当前位置未落子，一个刚 new 好的二维数组，默认全是0，相对于是空棋盘
    //  2）使用 1 表示 user1 的落子位置。
    //  3）使用 2 表示 user2 的落子位置。
    private int[][] board = new int[MAX_ROW][MAX_COL];

    public void putChess(String reqJson) throws IOException {
//        System.out.println("收到落子响应"+reqJson);
        //1.解析请求，并且记录落子的位置。
        GameRequest request = objectMapper.readValue(reqJson, GameRequest.class);
        GameResponse response = new GameResponse();
        int chess = request.getUserId() == user1.getUserId()? 1 : 2;//获取当前棋子
        int row = request.getRow();
        int col = request.getCol();
        if(board[row][col] != 0) {
            //当前位置有子了，不能重复落子。（客户端也判断过）
            System.out.println("当前位置 ("+row+","+col+") 已经有子了！");
            return;
        }
        board[row][col] = chess;

        //2.打印棋盘状态
        //printBoard();

        //3.判断当前是否分出胜负
        int winner = checkWinner(row, col, chess);
        if(winner == 1){
            winner = user1.getUserId();
        }else if(winner == 2){
            winner = user2.getUserId();
        }

        //4.返回响应，给房间中的所有客户端都返回响应。
        response.setMessage("putChess");
        response.setUserId(request.getUserId());
        response.setRow(row);
        response.setCol(col);
        response.setWinner(winner);

        //通过 websocket 把上述响应给发送客户端。
        WebSocketSession session1 = onlineUserManager.getFromGameRoom(user1.getUserId());
        WebSocketSession session2 = onlineUserManager.getFromGameRoom(user2.getUserId());

        //玩家下线
        if(session1 == null){
            //玩家1下线，直接判定玩家2获胜
            response.setWinner(user2.getUserId());
            System.out.println("玩家1掉线！");
        }else if(session2 == null){
            //玩家2下线，直接判定玩家1获胜
            response.setWinner(user1.getUserId());
            System.out.println("玩家2掉线！");
        }

        //5.把响应对象构造成 JSON 字符串，通过 session 对象进行传输。
        String respJson = objectMapper.writeValueAsString(response);
        if(session1 != null){
            session1.sendMessage(new TextMessage(respJson));
        }
        if(session2 != null){
            session2.sendMessage(new TextMessage(respJson));
        }

        //6.如果胜负已分，房间就失去存在的意义了，可以销毁房间了。
        if(response.getWinner() != 0){
            System.out.println("游戏结束！房间即将销毁！roomId"+roomId+" 获胜方为："+response.getWinner());
            //更新获胜方和失败方的信息。
            int winUserId = response.getWinner();
            int loseUserId = response.getWinner() == user1.getUserId()? user2.getUserId() : user1.getUserId();
            userMapper.userWin(winUserId);
            userMapper.userLose(loseUserId);

            //销毁房间
            roomManager.remove(roomManager.getRoomByRoomId(roomId), user1.getUserId(), user2.getUserId());
        }

    }

    private void printBoard() {
        //todo  更好的做法，应该给每个房间的信息放到单独的日志文件中进行打印。
        System.out.println("[打印棋盘信息]  房间号："+roomId);
        System.out.println("===============================");
        for(int r = 0; r < MAX_ROW; r++) {
            for(int c = 0; c < MAX_COL; c++){
                System.out.print(board[r][c] + " ");
            }
            System.out.println("/n");
        }
        System.out.println("===============================");
    }

    // 判断胜负
    private int checkWinner(int row, int col, int chess) {
        //定义两个指针从当前坐标(row, col)往两边数chess的数量
        //  start：往左or上方找头（记录当前棋子到头方向棋子数量）
        //  end：  往右or下方找尾（记录当前棋子到尾方向棋子数量）

        //检查横线
        int start=0,end=0;
        for(int r=row,c=col-1;c>=0 && board[r][c]==chess;c--){
            start++;
        }
        for(int r=row,c=col+1;c<MAX_COL && board[r][c]==chess;c++){
            end++;
        }
        if(start+end+1==5) return chess;

        //检查竖线
        start=0;
        end=0;
        for(int r=row-1,c=col;r>=0 && board[r][c]==chess;r--){
            start++;
        }
        for(int r=row+1,c=col;r<MAX_ROW && board[r][c]==chess;r++){
            end++;
        }
        if(start+end+1==5) return chess;

        //检查左上斜线
        start=0;
        end=0;
        for(int r=row-1,c=col-1;r>=0 && c>=0 && board[r][c]==chess; r--, c--){
            start++;
        }
        for(int r=row+1,c=col+1;r<MAX_ROW && c<MAX_COL && board[r][c]==chess;r++, c++){
            end++;
        }
        if(start+end+1==5) return chess;

        //检查左下斜线
        start=0;
        end=0;
        for(int r=row+1,c=col-1;r<MAX_ROW && c>=0 && board[r][c]==chess; r++, c--){
            start++;
        }
        for(int r=row-1,c=col+1;r>=0 && c<MAX_COL && board[r][c]==chess;r--, c++){
            end++;
        }
        if(start+end+1==5) return chess;

        return 0;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getWhiteUser() {
        return whiteUser;
    }

    public void setWhiteUser(User user) {
        whiteUser=user;
    }



    public Room() {
       //构造 Room 的时候生成一个唯一的字符串表示房间id。
        //使用UUID来作为房间 id
        roomId = UUID.randomUUID().toString();

        //通过入口类中的 context 成员，手动获取到 RoomManager 和 OnlineUserManager
        // 用这个方法来代替使用 @Autowired 交给 spring 来进行管理
        onlineUserManager = JavaGobangApplication.context.getBean(OnlineUserManager.class);
        roomManager = JavaGobangApplication.context.getBean(RoomManager.class);
        userMapper = JavaGobangApplication.context.getBean(UserMapper.class);
    }

    public static void main(String[] args) {
        Room room = new Room();
        System.out.println(room.roomId);
    }
}
