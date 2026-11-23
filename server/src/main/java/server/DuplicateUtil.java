package server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

/**
 * Допоміжні засоби для виявлення дублікатів: нормалізація тексту
 * (обрізання пробілів, стиснення повторних пробілів, нижній регістр)
 * та перетворення порушення унікальності в БД на зрозумілу помилку.
 */
final class DuplicateUtil {

    private DuplicateUtil() {
    }

    static String clean(String v) {
        return v == null ? null : v.trim().replaceAll("\\s+", " ");
    }

    static String key(String v) {
        return v == null ? "" : clean(v).toLowerCase(Locale.ROOT);
    }

    static boolean same(String a, String b) {
        return key(a).equals(key(b));
    }

    /** Телефон порівнюється лише за цифрами: «+380 50 000-00-01» = «380500000001». */
    static String phoneKey(String v) {
        return v == null ? "" : v.replaceAll("\\D", "");
    }

    /** Ідентифікатор для виключення власного запису під час оновлення (-1 = нічого не виключати). */
    static long excluded(Long id) {
        return id == null ? -1L : id;
    }

    static boolean isUniqueViolation(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException && e.getErrorCode() == 1062;
    }

    /**
     * Якщо два запити пройшли перевірку одночасно, дублікат остаточно зупиняє UNIQUE-індекс БД;
     * тут це перетворюється на звичайну помилку валідації (HTTP 400), а не на 500.
     */
    static void rethrowIfDuplicate(SQLException e, String message) {
        if (isUniqueViolation(e)) {
            throw new IllegalArgumentException(message, e);
        }
    }
}
