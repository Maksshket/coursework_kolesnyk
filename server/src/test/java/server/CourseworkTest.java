package server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
class CourseworkTest {
    @Test
    void priorityForFirstDemoRequestIs61_5() {
        double expected = 61.5;
        double actual = PriorityCalculator.calculate(70, 60, 50);
        assertEquals(expected, actual, 0.001);
    }

    @Test
    void priorityForSecondDemoRequestIs69() {
        assertEquals(69.0, PriorityCalculator.calculate(40, 80, 100), 0.001);
    }

    @Test
    void priorityIsZeroWhenAllFactorsAreZero() {
        assertEquals(0.0, PriorityCalculator.calculate(0, 0, 0), 0.001);
    }

    @Test
    void priorityIsHundredWhenAllFactorsAreHundred() {
        assertEquals(100.0, PriorityCalculator.calculate(100, 100, 100), 0.001);
    }

    @Test
    void priorityIsRoundedToTwoDecimals() {
        assertEquals(18.82, PriorityCalculator.calculate(10.5, 20.25, 30.125), 0.001);
    }

    @Test
    void priorityRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(101, 10, 10));
    }

    @Test
    void priorityRejectsNegativeFactor() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(10, -1, 10));
    }
    @Test
    void newToAssignedAllowed() {
        assertTrue(RequestService.isAllowedTransition(RequestState.NEW, RequestState.ASSIGNED));
    }

    @Test
    void assignedToInProgressAllowed() {
        assertTrue(RequestService.isAllowedTransition(RequestState.ASSIGNED, RequestState.IN_PROGRESS));
    }

    @Test
    void inProgressToDoneAllowed() {
        assertTrue(RequestService.isAllowedTransition(RequestState.IN_PROGRESS, RequestState.DONE));
    }

    @Test
    void newToDoneRejected() {
        assertFalse(RequestService.isAllowedTransition(RequestState.NEW, RequestState.DONE));
    }

    @Test
    void backwardTransitionRejected() {
        assertFalse(RequestService.isAllowedTransition(RequestState.IN_PROGRESS, RequestState.NEW));
        assertFalse(RequestService.isAllowedTransition(RequestState.DONE, RequestState.IN_PROGRESS));
    }

    @Test
    void onlyThreeTransitionsAreAllowedInTotal() {
        int allowed = 0;
        for (RequestState from : RequestState.values()) {
            for (RequestState to : RequestState.values()) {
                if (RequestService.isAllowedTransition(from, to)) {
                    allowed++;
                }
            }
        }
        assertEquals(3, allowed);
    }
    @Test
    void roomValidationAcceptsCorrectRoom() {
        Room room = new Room(null, "101", 1, "Стандарт", 2, "Кондиціонер, телевізор");
        assertDoesNotThrow(() -> RoomService.validate(room));
    }

    @Test
    void roomValidationRejectsBlankNumber() {
        Room room = new Room(null, " ", 1, "Стандарт", 2, "Кондиціонер");
        assertThrows(IllegalArgumentException.class, () -> RoomService.validate(room));
    }

    @Test
    void roomValidationRejectsZeroCapacity() {
        Room room = new Room(null, "101", 1, "Стандарт", 0, "Кондиціонер");
        assertThrows(IllegalArgumentException.class, () -> RoomService.validate(room));
    }
    @Test
    void specialistValidationRejectsBlankName() {
        Specialist specialist = new Specialist(null, "", "Електрик", "+380500000001");
        assertThrows(IllegalArgumentException.class, () -> SpecialistService.validate(specialist));
    }
    @Test
    void requestValidationRejectsFactorOutOfRange() {
        Request request = new Request(null, 1, null, "Не працює кондиціонер", RequestState.NEW, 120, 10, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> RequestService.validate(request));
    }

    @Test
    void requestValidationRejectsBlankDescription() {
        Request request = new Request(null, 1, null, " ", RequestState.NEW, 10, 10, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> RequestService.validate(request));
    }
    @Test
    void executionRecordValidationRejectsZeroHours() {
        ExecutionRecord record = new ExecutionRecord(null, 1, 1, "2026-10-14", 0, "Діагностика");
        assertThrows(IllegalArgumentException.class, () -> ExecutionRecordService.validate(record));
    }

    @Test
    void duplicateKeyIgnoresCaseAndExtraSpaces() {
        assertTrue(DuplicateUtil.same("  Не працює   кондиціонер ", "не працює кондиціонер"));
    }

    @Test
    void duplicateKeyDistinguishesDifferentText() {
        assertFalse(DuplicateUtil.same("101", "102"));
    }

    @Test
    void phoneKeyComparesDigitsOnly() {
        assertEquals(DuplicateUtil.phoneKey("+380 50 000-00-01"), DuplicateUtil.phoneKey("380500000001"));
    }

    @Test
    void cleanCollapsesWhitespace() {
        assertEquals("Кондиціонер, телевізор", DuplicateUtil.clean("  Кондиціонер,   телевізор "));
    }

    @Test
    void excludedIdIsMinusOneWhenAdding() {
        assertEquals(-1L, DuplicateUtil.excluded(null));
        assertEquals(7L, DuplicateUtil.excluded(7L));
    }
}
