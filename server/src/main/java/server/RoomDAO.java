package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public void add(Room r) throws SQLException {
        String sql = "INSERT INTO room(room_number,floor,room_type,capacity,equipment) VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.executeUpdate();
        }
    }

    public List<Room> getAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM room ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Room getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM room WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }
    /** Чи існує інший номер із таким самим номером кімнати (без урахування регістру й зайвих пробілів). */
    public boolean existsByNumber(String roomNumber, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM room WHERE TRIM(room_number)=? AND id<>? LIMIT 1";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, DuplicateUtil.clean(roomNumber));
            p.setLong(2, DuplicateUtil.excluded(excludeId));
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public void update(Room r) throws SQLException {
        String sql = "UPDATE room SET room_number=?,floor=?,room_type=?,capacity=?,equipment=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(6, r.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM room WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Room r) throws SQLException {
        p.setString(1, r.getRoomNumber());
        p.setInt(2, r.getFloor());
        p.setString(3, r.getRoomType());
        p.setInt(4, r.getCapacity());
        p.setString(5, r.getEquipment());
    }

    private Room map(ResultSet r) throws SQLException {
        return new Room(r.getLong("id"), r.getString("room_number"), r.getInt("floor"),
                r.getString("room_type"), r.getInt("capacity"), r.getString("equipment"));
    }
}
