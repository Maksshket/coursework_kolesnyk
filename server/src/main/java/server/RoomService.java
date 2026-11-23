package server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RoomService {

    private static final Logger LOG = Logger.getLogger(RoomService.class.getName());

    private final RoomDAO dao = new RoomDAO();

    public List<Room> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Room r) throws SQLException {
        validate(r);
        normalize(r);
        requireUnique(r);
        try {
            dao.add(r);
        } catch (SQLException e) {
            DuplicateUtil.rethrowIfDuplicate(e, duplicateMessage(r));
            throw e;
        }
        LOG.info("Додано номер");
    }

    public void update(Room r) throws SQLException {
        validate(r);
        normalize(r);
        requireUnique(r);
        try {
            dao.update(r);
        } catch (SQLException e) {
            DuplicateUtil.rethrowIfDuplicate(e, duplicateMessage(r));
            throw e;
        }
        LOG.info("Оновлено номер");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено номер");
    }

    static void validate(Room r) {
        if (r == null || blank(r.getRoomNumber()) || blank(r.getRoomType()) || blank(r.getEquipment())) {
            throw new IllegalArgumentException("Номер кімнати, категорія та обладнання є обов’язковими");
        }
        if (r.getCapacity() <= 0) {
            throw new IllegalArgumentException("Місткість номера має бути більшою за 0");
        }
    }

    private static void normalize(Room r) {
        r.setRoomNumber(DuplicateUtil.clean(r.getRoomNumber()));
        r.setRoomType(DuplicateUtil.clean(r.getRoomType()));
        r.setEquipment(DuplicateUtil.clean(r.getEquipment()));
    }

    private void requireUnique(Room r) throws SQLException {
        if (dao.existsByNumber(r.getRoomNumber(), r.getId())) {
            LOG.warning("Відхилено дублікат номера: " + r.getRoomNumber());
            throw new IllegalArgumentException(duplicateMessage(r));
        }
    }

    private static String duplicateMessage(Room r) {
        return "Номер «" + r.getRoomNumber() + "» уже існує";
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
