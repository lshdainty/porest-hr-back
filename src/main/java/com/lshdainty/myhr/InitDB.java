package com.lshdainty.myhr;

import com.lshdainty.myhr.domain.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

            User user1 = em.find(User.class, 1L);
            User user2 = em.find(User.class, 2L);
            User user3 = em.find(User.class, 3L);
            User user4 = em.find(User.class, 4L);
            User user5 = em.find(User.class, 5L);
            User user6 = em.find(User.class, 6L);

            Vacation user1Annual = Vacation.createVacation(user1, VacationType.ANNUAL, new BigDecimal("9.8750"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user1Annual);
            em.flush();
            List<VacationHistory> user1Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user1Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user1Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user1Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user1Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
                    1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 2, 3, 9, 0, 0),
                    1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 3, 17, 0, 0, 0),
                    1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "오전반차", VacationTimeType.MORNINGOFF,
                            LocalDateTime.of(now.getYear(), 10, 15, 9, 0, 0),
                    1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 12, 19, 14, 0, 0),
                    1L, "127.0.0.1")
            );
            for (VacationHistory annual : user1Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user2Annual = Vacation.createVacation(user2, VacationType.ANNUAL, new BigDecimal("11.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user2Annual);
            em.flush();
            List<VacationHistory> user2Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user2Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user2Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user2Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user2Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 4, 8, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 7, 25, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "3시간", VacationTimeType.MORNINGOFF,
                            LocalDateTime.of(now.getYear(), 9, 8, 9, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user2Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory annual : user2Annuals) {
                em.persist(annual);
            }

            Vacation user3Annual = Vacation.createVacation(user3, VacationType.ANNUAL, new BigDecimal("9.2500"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user3Annual);
            em.flush();
            List<VacationHistory> user3Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user3Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user3Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user3Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user3Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 3, 17, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "2시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user3Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory annual : user3Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user4Annual = Vacation.createVacation(user4, VacationType.ANNUAL, new BigDecimal("9.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user4Annual);
            em.flush();
            List<VacationHistory> user4Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user4Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user4Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user4Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user4Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 31, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 7, 0, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory annual : user4Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user5Annual = Vacation.createVacation(user5, VacationType.ANNUAL, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user5Annual);
            em.flush();
            List<VacationHistory> user5Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user5Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user5Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user5Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user5Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 7, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 6, 4, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 12, 26, 0, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory annual : user5Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user6Annual = Vacation.createVacation(user6, VacationType.ANNUAL, new BigDecimal("11.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user6Annual);
            em.flush();
            List<VacationHistory> user6Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user6Annual, "1분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user6Annual, "2분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user6Annual, "3분기 휴가", new BigDecimal("4.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user6Annual, "4분기 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 4, 8, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 7, 25, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "3시간", VacationTimeType.MORNINGOFF,
                            LocalDateTime.of(now.getYear(), 9, 8, 9, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user6Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory annual : user6Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user1Maternity = Vacation.createVacation(user1, VacationType.MATERNITY, new BigDecimal("3.0000"),
                    LocalDateTime.of(now.getYear(), 3, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 9, 30, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user1Maternity);
            em.flush();
            List<VacationHistory> user1Maternitys = List.of(
                    VacationHistory.createRegistVacationHistory(user1Maternity, "출산 휴가", new BigDecimal("10.0000"), 0L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 4, 0, 0, 0),
                            1L, "127.0.0.1"),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 11, 0, 0, 0),
                            1L, "127.0.0.1")
            );
            for (VacationHistory maternity : user1Maternitys) {
                em.persist(maternity);
            }
            em.flush();

            Vacation user1Overtime = Vacation.createVacation(user1, VacationType.OVERTIME, new BigDecimal("0.5000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user1Overtime);
            List<VacationHistory> user1Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.2500"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1")
            );
            for (VacationHistory overtime : user1Overtimes) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user2Wedding = Vacation.createVacation(user2, VacationType.WEDDING, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 2, 17, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 8, 17, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user2Wedding);
            List<VacationHistory> user2Weddings = List.of(
                    VacationHistory.createRegistVacationHistory(user2Wedding, "결혼 휴가", new BigDecimal("5.0000"), 0L, "127.0.0.1")
            );
            for (VacationHistory wedding : user2Weddings) {
                em.persist(wedding);
            }
            em.flush();

            Vacation user3Bereavement = Vacation.createVacation(user3, VacationType.BEREAVEMENT, new BigDecimal("3.0000"),
                    LocalDateTime.of(now.getYear(), 4, 4, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 10, 4, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user3Bereavement);
            List<VacationHistory> user3Bereavements = List.of(
                    VacationHistory.createRegistVacationHistory(user3Bereavement, "상조 휴가", new BigDecimal("3.0000"), 0L, "127.0.0.1")
            );
            for (VacationHistory bereavement : user3Bereavements) {
                em.persist(bereavement);
            }
            em.flush();

            Vacation user4Wedding = Vacation.createVacation(user4, VacationType.WEDDING, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 8, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+1, 2, 28, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user4Wedding);
            List<VacationHistory> user4Weddings = List.of(
                    VacationHistory.createRegistVacationHistory(user4Wedding, "결혼 휴가", new BigDecimal("5.0000"), 0L, "127.0.0.1")
            );
            for (VacationHistory wedding : user4Weddings) {
                em.persist(wedding);
            }
            em.flush();

            Vacation user5Overtime = Vacation.createVacation(user5, VacationType.OVERTIME, new BigDecimal("0.3750"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user5Overtime);
            List<VacationHistory> user5Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1"),
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1")
            );
            for (VacationHistory overtime : user5Overtimes) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user6Overtime = Vacation.createVacation(user6, VacationType.OVERTIME, new BigDecimal("0.1250"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                    0L, "127.0.0.1");
            em.persist(user6Overtime);
            List<VacationHistory> user6Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user6Overtime, "OT", new BigDecimal("0.1250"), 0L, "127.0.0.1")
            );
            for (VacationHistory overtime : user6Overtimes) {
                em.persist(overtime);
            }
            em.flush();
        }

        public void initSetSchedule() {
            LocalDateTime now = LocalDateTime.now();
            saveSchedule(1L, "교육", ScheduleType.EDUCATION,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 3, 23, 59, 59));
            saveSchedule(1L, "예비군", ScheduleType.DEFENSE,
                    LocalDateTime.of(now.getYear(), 2, 23, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 2, 28, 23, 59, 59));
            saveSchedule(1L, "출장", ScheduleType.BUSINESSTRIP,
                    LocalDateTime.of(now.getYear(), 3, 30, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59));
            saveSchedule(1L, "건강검진", ScheduleType.HEALTHCHECK,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
            saveSchedule(1L, "생일", ScheduleType.BIRTHDAY,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
            saveSchedule(1L, "출장", ScheduleType.BUSINESSTRIP,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
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

        public void saveSchedule(Long userNo, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate) {
            User user = em.find(User.class, userNo);
            Schedule schedule = Schedule.createSchedule(user, desc, type, startDate, endDate, 0L, "127.0.0.1");
            em.persist(schedule);
        }

        public void saveDues(String userName, int amount, DuesType type, String date, String detail) {
            Dues dues = Dues.createDues(userName, amount, type, date, detail);
            em.persist(dues);
        }
    }
}
