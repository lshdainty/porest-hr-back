package com.lshdainty.myhr;

import com.lshdainty.myhr.domain.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initSetMember();
        initService.initSetHoliday();
        initService.initSetVacation();
        initService.initSetSchedule();
        initService.initSetDues();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void initSetMember() {
            saveMember("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
            saveMember("김서연", "19701026", "8 ~ 5", "BP", "N");
            saveMember("김지후", "19740115", "10 ~ 7", "BP", "Y");
            saveMember("이준우", "19800430", "9 ~ 6", "BP", "N");
            saveMember("조민서", "19921220", "9 ~ 6", "ADMIN", "N");
            saveMember("이하은", "18850902", "8 ~ 5", "ADMIN", "N");
        }

        public void initSetHoliday() {
            saveHoliday("신정", "20250101", HolidayType.PUBLIC);
            saveHoliday("임시공휴일(설날)", "20250127", HolidayType.PUBLIC);
            saveHoliday("설날연휴", "20250128", HolidayType.PUBLIC);
            saveHoliday("설날", "20250129", HolidayType.PUBLIC);
            saveHoliday("설날연휴", "20250130", HolidayType.PUBLIC);
            saveHoliday("삼일절", "20250301", HolidayType.PUBLIC);
            saveHoliday("대체공휴일(삼일절)", "20250303", HolidayType.PUBLIC);
            saveHoliday("근로자의 날", "20250501", HolidayType.PUBLIC);
            saveHoliday("어린이날", "20250505", HolidayType.PUBLIC);
            saveHoliday("대체공휴일(석가탄신일)", "20250506", HolidayType.PUBLIC);
            saveHoliday("임시공휴일(제 21대 대선)", "20250603", HolidayType.PUBLIC);
            saveHoliday("현충일", "20250606", HolidayType.PUBLIC);
            saveHoliday("광복절", "20250815", HolidayType.PUBLIC);
            saveHoliday("개천절", "20251003", HolidayType.PUBLIC);
            saveHoliday("추석연휴", "20251005", HolidayType.PUBLIC);
            saveHoliday("추석", "20251006", HolidayType.PUBLIC);
            saveHoliday("추석연휴", "20251007", HolidayType.PUBLIC);
            saveHoliday("대체공휴일(추석)", "20251008", HolidayType.PUBLIC);
            saveHoliday("한글날", "20251009", HolidayType.PUBLIC);
            saveHoliday("크리스마스", "20251225", HolidayType.PUBLIC);

            saveHoliday("권장휴가", "20250131", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250304", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250404", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250502", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250523", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250704", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250814", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20250905", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20251010", HolidayType.RECOMMEND);
            saveHoliday("권장휴가", "20251114", HolidayType.RECOMMEND);
        }

        public void initSetVacation() {
            LocalDateTime now = LocalDateTime.now();

            saveVacation(1L, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(2L, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(3L, "1분기 휴가", "작년 하루 사용", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(4L, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(5L, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(6L, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(1L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));
            saveVacation(2L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));
            saveVacation(3L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));
            saveVacation(4L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));
            saveVacation(5L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));
            saveVacation(6L, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0));

            saveVacation(1L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));
            saveVacation(2L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));
            saveVacation(3L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));
            saveVacation(4L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));
            saveVacation(5L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));
            saveVacation(6L, "3분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 7, 1, 0, 0, 0));

            saveVacation(1L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));
            saveVacation(2L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));
            saveVacation(3L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));
            saveVacation(4L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));
            saveVacation(5L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));
            saveVacation(6L, "4분기 휴가", "", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 10, 1, 0, 0, 0));

            saveVacation(1L, "출산 휴가", "출산 추가 휴가 부여", VacationType.ADDED, new BigDecimal("80"), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).plusMonths(6), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("3"), LocalDateTime.of(2025, 1, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("1"), LocalDateTime.of(2025, 1, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("6"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("5"), LocalDateTime.of(2025, 2, 15, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("1"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(1L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("1"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(2L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("2"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(2L, "결혼 휴가", "결혼 추가 휴가 부여", VacationType.ADDED, new BigDecimal("40"), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).plusMonths(6), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0));
            saveVacation(2L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("2"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(3L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("1"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
            saveVacation(3L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("1"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(4L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("6"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(5L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("2"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));

            saveVacation(6L, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("10"), LocalDateTime.of(2025, 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0));
        }

        public void initSetSchedule() {
            LocalDateTime now = LocalDateTime.now();

            saveSchedule(1L, 1L, "휴가", ScheduleType.DAYOFF, LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0), LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59));
            saveSchedule(1L, 1L, "1시간", ScheduleType.ONETIMEOFF, LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0), LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0));
            saveSchedule(1L, 1L, "휴가", ScheduleType.DAYOFF, LocalDateTime.of(now.getYear(), 3, 17, 0, 0, 0), LocalDateTime.of(now.getYear(), 3, 17, 23, 59, 59));
            saveSchedule(1L, 25L, "2시간", ScheduleType.TWOTIMEOFF, LocalDateTime.of(now.getYear(), 4, 27, 9, 0, 0), LocalDateTime.of(now.getYear(), 4, 27, 11, 0, 0));
            saveSchedule(1L, 25L, "6시간", ScheduleType.SIXTIMEOFF, LocalDateTime.of(now.getYear(), 6, 5, 11, 0, 0), LocalDateTime.of(now.getYear(), 6, 5, 18, 0, 0));
            saveSchedule(1L, 25L, "3시간", ScheduleType.THREETIMEOFF, LocalDateTime.of(now.getYear(), 7, 18, 9, 0, 0), LocalDateTime.of(now.getYear(), 7, 18, 12, 0, 0));
            saveSchedule(1L, 25L,"5시간", ScheduleType.FIVETIMEOFF, LocalDateTime.of(now.getYear(), 8, 30, 13, 0, 0), LocalDateTime.of(now.getYear(), 8, 30, 18, 0, 0));
            saveSchedule(1L, 7L, "오전반차", ScheduleType.MORNINGOFF, LocalDateTime.of(now.getYear(), 10, 15, 9, 0, 0), LocalDateTime.of(now.getYear(), 10, 15, 14, 0, 0));
            saveSchedule(1L, 7L, "오후반차", ScheduleType.AFTERNOONOFF, LocalDateTime.of(now.getYear(), 12, 20, 14, 0, 0), LocalDateTime.of(now.getYear(), 12, 20, 18, 0, 0));
            saveSchedule(1L, 25L, "휴가", ScheduleType.DAYOFF, LocalDateTime.of(now.getYear(), 4, 30, 0, 0, 0), LocalDateTime.of(now.getYear(), 5, 11, 23, 59, 59));
            saveSchedule(1L, null, "교육", ScheduleType.EDUCATION, LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 5, 3, 23, 59, 59));
            saveSchedule(1L, null, "예비군", ScheduleType.DEFENSE, LocalDateTime.of(now.getYear(), 2, 23, 0, 0, 0), LocalDateTime.of(now.getYear(), 2, 28, 23, 59, 59));
            saveSchedule(1L, null, "출장", ScheduleType.BUSINESSTRIP, LocalDateTime.of(now.getYear(), 3, 30, 0, 0, 0), LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59));
            saveSchedule(1L, null, "건강검진", ScheduleType.HEALTHCHECK, LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
            saveSchedule(1L, null, "생일", ScheduleType.BIRTHDAY, LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
            saveSchedule(1L, null, "출장", ScheduleType.BUSINESSTRIP, LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
        }

        public void initSetDues() {
            saveDues("이서준", 10000, DuesType.PLUS, "20250104", "생일비");
            saveDues("김서연", 10000, DuesType.PLUS, "20250104", "생일비");
            saveDues("김지후", 10000, DuesType.PLUS, "20250104", "생일비");
            saveDues("이준우", 10000, DuesType.PLUS, "20250104", "생일비");
            saveDues("조민서", 80000, DuesType.MINUS, "20250131", "생일비 출금");
            saveDues("이하은", 10000, DuesType.PLUS, "20250204", "생일비");
            saveDues("김서연", 10000, DuesType.PLUS, "20250204", "생일비");
            saveDues("김지후", 10000, DuesType.PLUS, "20250204", "생일비");
            saveDues("이준우", 10000, DuesType.PLUS, "20250204", "생일비");
            saveDues("조민서", 30000, DuesType.MINUS, "20250228", "생일비 출금");
        }

        public void saveMember(String name, String birth, String workTime, String employ, String lunar) {
            User user = User.createUser(name, birth, employ, workTime, lunar);
            em.persist(user);
        }

        public void saveHoliday(String name, String date, HolidayType type) {
            Holiday holiday = Holiday.createHoliday(name, date, type);
            em.persist(holiday);
        }

        public void saveVacation(Long userNo, String name, String desc, VacationType type, BigDecimal grantTime, LocalDateTime expiryDate, LocalDateTime occurDate) {
            User user = em.find(User.class, userNo);
            Vacation vacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");
            em.persist(vacation);
        }

        public void saveSchedule(Long userNo, Long vacationId, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate) {
            User user = em.find(User.class, userNo);
            Vacation vacation = null;
            Schedule schedule = null;

            if (Objects.nonNull(vacationId)) {
                vacation = em.find(Vacation.class, vacationId);
                schedule = Schedule.createSchedule(user, vacation, desc, type, startDate, endDate, 0L, "127.0.0.1");
            } else {
                schedule = Schedule.createSchedule(user, null, desc, type, startDate, endDate, 0L, "127.0.0.1");
            }

            em.persist(schedule);
        }

        public void saveDues(String userName, int amount, DuesType type, String date, String detail) {
            Dues dues = Dues.createDues(userName, amount, type, date, detail);
            em.persist(dues);
        }
    }
}
