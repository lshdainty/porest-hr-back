package com.lshdainty.porest;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.type.RoleType;
import com.lshdainty.porest.vacation.domain.Vacation;
import com.lshdainty.porest.vacation.domain.VacationHistory;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.type.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initSetMember();
        initService.initSetDepartment();
        initService.initSetUserDepartment();
        initService.initSetHoliday();
        initService.initSetVacation();
        initService.initSetSchedule();
        initService.initSetDues();
        initService.initSetVacationPolicy();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        private final BCryptPasswordEncoder passwordEncoder;

        public void initSetMember() {
            saveMember("user1", "이서준", "aaa@naver.com", LocalDate.of(1970, 7, 23), OriginCompanyType.SKAX, "9 ~ 6", YNType.N);
            saveMember("user2", "김서연", "bbb@naver.com", LocalDate.of(1970, 10, 26), OriginCompanyType.DTOL, "8 ~ 5",  YNType.N);
            saveMember("user3", "김지후", "ccc@naver.com", LocalDate.of(1974, 1, 15), OriginCompanyType.INSIGHTON, "10 ~ 7", YNType.Y);
            saveMember("user4", "이준우", "ddd@naver.com", LocalDate.of(1980, 4, 30), OriginCompanyType.BIGXDATA, "9 ~ 6", YNType.N);
            saveMember("user5", "조민서", "eee@naver.com", LocalDate.of(1992, 12, 20), OriginCompanyType.CNTHOTH, "10 ~ 7", YNType.N);
            saveMember("user6", "이하은", "fff@naver.com", LocalDate.of(1885, 9, 2), OriginCompanyType.SKAX, "8 ~ 5", YNType.N);

            User user1 = em.find(User.class, "user1");
            User user2 = em.find(User.class, "user2");
            User user3 = em.find(User.class, "user3");
            User user4 = em.find(User.class, "user4");
            User user5 = em.find(User.class, "user5");
            User user6 = em.find(User.class, "user6");

            user1.updateUser(user1.getName(), user1.getEmail(), RoleType.ADMIN, user1.getBirth(), user1.getCompany(), user1.getWorkTime(), user1.getLunarYN(), null, null);
            user3.updateUser(user3.getName(), user3.getEmail(), RoleType.ADMIN, user3.getBirth(), user3.getCompany(), user3.getWorkTime(), user3.getLunarYN(), null, null);

            user1.completeRegistration(user1.getBirth(), user1.getLunarYN());
            user2.completeRegistration(user2.getBirth(), user2.getLunarYN());
            user3.completeRegistration(user3.getBirth(), user3.getLunarYN());
            user4.completeRegistration(user4.getBirth(), user4.getLunarYN());
            user5.completeRegistration(user5.getBirth(), user5.getLunarYN());
            user6.completeRegistration(user6.getBirth(), user6.getLunarYN());
        }

        public void initSetDepartment() {
            Company company = Company.createCompany("SKC", "SKC", "SKC입니다.");
            em.persist(company);

            Department parent = saveDepartment("dept", "생산운영", null, 0L, "mes 생산운영 파트입니다.", null, company);
            saveDepartment("Olive", "Olive", parent, 1L, "울산 운영 부서입니다.", null, company);
            Department mes = saveDepartment("G-MES", "G-MES", parent, 1L, "G-MES 부서입니다.", null, company);
            saveDepartment("G-MESJ", "G-MESJ", mes, 2L, "정읍 G-MES 파트입니다.", null, company);
            saveDepartment("G-MESM", "G-MESM", mes, 2L, "말련 G-MES 파트입니다.", null, company);
            saveDepartment("G-SCM", "G-SCM", parent, 1L, "G-SCM 부서입니다.", null, company);
            Department dt = saveDepartment("DT", "DT", parent, 1L, "SKC DT 부서입니다.", null, company);
            saveDepartment("myDATA", "myDATA", dt, 2L, "myDATA 파트입니다.", null, company);
            saveDepartment("Tableau", "Tableau", dt, 2L, "Tableau 파트입니다.", null, company);
            saveDepartment("AOI", "AOI", dt, 2L, "AOI 파트입니다.", null, company);
            saveDepartment("CMP", "CMP", parent, 1L, "CMP 부서입니다.", null, company);
        }

        public void initSetUserDepartment() {
            User user1 = em.find(User.class, "user1");
            User user2 = em.find(User.class, "user2");
            User user3 = em.find(User.class, "user3");
            User user4 = em.find(User.class, "user4");
            User user5 = em.find(User.class, "user5");
            User user6 = em.find(User.class, "user6");

            Department dept = findDepartmentByName("dept");
            Department GMESJ = findDepartmentByName("G-MESJ");
            Department GMESM = findDepartmentByName("G-MESM");
            Department DT = findDepartmentByName("DT");
            Department myDATA = findDepartmentByName("myDATA");
            Department tableau = findDepartmentByName("Tableau");

            UserDepartment ud1 = UserDepartment.createUserDepartment(user1, myDATA, YNType.Y);
            UserDepartment ud2 = UserDepartment.createUserDepartment(user2, tableau, YNType.Y);
            UserDepartment ud3 = UserDepartment.createUserDepartment(user3, DT, YNType.Y);
            UserDepartment ud4 = UserDepartment.createUserDepartment(user4, GMESJ, YNType.Y);
            UserDepartment ud5 = UserDepartment.createUserDepartment(user5, GMESM, YNType.Y);
            UserDepartment ud6 = UserDepartment.createUserDepartment(user6, dept, YNType.Y);
            UserDepartment ud7 = UserDepartment.createUserDepartment(user1, GMESJ, YNType.N);

            em.persist(ud1);
            em.persist(ud2);
            em.persist(ud3);
            em.persist(ud4);
            em.persist(ud5);
            em.persist(ud6);
            em.persist(ud7);
        }

        private Department findDepartmentByName(String name) {
            return em.createQuery(
                    "SELECT d FROM Department d WHERE d.name = :name AND d.isDeleted = :isDeleted", Department.class)
                    .setParameter("name", name)
                    .setParameter("isDeleted", YNType.N)
                    .getSingleResult();
        }

        public void initSetHoliday() {
            saveHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🌅");
            saveHoliday("임시공휴일(설날)", "20250127", HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null, YNType.N, null);
            saveHoliday("설날연휴", "20250128", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20241231", YNType.Y, "🧧");
            saveHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250101", YNType.Y, "🧧");
            saveHoliday("설날연휴", "20250130", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250102", YNType.Y, "🧧");
            saveHoliday("삼일절", "20250301", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🇰🇷");
            saveHoliday("대체공휴일(삼일절)", "20250303", HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null, YNType.N, null);
            saveHoliday("근로자의 날", "20250501", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🧑‍💻");
            saveHoliday("어린이날", "20250505", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "👶");
            saveHoliday("석가탄신일", "20250505", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250408", YNType.Y, "🪷");
            saveHoliday("대체공휴일(석가탄신일)", "20250506", HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null, YNType.N, null);
            saveHoliday("임시공휴일(제 21대 대선)", "20250603", HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null, YNType.N, "🗳");
            saveHoliday("현충일", "20250606", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🪖");
            saveHoliday("광복절", "20250815", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🇰🇷");
            saveHoliday("개천절", "20251003", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🫅");
            saveHoliday("추석연휴", "20251005", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250814", YNType.Y, "🎑");
            saveHoliday("추석", "20251006", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250815", YNType.Y, "🎑");
            saveHoliday("추석연휴", "20251007", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "20250816", YNType.Y, "🎑");
            saveHoliday("대체공휴일(추석)", "20251008", HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null, YNType.N, null);
            saveHoliday("한글날", "20251009", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "📚");
            saveHoliday("크리스마스", "20251225", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎄");

            saveHoliday("권장휴가", "20250131", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250304", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250404", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250502", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250523", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250704", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250814", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20250905", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20251010", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
            saveHoliday("권장휴가", "20251114", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.N, "🏖");
        }

        public void initSetVacation() {
            LocalDateTime now = LocalDateTime.now();

            User user1 = em.find(User.class, "user1");
            User user2 = em.find(User.class, "user2");
            User user3 = em.find(User.class, "user3");
            User user4 = em.find(User.class, "user4");
            User user5 = em.find(User.class, "user5");
            User user6 = em.find(User.class, "user6");

            Vacation user1Annual = Vacation.createVacation(user1, VacationType.ANNUAL, new BigDecimal("9.8750"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user1Annual);
            em.flush();
            List<VacationHistory> user1Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user1Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 3, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 2, 3, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 3, 17, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "오전반차", VacationTimeType.MORNINGOFF,
                            LocalDateTime.of(now.getYear(), 10, 15, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 12, 19, 14, 0, 0))
            );
            for (VacationHistory annual : user1Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user2Annual = Vacation.createVacation(user2, VacationType.ANNUAL, new BigDecimal("11.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user2Annual);
            em.flush();
            List<VacationHistory> user2Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user2Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user2Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user2Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user2Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 4, 8, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user2Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 7, 25, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user2Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user2Annual, "3시간", VacationTimeType.THREETIMEOFF,
                            LocalDateTime.of(now.getYear(), 9, 8, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user2Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0))
            );
            for (VacationHistory annual : user2Annuals) {
                em.persist(annual);
            }

            Vacation user3Annual = Vacation.createVacation(user3, VacationType.ANNUAL, new BigDecimal("9.2500"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user3Annual);
            em.flush();
            List<VacationHistory> user3Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user3Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user3Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user3Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user3Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 3, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 3, 17, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "2시간", VacationTimeType.TWOTIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user3Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0))
            );
            for (VacationHistory annual : user3Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user4Annual = Vacation.createVacation(user4, VacationType.ANNUAL, new BigDecimal("9.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user4Annual);
            em.flush();
            List<VacationHistory> user4Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user4Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user4Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user4Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user4Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 3, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 31, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 7, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 8, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user4Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 9, 0, 0, 0))
            );
            for (VacationHistory annual : user4Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user5Annual = Vacation.createVacation(user5, VacationType.ANNUAL, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user5Annual);
            em.flush();
            List<VacationHistory> user5Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user5Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user5Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user5Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user5Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 1, 3, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 7, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 8, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 5, 9, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 4, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 5, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user5Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 12, 26, 0, 0, 0))
            );
            for (VacationHistory annual : user5Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user6Annual = Vacation.createVacation(user6, VacationType.ANNUAL, new BigDecimal("11.0000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user6Annual);
            em.flush();
            List<VacationHistory> user6Annuals = List.of(
                    VacationHistory.createRegistVacationHistory(user6Annual, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user6Annual, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user6Annual, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user6Annual, "4분기 휴가", new BigDecimal("3.0000")),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 4, 8, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user6Annual, "1시간", VacationTimeType.ONETIMEOFF,
                            LocalDateTime.of(now.getYear(), 4, 9, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 7, 25, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user6Annual, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user6Annual, "3시간", VacationTimeType.THREETIMEOFF,
                            LocalDateTime.of(now.getYear(), 9, 8, 9, 0, 0)),
                    VacationHistory.createUseVacationHistory(user6Annual, "오후반차", VacationTimeType.AFTERNOONOFF,
                            LocalDateTime.of(now.getYear(), 10, 10, 14, 0, 0))
            );
            for (VacationHistory annual : user6Annuals) {
                em.persist(annual);
            }
            em.flush();

            Vacation user1Maternity = Vacation.createVacation(user1, VacationType.MATERNITY, new BigDecimal("3.0000"),
                    LocalDateTime.of(now.getYear(), 3, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 9, 1, 23, 59, 59));
            em.persist(user1Maternity);
            em.flush();
            List<VacationHistory> user1Maternitys = List.of(
                    VacationHistory.createRegistVacationHistory(user1Maternity, "출산 휴가", new BigDecimal("10.0000")),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 2, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 4, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 6, 5, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 11, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 12, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 13, 0, 0, 0)),
                    VacationHistory.createUseVacationHistory(user1Maternity, "연차", VacationTimeType.DAYOFF,
                            LocalDateTime.of(now.getYear(), 8, 14, 0, 0, 0))
            );
            for (VacationHistory maternity : user1Maternitys) {
                em.persist(maternity);
            }
            em.flush();

            Vacation user1Overtime = Vacation.createVacation(user1, VacationType.OVERTIME, new BigDecimal("0.5000"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user1Overtime);
            List<VacationHistory> user1Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.1250")),
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.2500")),
                    VacationHistory.createRegistVacationHistory(user1Overtime, "OT", new BigDecimal("0.1250"))
            );
            for (VacationHistory overtime : user1Overtimes) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user2Wedding = Vacation.createVacation(user2, VacationType.WEDDING, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 2, 17, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 8, 17, 23, 59, 59));
            em.persist(user2Wedding);
            List<VacationHistory> user2Weddings = List.of(
                    VacationHistory.createRegistVacationHistory(user2Wedding, "결혼 휴가", new BigDecimal("5.0000"))
            );
            for (VacationHistory wedding : user2Weddings) {
                em.persist(wedding);
            }
            em.flush();

            Vacation user3Bereavement = Vacation.createVacation(user3, VacationType.BEREAVEMENT, new BigDecimal("3.0000"),
                    LocalDateTime.of(now.getYear(), 4, 4, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 10, 4, 23, 59, 59));
            em.persist(user3Bereavement);
            List<VacationHistory> user3Bereavements = List.of(
                    VacationHistory.createRegistVacationHistory(user3Bereavement, "상조 휴가", new BigDecimal("3.0000"))
            );
            for (VacationHistory bereavement : user3Bereavements) {
                em.persist(bereavement);
            }
            em.flush();

            Vacation user4Wedding = Vacation.createVacation(user4, VacationType.WEDDING, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear(), 8, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+1, 2, 1, 23, 59, 59));
            em.persist(user4Wedding);
            List<VacationHistory> user4Weddings = List.of(
                    VacationHistory.createRegistVacationHistory(user4Wedding, "결혼 휴가", new BigDecimal("5.0000"))
            );
            for (VacationHistory wedding : user4Weddings) {
                em.persist(wedding);
            }
            em.flush();

            Vacation user5Overtime = Vacation.createVacation(user5, VacationType.OVERTIME, new BigDecimal("0.3750"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user5Overtime);
            List<VacationHistory> user5Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250")),
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250")),
                    VacationHistory.createRegistVacationHistory(user5Overtime, "OT", new BigDecimal("0.1250"))
            );
            for (VacationHistory overtime : user5Overtimes) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user6Overtime = Vacation.createVacation(user6, VacationType.OVERTIME, new BigDecimal("0.1250"),
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59));
            em.persist(user6Overtime);
            List<VacationHistory> user6Overtimes = List.of(
                    VacationHistory.createRegistVacationHistory(user6Overtime, "OT", new BigDecimal("0.1250"))
            );
            for (VacationHistory overtime : user6Overtimes) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user1Annual26 = Vacation.createVacation(user1, VacationType.ANNUAL, new BigDecimal("15.0000"),
                    LocalDateTime.of(now.getYear()+1, 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+1, 12, 31, 23, 59, 59));
            em.persist(user1Annual26);
            em.flush();
            List<VacationHistory> user1Annuals26 = List.of(
                    VacationHistory.createRegistVacationHistory(user1Annual26, "1분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual26, "2분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual26, "3분기 휴가", new BigDecimal("4.0000")),
                    VacationHistory.createRegistVacationHistory(user1Annual26, "4분기 휴가", new BigDecimal("3.0000"))
            );
            for (VacationHistory annual : user1Annuals26) {
                em.persist(annual);
            }
            em.flush();

            Vacation user1Maternity26 = Vacation.createVacation(user1, VacationType.MATERNITY, new BigDecimal("10.0000"),
                    LocalDateTime.of(now.getYear()+1, 10, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+2, 4, 1, 23, 59, 59));
            em.persist(user1Maternity26);
            em.flush();
            List<VacationHistory> user1Maternitys26 = List.of(
                    VacationHistory.createRegistVacationHistory(user1Maternity26, "출산 휴가", new BigDecimal("10.0000"))
            );
            for (VacationHistory maternity : user1Maternitys26) {
                em.persist(maternity);
            }
            em.flush();

            Vacation user1Overtime26 = Vacation.createVacation(user1, VacationType.OVERTIME, new BigDecimal("0.5000"),
                    LocalDateTime.of(now.getYear()+1, 1, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+1, 12, 31, 23, 59, 59));
            em.persist(user1Overtime26);
            List<VacationHistory> user1Overtimes26 = List.of(
                    VacationHistory.createRegistVacationHistory(user1Overtime26, "OT", new BigDecimal("0.1250")),
                    VacationHistory.createRegistVacationHistory(user1Overtime26, "OT", new BigDecimal("0.3750"))
            );
            for (VacationHistory overtime : user1Overtimes26) {
                em.persist(overtime);
            }
            em.flush();

            Vacation user1Wedding26 = Vacation.createVacation(user1, VacationType.WEDDING, new BigDecimal("5.0000"),
                    LocalDateTime.of(now.getYear()+1, 2, 17, 0, 0, 0),
                    LocalDateTime.of(now.getYear()+1, 8, 17, 23, 59, 59));
            em.persist(user1Wedding26);
            List<VacationHistory> user1Weddings26 = List.of(
                    VacationHistory.createRegistVacationHistory(user1Wedding26, "결혼 휴가", new BigDecimal("5.0000"))
            );
            for (VacationHistory wedding : user1Weddings26) {
                em.persist(wedding);
            }
            em.flush();
        }

        public void initSetSchedule() {
            LocalDateTime now = LocalDateTime.now();
            saveSchedule("user1", "교육", ScheduleType.EDUCATION,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 3, 23, 59, 59));
            saveSchedule("user1", "예비군", ScheduleType.DEFENSE,
                    LocalDateTime.of(now.getYear(), 2, 23, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 2, 28, 23, 59, 59));
            saveSchedule("user1", "출장", ScheduleType.BUSINESSTRIP,
                    LocalDateTime.of(now.getYear(), 3, 30, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59));
            saveSchedule("user1", "건강검진(반차)", ScheduleType.HEALTHCHECKHALF,
                    LocalDateTime.of(now.getYear(), 5, 1, 9, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 14, 0, 0));
            saveSchedule("user1", "생일", ScheduleType.BIRTHDAY,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
            saveSchedule("user1", "출장", ScheduleType.BUSINESSTRIP,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
        }

        public void initSetDues() {
            saveDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비");
            saveDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비");
            saveDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비");
            saveDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비");
            saveDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250131", "생일비 출금");
            saveDues("이하은", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비");
            saveDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비");
            saveDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비");
            saveDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비");
            saveDues("조민서", 30000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250228", "생일비 출금");
            saveDues("이서준", 30000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250128", "운영비 입금");
            saveDues("김서연", 30000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250428", "운영비 입금");
            saveDues("김지후", 10000L, DuesType.FINE, DuesCalcType.PLUS, "20250728", "운영비 입금");
            saveDues("조민서", 20000L, DuesType.FINE, DuesCalcType.PLUS, "20250728", "운영비 출금");
            saveDues("이준우", 10000L, DuesType.FINE, DuesCalcType.PLUS, "20250728", "운영비 출금");
            saveDues("이하은", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250728", "운영비 출금");
            saveDues("조민서", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250728", "운영비 출금");
            saveDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250704", "생일비");
        }

        public void initSetVacationPolicy() {
            LocalDateTime now = LocalDateTime.now();

            // 관리자 부여용 휴가정책 (MANUAL_GRANT - firstGrantDate, isRecurring, maxGrantCount 모두 null)
            saveVacationPolicy("연차(관리자용)", "연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, new BigDecimal("15.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("1분기 연차(관리자용)", "1분기 연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, new BigDecimal("4.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("2분기 연차(관리자용)", "2분기 연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, new BigDecimal("4.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("3분기 연차(관리자용)", "3분기 연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, new BigDecimal("4.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("4분기 연차(관리자용)", "4분기 연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, new BigDecimal("4.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("OT(관리자용)", "연장 근무에 대한 보상 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.OVERTIME, GrantMethod.MANUAL_GRANT, null, null, null, null, null, null, null, null);
            saveVacationPolicy("건강검진", "건강검진 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.HEALTH, GrantMethod.MANUAL_GRANT, new BigDecimal("0.5000"), null, null, null, null, null, null, null);
            saveVacationPolicy("동원훈련(관리자용)", "동원 훈련에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ARMY, GrantMethod.MANUAL_GRANT, new BigDecimal("3.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("동미참훈련(관리자용)", "동미참 훈련에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ARMY, GrantMethod.MANUAL_GRANT, new BigDecimal("1.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("예비군(관리자용)", "예비군 훈련에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ARMY, GrantMethod.MANUAL_GRANT, new BigDecimal("1.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("예비군(반차)(관리자용)", "예비군 훈련에 대한 반차 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.ARMY, GrantMethod.MANUAL_GRANT, new BigDecimal("0.5000"), null, null, null, null, null, null, null);
            saveVacationPolicy("결혼(관리자용)", "결혼에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.WEDDING, GrantMethod.MANUAL_GRANT, new BigDecimal("5.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("출산(관리자용)", "출산에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.MATERNITY, GrantMethod.MANUAL_GRANT, new BigDecimal("10.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("조사(관리자용)", "부친상, 모친상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, new BigDecimal("5.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("조사(관리자용)", "빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, new BigDecimal("3.0000"), null, null, null, null, null, null, null);

            // ===== 반복 부여 휴가 정책 (REPEAT_GRANT) =====

            // YEARLY 예제들
            saveVacationPolicy("연차", "연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("15.0000"), RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("1분기 연차", "1분기 연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("2분기 연차", "2분기 연차 정책입니다. 매년 4월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), RepeatUnit.YEARLY, 1, 4, 1, LocalDateTime.of(now.getYear(), 4, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("3분기 연차", "3분기 연차 정책입니다. 매년 7월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), RepeatUnit.YEARLY, 1, 7, 1, LocalDateTime.of(now.getYear(), 7, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("4분기 연차", "4분기 연차 정책입니다. 매년 10월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), RepeatUnit.YEARLY, 1, 10, 1, LocalDateTime.of(now.getYear(), 10, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("생일 휴가", "매년 생일에 자동 부여되는 휴가입니다. 매년 3월 15일에 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), RepeatUnit.YEARLY, 1, 3, 15, LocalDateTime.of(now.getYear(), 3, 15, 0, 0), YNType.Y, null);
            saveVacationPolicy("하계 휴가", "매년 6월에 자동 부여되는 하계 휴가입니다. 첫 부여일의 일자(15일) 사용.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), RepeatUnit.YEARLY, 1, 6, null, LocalDateTime.of(now.getYear(), 6, 15, 0, 0), YNType.Y, null);
            saveVacationPolicy("7년 근속 휴가", "7년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("5.0000"), RepeatUnit.YEARLY, 7, 1, 1, LocalDateTime.of(now.getYear() + 7, 1, 1, 0, 0), YNType.N, 1);
            saveVacationPolicy("3년 근속 휴가", "3년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), RepeatUnit.YEARLY, 3, 1, 1, LocalDateTime.of(now.getYear() + 3, 1, 1, 0, 0), YNType.N, 1);

            // MONTHLY 예제들
            saveVacationPolicy("매월 리프레시 휴가", "매월 1일 자동 부여되는 리프레시 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), RepeatUnit.MONTHLY, 1, null, 1, LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("매월 정기 휴가", "매월 15일 자동 부여되는 정기 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), RepeatUnit.MONTHLY, 1, null, 15, LocalDateTime.of(now.getYear(), now.getMonthValue(), 15, 0, 0), YNType.Y, null);
            saveVacationPolicy("2개월마다 휴가", "2개월마다 첫 부여일의 일자에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), RepeatUnit.MONTHLY, 2, null, null, LocalDateTime.of(now.getYear(), now.getMonthValue(), 10, 0, 0), YNType.Y, null);

            // QUARTERLY 예제들
            saveVacationPolicy("분기별 휴가", "매 분기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), RepeatUnit.QUARTERLY, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("분기별 건강 휴가", "매 분기 15일에 자동 부여되는 건강 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.5000"), RepeatUnit.QUARTERLY, 1, null, 15, LocalDateTime.of(now.getYear(), 1, 15, 0, 0), YNType.Y, null);
            saveVacationPolicy("분기별 특별 휴가", "매 분기 첫 부여일의 일자(20일)에 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), RepeatUnit.QUARTERLY, 1, null, null, LocalDateTime.of(now.getYear(), 1, 20, 0, 0), YNType.Y, null);

            // HALF 예제들
            saveVacationPolicy("반기별 휴가", "매 반기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), RepeatUnit.HALF, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null);
            saveVacationPolicy("반기별 리프레시", "매 반기 31일에 자동 부여되는 휴가입니다. 월말이 31일 미만이면 해당 월 마지막 날 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.5000"), RepeatUnit.HALF, 1, null, 31, LocalDateTime.of(now.getYear(), 1, 31, 0, 0), YNType.Y, null);
            saveVacationPolicy("반기별 특별 휴가", "매 반기 첫 부여일의 일자(15일)에 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), RepeatUnit.HALF, 1, null, null, LocalDateTime.of(now.getYear(), 1, 15, 0, 0), YNType.Y, null);

            // DAILY 예제
            saveVacationPolicy("매일 포인트 휴가", "매일 자동 부여되는 포인트 휴가입니다. 0.1일씩 적립.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.1000"), RepeatUnit.DAILY, 1, null, null, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null);

            // 구성원 신청용 휴가 정책 (ON_REQUEST - firstGrantDate, isRecurring, maxGrantCount 모두 null)
            saveVacationPolicy("동원훈련", "동원 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("3.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("동미참훈련", "동미참 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("1.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("예비군", "예비군 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("1.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("예비군(반차)", "예비군 훈련에 대한 반차 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("0.5000"), null, null, null, null, null, null, null);
            saveVacationPolicy("OT", "연장 근무에 대한 보상 휴가 정책입니다. 구성원이 직접 신청하는 휴가 정책입니다.", VacationType.OVERTIME, GrantMethod.ON_REQUEST, null, null, null, null, null, null, null, null);
            saveVacationPolicy("결혼", "결혼에 대한 휴가 정책입니다.", VacationType.WEDDING, GrantMethod.ON_REQUEST, new BigDecimal("5.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("출산", "출산에 대한 휴가 정책입니다.", VacationType.MATERNITY, GrantMethod.ON_REQUEST, new BigDecimal("10.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("조사", "부친상, 모친상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.ON_REQUEST, new BigDecimal("5.0000"), null, null, null, null, null, null, null);
            saveVacationPolicy("조사", "빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.ON_REQUEST, new BigDecimal("3.0000"), null, null, null, null, null, null, null);
        }

        public void saveMember(String id, String name, String email, LocalDate birth, OriginCompanyType company, String workTime, YNType lunar) {
            String encodedPassword = passwordEncoder.encode("1234");
            User user = User.createUser(id, encodedPassword, name, email, birth, company, workTime, lunar, null, null);
            em.persist(user);
        }

        public Department saveDepartment(String name, String nameKR, Department parent, Long level, String desc, String color, Company company) {
            Department department = Department.createDepartment(name, nameKR, parent, null, level, desc, color, company);
            em.persist(department);
            return department;
        }

        public void saveHoliday(String name, String date, HolidayType type, CountryCode countryCode, YNType lunarYN, String lunarDate, YNType isRecurring, String icon) {
            Holiday holiday = Holiday.createHoliday(name, date, type, countryCode, lunarYN, lunarDate, isRecurring, icon);
            em.persist(holiday);
        }

        public void saveSchedule(String userId, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate) {
            User user = em.find(User.class, userId);
            Schedule schedule = Schedule.createSchedule(user, desc, type, startDate, endDate);
            em.persist(schedule);
        }

        public void saveDues(String userName, Long amount, DuesType type, DuesCalcType calc, String date, String detail) {
            Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);
            em.persist(dues);
        }

        public void saveVacationPolicy(String name, String desc, VacationType vacationType, GrantMethod grantMethod, BigDecimal grantTime, RepeatUnit repeatUnit, Integer repeatInterval, Integer specificMonths, Integer specificDays, LocalDateTime firstGrantDate, YNType isRecurring, Integer maxGrantCount) {
            VacationPolicy vacationPolicy = VacationPolicy.createVacationPolicy(name, desc, vacationType, grantMethod, grantTime, repeatUnit, repeatInterval, specificMonths, specificDays, firstGrantDate, isRecurring, maxGrantCount);
            em.persist(vacationPolicy);
            if (vacationPolicy.getGrantMethod().equals(GrantMethod.MANUAL_GRANT)) {
                vacationPolicy.updateCantDeleted();
            } else {
                vacationPolicy.updateCanDeleted();
            }
        }
    }
}
