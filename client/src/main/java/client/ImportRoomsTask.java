package client;

import javafx.concurrent.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фоновий імпорт номерів із CSV-файлу. Task читає файл (UTF-8), перевіряє заголовок,
 * послідовно розбирає рядки й передає кожен коректний запис серверу через HttpHotelClient.
 * Предметну валідацію та роботу з MySQL виконує сервер.

 */
public class ImportRoomsTask extends Task<Integer> {

    static final String HEADER = "room_number;floor;room_type;capacity;equipment";
    static final int COLUMNS = 5;
    private static final Logger LOG = Logger.getLogger(ImportRoomsTask.class.getName());

    private final Path file;
    private final HttpHotelClient api;
    private volatile int skipped;
    private volatile int duplicates;
    public ImportRoomsTask(Path file, HttpHotelClient api) {
        this.file = file;
        this.api = api;
    }
    public int getSkipped() {
        return skipped;
    }

    /** Скільки рядків пропущено, бо такий номер уже є на сервері або повторюється у файлі. */
    public int getDuplicates() {
        return duplicates;
    }
    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок CSV-імпорту: " + file);
        updateMessage("Читання CSV-файлу...");
        if (!Files.isReadable(file)) {
            LOG.severe("CSV-файл не знайдено або недоступний: " + file);
            throw new IOException("CSV-файл не знайдено або недоступний: " + file);
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            LOG.severe("CSV-файл порожній: " + file);
            throw new IllegalArgumentException("CSV-файл порожній");
        }
        String header = lines.get(0).replace("\uFEFF", "").trim();
        if (!HEADER.equals(header)) {
            LOG.severe("Неправильний заголовок CSV: " + header);
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: " + HEADER);
        }
        int total = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                total++;
            }
        }
        if (total == 0) {
            updateProgress(0, 1);
            updateMessage("Немає рядків для імпорту");
            LOG.info("CSV-файл не містить рядків даних");
            return 0;
        }
        updateMessage("Перевірка наявних номерів...");
        Set<String> knownNumbers = new HashSet<>();
        for (Room existing : api.getRooms()) {
            knownNumbers.add(numberKey(existing.getRoomNumber()));
        }
        int processed = 0;
        int imported = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            processed++;
            Room room = parse(line, i + 1);
            if (room == null) {
                skipped++;
            } else if (!knownNumbers.add(numberKey(room.getRoomNumber()))) {
                duplicates++;
                LOG.warning("Пропущено рядок " + (i + 1) + ": номер " + room.getRoomNumber()
                        + " уже існує або повторюється у файлі");
            } else {
                try {
                    api.addRoom(room);
                    imported++;
                } catch (RuntimeException e) {
                    LOG.log(Level.SEVERE, "Помилка надсилання рядка " + (i + 1), e);
                    throw new IllegalStateException("Рядок " + (i + 1) + ": " + reason(e)
                            + ". Імпортовано до збою: " + imported, e);
                }
            }
            updateProgress(processed, total);
            updateMessage("Опрацьовано " + processed + " із " + total + ", імпортовано " + imported
                    + (skipped > 0 ? ", пропущено " + skipped : "")
                    + (duplicates > 0 ? ", дублікатів " + duplicates : ""));
        }
        LOG.info("Імпорт завершено: імпортовано " + imported + ", пропущено " + skipped
                + ", дублікатів " + duplicates + ", опрацьовано " + processed);
        return imported;
    }
    static Room parse(String line, int lineNumber) {
        String[] p = line.split(";", -1);
        if (p.length != COLUMNS) {
            LOG.warning("Пропущено рядок " + lineNumber + ": очікувано " + COLUMNS + " полів, отримано " + p.length);
            return null;
        }
        if (p[0].isBlank() || p[2].isBlank() || p[4].isBlank()) {
            LOG.warning("Пропущено рядок " + lineNumber + ": порожнє обов’язкове текстове поле");
            return null;
        }
        try {
            return new Room(null, p[0].trim(), Integer.parseInt(p[1].trim()), p[2].trim(),
                    Integer.parseInt(p[3].trim()), p[4].trim());
        } catch (NumberFormatException e) {
            LOG.warning("Пропущено рядок " + lineNumber + ": поверх і місткість мають бути цілими числами");
            return null;
        }
    }
    static String numberKey(String roomNumber) {
        return roomNumber == null ? "" : roomNumber.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String reason(RuntimeException e) {
        if (e.getCause() instanceof IOException) {
            return "сервер недоступний";
        }
        String message = e.getMessage() == null ? "помилка сервера" : e.getMessage();
        int start = message.indexOf("\"error\":\"");
        int end = message.lastIndexOf('"');
        if (start >= 0 && end > start + 9) {
            return message.substring(start + 9, end);
        }
        return message;
    }
}
