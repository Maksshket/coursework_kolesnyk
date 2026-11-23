package server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ExecutionRecordService {

    private static final Logger LOG = Logger.getLogger(ExecutionRecordService.class.getName());

    private final ExecutionRecordDAO dao = new ExecutionRecordDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final SpecialistDAO specialistDAO = new SpecialistDAO();

    public List<ExecutionRecord> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(ExecutionRecord e) throws SQLException {
        validate(e);
        requireRequest(e.getRequestId());
        requireSpecialist(e.getSpecialistId());
        normalize(e);
        requireUnique(e);
        dao.add(e);
        LOG.info("Додано запис виконання");
    }

    public void update(ExecutionRecord e) throws SQLException {
        validate(e);
        requireRequest(e.getRequestId());
        requireSpecialist(e.getSpecialistId());
        normalize(e);
        requireUnique(e);
        dao.update(e);
        LOG.info("Оновлено запис виконання");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено запис виконання");
    }

    static void validate(ExecutionRecord e) {
        if (e == null || e.getRequestId() <= 0 || e.getSpecialistId() <= 0
                || e.getWorkDate() == null || e.getWorkDate().isBlank()
                || e.getDescription() == null || e.getDescription().isBlank()) {
            throw new IllegalArgumentException("Некоректні дані запису виконання");
        }
        if (e.getHoursSpent() <= 0) {
            throw new IllegalArgumentException("Витрачений час має бути більшим за 0");
        }
    }

    private static void normalize(ExecutionRecord e) {
        e.setWorkDate(DuplicateUtil.clean(e.getWorkDate()));
        e.setDescription(DuplicateUtil.clean(e.getDescription()));
    }

    private void requireUnique(ExecutionRecord e) throws SQLException {
        if (dao.exists(e, e.getId())) {
            LOG.warning("Відхилено дублікат запису виконання для заявки " + e.getRequestId());
            throw new IllegalArgumentException("Такий запис виконання вже існує");
        }
    }

    private void requireRequest(long id) throws SQLException {
        if (requestDAO.getById(id) == null) {
            throw new IllegalArgumentException("Заявку не знайдено");
        }
    }

    private void requireSpecialist(long id) throws SQLException {
        if (specialistDAO.getById(id) == null) {
            throw new IllegalArgumentException("Фахівця не знайдено");
        }
    }
}
