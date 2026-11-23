package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class HttpHotelClient {

    private static final String BASE = "http://localhost:8080/hotel-requests-server";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Room> getRooms() {
        return Arrays.asList(read(get("/rooms"), Room[].class));
    }
    public void addRoom(Room r) {
        post("/rooms", roomForm("add", r));
    }

    public void updateRoom(Room r) {
        post("/rooms", roomForm("update", r) + "&id=" + r.getId());
    }

    public void deleteRoom(long id) {
        post("/rooms", "action=delete&id=" + id);
    }

    public List<Specialist> getSpecialists() {
        return Arrays.asList(read(get("/specialists"), Specialist[].class));
    }

    public void addSpecialist(Specialist s) {
        post("/specialists", specialistForm("add", s));
    }

    public void updateSpecialist(Specialist s) {
        post("/specialists", specialistForm("update", s) + "&id=" + s.getId());
    }

    public void deleteSpecialist(long id) {
        post("/specialists", "action=delete&id=" + id);
    }

    public List<Request> getRequests() {
        return Arrays.asList(read(get("/requests"), Request[].class));
    }

    public void addRequest(Request q) {
        post("/requests", requestForm("add", q));
    }

    public void updateRequest(Request q) {
        post("/requests", requestForm("update", q) + "&id=" + q.getId());
    }

    public void changeRequestState(long id, RequestState state) {
        post("/requests", "action=transition&id=" + id + "&state=" + state.name());
    }

    public void deleteRequest(long id) {
        post("/requests", "action=delete&id=" + id);
    }

    public List<ExecutionRecord> getRecords() {
        return Arrays.asList(read(get("/records"), ExecutionRecord[].class));
    }

    public void addRecord(ExecutionRecord e) {
        post("/records", recordForm("add", e));
    }

    public void updateRecord(ExecutionRecord e) {
        post("/records", recordForm("update", e) + "&id=" + e.getId());
    }

    public void deleteRecord(long id) {
        post("/records", "action=delete&id=" + id);
    }

    private String roomForm(String action, Room r) {
        return "action=" + action + "&roomNumber=" + enc(r.getRoomNumber()) + "&floor=" + r.getFloor()
                + "&roomType=" + enc(r.getRoomType()) + "&capacity=" + r.getCapacity()
                + "&equipment=" + enc(r.getEquipment());
    }

    private String specialistForm(String action, Specialist s) {
        return "action=" + action + "&fullName=" + enc(s.getFullName())
                + "&specialty=" + enc(s.getSpecialty()) + "&phone=" + enc(s.getPhone());
    }

    private String requestForm(String action, Request q) {
        return "action=" + action + "&roomId=" + q.getRoomId()
                + "&specialistId=" + (q.getSpecialistId() == null ? "" : q.getSpecialistId())
                + "&description=" + enc(q.getDescription())
                + "&f1=" + q.getF1() + "&f2=" + q.getF2() + "&f3=" + q.getF3();
    }

    private String recordForm(String action, ExecutionRecord e) {
        return "action=" + action + "&requestId=" + e.getRequestId() + "&specialistId=" + e.getSpecialistId()
                + "&workDate=" + enc(e.getWorkDate()) + "&hoursSpent=" + e.getHoursSpent()
                + "&description=" + enc(e.getDescription());
    }

    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).GET().build();
        return send(req);
    }

    private String post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return send(req);
    }
    private String send(HttpRequest req) {
        try {
            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() >= 400) {
                throw new RuntimeException(r.body());
            }
            return r.body();
        } catch (Exception e) {
            throw new RuntimeException("Помилка HTTP: " + e.getMessage(), e);
        }
    }
    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Помилка JSON", e);
        }
    }
    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
