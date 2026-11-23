package server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RequestService {

    private static final Logger LOG = Logger.getLogger(RequestService.class.getName());

    private final RequestDAO dao = new RequestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final SpecialistDAO specialistDAO = new SpecialistDAO();

    public List<Request> getAll() throws SQLException {
        return dao.getAll();
    }
    public void add(Request r) throws SQLException {
        validate(r);
        requireRoom(r.getRoomId());
        requireSpecialist(r.getSpecialistId());
        r.setDescription(DuplicateUtil.clean(r.getDescription()));
        requireNoActiveDuplicate(r);
        r.setState(RequestState.NEW);
        r.setPriorityIndex(PriorityCalculator.calculate(r.getF1(), r.getF2(), r.getF3()));
        dao.add(r);
        LOG.info("Створено заявку");
    }
    public void update(Request r) throws SQLException {
        if (r == null || r.getId() == null) {
            throw new IllegalArgumentException("Не вказано заявку");
        }
        Request old = dao.getById(r.getId());
        if (old == null) {
            throw new IllegalArgumentException("Заявку не знайдено");
        }
        validate(r);
        requireRoom(r.getRoomId());
        requireSpecialist(r.getSpecialistId());
        r.setDescription(DuplicateUtil.clean(r.getDescription()));
        if (old.getState() != RequestState.DONE) {
            requireNoActiveDuplicate(r);
        }
        r.setState(old.getState());
        r.setPriorityIndex(PriorityCalculator.calculate(r.getF1(), r.getF2(), r.getF3()));
        dao.update(r);
        LOG.info("Оновлено заявку");
    }

    public void changeState(long id, RequestState next) throws SQLException {
        Request current = dao.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Заявку не знайдено");
        }
        if (next == null || !isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан: " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено заявку");
    }

    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.NEW && to == RequestState.ASSIGNED)
                || (from == RequestState.ASSIGNED && to == RequestState.IN_PROGRESS)
                || (from == RequestState.IN_PROGRESS && to == RequestState.DONE);
    }

    static void validate(Request r) {
        if (r == null || r.getRoomId() <= 0 || r.getDescription() == null || r.getDescription().isBlank()) {
            throw new IllegalArgumentException("Некоректні основні дані заявки");
        }
        if (r.getSpecialistId() != null && r.getSpecialistId() <= 0) {
            throw new IllegalArgumentException("Некоректний ідентифікатор фахівця");
        }
        if (r.getF1() < 0 || r.getF1() > 100 || r.getF2() < 0 || r.getF2() > 100
                || r.getF3() < 0 || r.getF3() > 100) {
            throw new IllegalArgumentException("F1, F2 і F3 мають бути в діапазоні 0..100");
        }
    }

    /** Для одного номера не може бути двох незавершених заявок з однаковим описом. */
    private void requireNoActiveDuplicate(Request r) throws SQLException {
        if (dao.existsActiveDuplicate(r.getRoomId(), r.getDescription(), r.getId())) {
            LOG.warning("Відхилено дублікат заявки для номера " + r.getRoomId());
            throw new IllegalArgumentException("Для цього номера вже є незавершена заявка з таким самим описом");
        }
    }

    private void requireRoom(long id) throws SQLException {
        if (roomDAO.getById(id) == null) {
            throw new IllegalArgumentException("Номер не знайдено");
        }
    }
    private void requireSpecialist(Long id) throws SQLException {
        if (id != null && specialistDAO.getById(id) == null) {
            throw new IllegalArgumentException("Фахівця не знайдено");
        }
    }
}
