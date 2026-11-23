package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public void add(Request q) throws SQLException {
        String sql = "INSERT INTO request(room_id,specialist_id,description,state,f1,f2,f3,priority_index) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, q);
            p.executeUpdate();
        }
    }

    public List<Request> getAll() throws SQLException {
        List<Request> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM request ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Request getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM request WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }
    /** Чи є інша незавершена заявка для того ж номера з таким самим описом. */
    public boolean existsActiveDuplicate(long roomId, String description, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM request WHERE room_id=? AND TRIM(description)=? AND state<>? AND id<>? LIMIT 1";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, roomId);
            p.setString(2, DuplicateUtil.clean(description));
            p.setString(3, RequestState.DONE.name());
            p.setLong(4, DuplicateUtil.excluded(excludeId));
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public void update(Request q) throws SQLException {
        String sql = "UPDATE request SET room_id=?,specialist_id=?,description=?,state=?,f1=?,f2=?,f3=?,priority_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, q);
            p.setLong(9, q.getId());
            p.executeUpdate();
        }
    }

    public void updateState(long id, RequestState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE request SET state=? WHERE id=?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM request WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }
    private void bind(PreparedStatement p, Request q) throws SQLException {
        p.setLong(1, q.getRoomId());
        if (q.getSpecialistId() == null) {
            p.setNull(2, Types.BIGINT);
        } else {
            p.setLong(2, q.getSpecialistId());
        }
        p.setString(3, q.getDescription());
        p.setString(4, q.getState().name());
        p.setDouble(5, q.getF1());
        p.setDouble(6, q.getF2());
        p.setDouble(7, q.getF3());
        p.setDouble(8, q.getPriorityIndex());
    }
    private Long specialistId(ResultSet r) throws SQLException {
        long value = r.getLong("specialist_id");
        return r.wasNull() ? null : value;
    }

    private Request map(ResultSet r) throws SQLException {
        return new Request(r.getLong("id"), r.getLong("room_id"), specialistId(r), r.getString("description"),
                RequestState.valueOf(r.getString("state")),
                r.getDouble("f1"), r.getDouble("f2"), r.getDouble("f3"), r.getDouble("priority_index"));
    }
}
