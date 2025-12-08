package org.example.java_gobang.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

//房间管理器
//这个类希望有唯一实例
@Component
public class RoomManager {
    private ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> userIdToRoomId = new ConcurrentHashMap<>();

    public void add(Room room, int userId1, int userId2) {
        rooms.put(room.getRoomId(), room);
        userIdToRoomId.put(userId1, room.getRoomId());
        userIdToRoomId.put(userId2, room.getRoomId());
    }

    public void remove(Room room, int userId1, int userId2) {
        rooms.remove(room.getRoomId());
        userIdToRoomId.remove(userId1);
        userIdToRoomId.remove(userId2);
    }

    public Room getRoomByRoomId(String roomId) {
        return rooms.get(roomId);
    }

    public Room getRoomByUserId(int userId) {
        String roomId = userIdToRoomId.get(userId);
        if(roomId == null) {
            //userId->roomId映射不存在
            return null;
        }
        return rooms.get(roomId);
    }

    public Room getRoomByRoomId(int roomId) {
        return rooms.get(roomId);
    }
}
