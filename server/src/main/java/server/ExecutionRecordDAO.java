package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExecutionRecordDAO {

    public void add(ExecutionRecord e) throws SQLException {
        String sql = "INSERT INTO execution_record(request_id,specialist_id,work_date,hours_spent,description) VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, e);
            p.executeUpdate();
        }
    }

    public List<ExecutionRecord> getAll() throws SQLException {
        List<ExecutionRecord> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM execution_record ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public ExecutionRecord getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM execution_record WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Чи існує такий самий запис виконання (заявка, фахівець, дата й опис робіт). */
    public boolean exists(ExecutionRecord e, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM execution_record WHERE request_id=? AND specialist_id=? AND work_date=? "
                + "AND TRIM(description)=? AND id<>? LIMIT 1";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, e.getRequestId());
            p.setLong(2, e.getSpecialistId());
            p.setString(3, DuplicateUtil.clean(e.getWorkDate()));
            p.setString(4, DuplicateUtil.clean(e.getDescription()));
            p.setLong(5, DuplicateUtil.excluded(excludeId));
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public void update(ExecutionRecord e) throws SQLException {
        String sql = "UPDATE execution_record SET request_id=?,specialist_id=?,work_date=?,hours_spent=?,description=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, e);
            p.setLong(6, e.getId());
            p.executeUpdate();
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM execution_record WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }
    private void bind(PreparedStatement p, ExecutionRecord e) throws SQLException {
        p.setLong(1, e.getRequestId());
        p.setLong(2, e.getSpecialistId());
        p.setString(3, e.getWorkDate());
        p.setDouble(4, e.getHoursSpent());
        p.setString(5, e.getDescription());
    }

    private ExecutionRecord map(ResultSet r) throws SQLException {
        return new ExecutionRecord(r.getLong("id"), r.getLong("request_id"), r.getLong("specialist_id"),
                r.getString("work_date"), r.getDouble("hours_spent"), r.getString("description"));
    }
}
