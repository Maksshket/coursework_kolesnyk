package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RequestServlet.class.getName());

    private final RequestService service = new RequestService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                service.delete(id(req));
            } else if ("transition".equals(action)) {
                String stateValue = req.getParameter("state");
                if (stateValue == null || stateValue.isBlank()) {
                    throw new IllegalArgumentException("Не задано стан заявки");
                }
                service.changeState(id(req), RequestState.valueOf(stateValue));
            } else if ("add".equals(action) || "update".equals(action)) {
                Request value = new Request(
                        "update".equals(action) ? id(req) : null,
                        Long.parseLong(req.getParameter("roomId")),
                        specialistId(req),
                        req.getParameter("description"),
                        RequestState.NEW,
                        Double.parseDouble(req.getParameter("f1")),
                        Double.parseDouble(req.getParameter("f2")),
                        Double.parseDouble(req.getParameter("f3")),
                        0
                );
                if ("add".equals(action)) {
                    service.add(value);
                } else {
                    service.update(value);
                }
            } else {
                throw new IllegalArgumentException("Невідома операція");
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private long id(HttpServletRequest req) {
        return Long.parseLong(req.getParameter("id"));
    }
    private Long specialistId(HttpServletRequest req) {
        String value = req.getParameter("specialistId");
        return (value == null || value.isBlank()) ? null : Long.valueOf(value);
    }

    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    private void writeError(HttpServletResponse resp, Exception e) throws IOException {
        boolean clientError = e instanceof IllegalArgumentException;
        if (!clientError) {
            LOG.log(Level.SEVERE, "Помилка виконання запиту", e);
        }
        int status = clientError
                ? HttpServletResponse.SC_BAD_REQUEST
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String message = clientError
                ? (e.getMessage() == null ? "Некоректний запит" : e.getMessage())
                : "Внутрішня помилка сервера";
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }
}
