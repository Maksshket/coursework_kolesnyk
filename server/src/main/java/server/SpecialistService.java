package server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class SpecialistService {

    private static final Logger LOG = Logger.getLogger(SpecialistService.class.getName());

    private final SpecialistDAO dao = new SpecialistDAO();

    public List<Specialist> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Specialist s) throws SQLException {
        validate(s);
        normalize(s);
        requireUnique(s);
        try {
            dao.add(s);
        } catch (SQLException e) {
            DuplicateUtil.rethrowIfDuplicate(e, "Такий фахівець уже існує");
            throw e;
        }
        LOG.info("Додано фахівця");
    }

    public void update(Specialist s) throws SQLException {
        validate(s);
        normalize(s);
        requireUnique(s);
        try {
            dao.update(s);
        } catch (SQLException e) {
            DuplicateUtil.rethrowIfDuplicate(e, "Такий фахівець уже існує");
            throw e;
        }
        LOG.info("Оновлено фахівця");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено фахівця");
    }

    static void validate(Specialist s) {
        if (s == null || blank(s.getFullName()) || blank(s.getSpecialty()) || blank(s.getPhone())) {
            throw new IllegalArgumentException("ПІБ, спеціалізація та телефон фахівця є обов’язковими");
        }
    }

    private static void normalize(Specialist s) {
        s.setFullName(DuplicateUtil.clean(s.getFullName()));
        s.setSpecialty(DuplicateUtil.clean(s.getSpecialty()));
        s.setPhone(DuplicateUtil.clean(s.getPhone()));
    }

    private void requireUnique(Specialist s) throws SQLException {
        if (dao.existsByPhone(s.getPhone(), s.getId())) {
            LOG.warning("Відхилено дублікат телефону фахівця: " + s.getPhone());
            throw new IllegalArgumentException("Фахівець із телефоном " + s.getPhone() + " уже існує");
        }
        if (dao.existsByNameAndSpecialty(s.getFullName(), s.getSpecialty(), s.getId())) {
            LOG.warning("Відхилено дублікат фахівця: " + s.getFullName());
            throw new IllegalArgumentException("Фахівець «" + s.getFullName() + "» зі спеціалізацією «"
                    + s.getSpecialty() + "» уже існує");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
