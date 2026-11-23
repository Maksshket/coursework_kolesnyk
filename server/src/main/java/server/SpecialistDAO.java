package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecialistDAO {

    public void add(Specialist s) throws SQLException {
        String sql = "INSERT INTO specialist(full_name,specialty,phone) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            p.executeUpdate();
        }
    }

    public List<Specialist> getAll() throws SQLException {
        List<Specialist> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM specialist ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Specialist getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM specialist WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Чи існує інший фахівець із таким самим ПІБ і спеціалізацією. */
    public boolean existsByNameAndSpecialty(String fullName, String specialty, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM specialist WHERE TRIM(full_name)=? AND TRIM(specialty)=? AND id<>? LIMIT 1";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, DuplicateUtil.clean(fullName));
            p.setString(2, DuplicateUtil.clean(specialty));
            p.setLong(3, DuplicateUtil.excluded(excludeId));
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    /** Чи існує інший фахівець із таким самим телефоном (порівняння за цифрами). */
    public boolean existsByPhone(String phone, Long excludeId) throws SQLException {
        String key = DuplicateUtil.phoneKey(phone);
        String sql = "SELECT phone FROM specialist WHERE id<>?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, DuplicateUtil.excluded(excludeId));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    if (DuplicateUtil.phoneKey(r.getString(1)).equals(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void update(Specialist s) throws SQLException {
        String sql = "UPDATE specialist SET full_name=?,specialty=?,phone=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            p.setLong(4, s.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM specialist WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Specialist s) throws SQLException {
        p.setString(1, s.getFullName());
        p.setString(2, s.getSpecialty());
        p.setString(3, s.getPhone());
    }

    private Specialist map(ResultSet r) throws SQLException {
        return new Specialist(r.getLong("id"), r.getString("full_name"), r.getString("specialty"), r.getString("phone"));
    }
}
