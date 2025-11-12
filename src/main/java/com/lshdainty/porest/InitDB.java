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
import com.lshdainty.porest.vacation.domain.*;
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
        initService.initSetSchedule();
        initService.initSetDues();
        initService.initSetVacationPolicy();
        initService.initSetUserVacationPolicy();
        initService.initSetVacationGrant();
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

        public void initSetSchedule() {
            LocalDateTime now = LocalDateTime.now();
            saveSchedule("user1", "교육", ScheduleType.EDUCATION,
                    LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 5, 3, 23, 59, 59));
//            saveSchedule("user1", "예비군", ScheduleType.DEFENSE,
//                    LocalDateTime.of(now.getYear(), 2, 23, 0, 0, 0),
//                    LocalDateTime.of(now.getYear(), 2, 28, 23, 59, 59));
            saveSchedule("user1", "출장", ScheduleType.BUSINESSTRIP,
                    LocalDateTime.of(now.getYear(), 3, 30, 0, 0, 0),
                    LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59));
//            saveSchedule("user1", "건강검진(반차)", ScheduleType.HEALTHCHECKHALF,
//                    LocalDateTime.of(now.getYear(), 5, 1, 9, 0, 0),
//                    LocalDateTime.of(now.getYear(), 5, 1, 14, 0, 0));
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
            // isFlexibleGrant = Y (관리자가 직접 시간을 지정하므로 가변 부여)
            saveVacationPolicy("연차(관리자용)", "연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 1분기 4일, 2분기 4일, 3분기 4일, 4분기 3일이 기본 값입니다.", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("OT(관리자용)", "연장 근무에 대한 보상 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 1시간 단위로 부여합니다. 예) 1시간 50분 근무 -> 1시간 부여, 2시간 10분 근무 -> 2시간 부여", VacationType.OVERTIME, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("건강검진", "건강검진 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 반차가 기본 값입니다.", VacationType.HEALTH, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("예비군(관리자용)", "예비군 훈련에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 동원(3일), 동미참(1일), 민방위(1일), 민방위(반차)가 있습니다.", VacationType.ARMY, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("결혼(관리자용)", "결혼에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 5일이 기본 값입니다.", VacationType.WEDDING, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT, null);
            saveVacationPolicy("출산(관리자용)", "출산에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 10일이 기본 값입니다.", VacationType.MATERNITY, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT, null);
            saveVacationPolicy("조사(관리자용)", "부친상, 모친상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 5일이 기본 값입니다.", VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.ONE_MONTHS_AFTER_GRANT, null);
            saveVacationPolicy("조사(관리자용)", "빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 3일이 기본 값입니다.", VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.ONE_MONTHS_AFTER_GRANT, null);

            // ===== 반복 부여 휴가 정책 (REPEAT_GRANT) =====

            // YEARLY 예제들
            saveVacationPolicy("연차", "연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("15.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("1분기 연차", "1분기 연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("2분기 연차", "2분기 연차 정책입니다. 매년 4월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 4, 1, LocalDateTime.of(now.getYear(), 4, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("3분기 연차", "3분기 연차 정책입니다. 매년 7월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 7, 1, LocalDateTime.of(now.getYear(), 7, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("4분기 연차", "4분기 연차 정책입니다. 매년 10월 1일 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 10, 1, LocalDateTime.of(now.getYear(), 10, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("생일 휴가", "매년 생일에 자동 부여되는 휴가입니다. 매년 3월 15일에 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 3, 15, LocalDateTime.of(now.getYear(), 3, 15, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("하계 휴가", "매년 6월에 자동 부여되는 하계 휴가입니다. 첫 부여일의 일자(15일) 사용.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N, RepeatUnit.YEARLY, 1, 6, null, LocalDateTime.of(now.getYear(), 6, 15, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("7년 근속 휴가", "7년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("5.0000"), null, YNType.N, RepeatUnit.YEARLY, 7, 1, 1, LocalDateTime.of(now.getYear() + 7, 1, 1, 0, 0), YNType.N, 1, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("3년 근속 휴가", "3년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N, RepeatUnit.YEARLY, 3, 1, 1, LocalDateTime.of(now.getYear() + 3, 1, 1, 0, 0), YNType.N, 1, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

            // MONTHLY 예제들
            saveVacationPolicy("매월 리프레시 휴가", "매월 1일 자동 부여되는 리프레시 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), null, YNType.N, RepeatUnit.MONTHLY, 1, null, 1, LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("매월 정기 휴가", "매월 15일 자동 부여되는 정기 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), null, YNType.N, RepeatUnit.MONTHLY, 1, null, 15, LocalDateTime.of(now.getYear(), now.getMonthValue(), 15, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("2개월마다 휴가", "2개월마다 첫 부여일의 일자에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N, RepeatUnit.MONTHLY, 2, null, null, LocalDateTime.of(now.getYear(), now.getMonthValue(), 10, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

            // QUARTERLY 예제들
            saveVacationPolicy("분기별 휴가", "매 분기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N, RepeatUnit.QUARTERLY, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("분기별 건강 휴가", "매 분기 15일에 자동 부여되는 건강 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.5000"), null, YNType.N, RepeatUnit.QUARTERLY, 1, null, 15, LocalDateTime.of(now.getYear(), 1, 15, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("분기별 특별 휴가", "매 분기 첫 부여일의 일자(20일)에 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N, RepeatUnit.QUARTERLY, 1, null, null, LocalDateTime.of(now.getYear(), 1, 20, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

            // HALF 예제들
            saveVacationPolicy("반기별 휴가", "매 반기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N, RepeatUnit.HALF, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("반기별 리프레시", "매 반기 31일에 자동 부여되는 휴가입니다. 월말이 31일 미만이면 해당 월 마지막 날 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.5000"), null, YNType.N, RepeatUnit.HALF, 1, null, 31, LocalDateTime.of(now.getYear(), 1, 31, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
            saveVacationPolicy("반기별 특별 휴가", "매 반기 첫 부여일의 일자(15일)에 자동 부여.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N, RepeatUnit.HALF, 1, null, null, LocalDateTime.of(now.getYear(), 1, 15, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

            // DAILY 예제
            saveVacationPolicy("매일 포인트 휴가", "매일 자동 부여되는 포인트 휴가입니다. 0.1일씩 적립.", VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("0.1000"), null, YNType.N, RepeatUnit.DAILY, 1, null, null, LocalDateTime.of(now.getYear(), 1, 1, 0, 0), YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

            // 구성원 신청용 휴가 정책 (ON_REQUEST - firstGrantDate, isRecurring, maxGrantCount 모두 null)
            // isFlexibleGrant = N (고정 시간 부여), isFlexibleGrant = Y (가변 부여, 예: OT는 시간 계산)
            saveVacationPolicy("동원훈련", "동원 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("3.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
            saveVacationPolicy("동미참훈련", "동미참 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("1.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
            saveVacationPolicy("예비군", "예비군 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("1.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
            saveVacationPolicy("예비군(반차)", "예비군 훈련에 대한 반차 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST, new BigDecimal("0.5000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
            saveVacationPolicy("OT", "연장 근무에 대한 보상 휴가 정책입니다. 구성원이 직접 신청하는 휴가 정책입니다.", VacationType.OVERTIME, GrantMethod.ON_REQUEST, null, YNType.Y, YNType.Y, null, null, null, null, null, null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
            saveVacationPolicy("결혼", "결혼에 대한 휴가 정책입니다.", VacationType.WEDDING, GrantMethod.ON_REQUEST, new BigDecimal("5.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT, 1);
            saveVacationPolicy("출산", "출산에 대한 휴가 정책입니다.", VacationType.MATERNITY, GrantMethod.ON_REQUEST, new BigDecimal("10.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT, 1);
            saveVacationPolicy("조사", "부친상, 모친상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.ON_REQUEST, new BigDecimal("5.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.ONE_MONTHS_AFTER_GRANT, 1);
            saveVacationPolicy("조사", "빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT, GrantMethod.ON_REQUEST, new BigDecimal("3.0000"), YNType.N, YNType.N, null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY, ExpirationType.ONE_MONTHS_AFTER_GRANT, 1);
        }

        public void initSetUserVacationPolicy() {
            // 유저 조회
            User user1 = em.find(User.class, "user1");
            User user2 = em.find(User.class, "user2");
            User user3 = em.find(User.class, "user3");
            User user4 = em.find(User.class, "user4");
            User user5 = em.find(User.class, "user5");
            User user6 = em.find(User.class, "user6");

            // 조사 정책 조회 (재사용)
            List<VacationPolicy> bereavementPolicies = findVacationPoliciesByNameAndType("조사", VacationType.BEREAVEMENT);

            // user1에게 휴가 정책 부여
            // 반복 부여 휴가 정책: 분기별 연차
            saveUserVacationPolicy(user1, findVacationPolicyByName("1분기 연차"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("2분기 연차"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("3분기 연차"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("4분기 연차"));
            // 구성원 신청용 휴가 정책
            saveUserVacationPolicy(user1, findVacationPolicyByName("동원훈련"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("동미참훈련"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("예비군"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("예비군(반차)"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("OT"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("결혼"));
            saveUserVacationPolicy(user1, findVacationPolicyByName("출산"));
            for (VacationPolicy policy : bereavementPolicies) {
                saveUserVacationPolicy(user1, policy);
            }

            // user2에게 휴가 정책 부여
            // 반복 부여 휴가 정책: 분기별 연차 + 7년 근속 휴가
            saveUserVacationPolicy(user2, findVacationPolicyByName("1분기 연차"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("2분기 연차"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("3분기 연차"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("4분기 연차"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("7년 근속 휴가"));
            // 구성원 신청용 휴가 정책
            saveUserVacationPolicy(user2, findVacationPolicyByName("동원훈련"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("동미참훈련"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("예비군"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("예비군(반차)"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("OT"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("결혼"));
            saveUserVacationPolicy(user2, findVacationPolicyByName("출산"));
            for (VacationPolicy policy : bereavementPolicies) {
                saveUserVacationPolicy(user2, policy);
            }

            // user3~6에게도 동일하게 분기별 연차 정책 부여
            for (User user : List.of(user3, user4, user5, user6)) {
                saveUserVacationPolicy(user, findVacationPolicyByName("1분기 연차"));
                saveUserVacationPolicy(user, findVacationPolicyByName("2분기 연차"));
                saveUserVacationPolicy(user, findVacationPolicyByName("3분기 연차"));
                saveUserVacationPolicy(user, findVacationPolicyByName("4분기 연차"));
                // 구성원 신청용 휴가 정책
                saveUserVacationPolicy(user, findVacationPolicyByName("동원훈련"));
                saveUserVacationPolicy(user, findVacationPolicyByName("동미참훈련"));
                saveUserVacationPolicy(user, findVacationPolicyByName("예비군"));
                saveUserVacationPolicy(user, findVacationPolicyByName("예비군(반차)"));
                saveUserVacationPolicy(user, findVacationPolicyByName("OT"));
                saveUserVacationPolicy(user, findVacationPolicyByName("결혼"));
                saveUserVacationPolicy(user, findVacationPolicyByName("출산"));
                for (VacationPolicy policy : bereavementPolicies) {
                    saveUserVacationPolicy(user, policy);
                }
            }
        }

        private VacationPolicy findVacationPolicyByName(String name) {
            return em.createQuery(
                            "SELECT vp FROM VacationPolicy vp WHERE vp.name = :name AND vp.isDeleted = :isDeleted AND vp.grantMethod != :manualGrant", VacationPolicy.class)
                    .setParameter("name", name)
                    .setParameter("isDeleted", YNType.N)
                    .setParameter("manualGrant", GrantMethod.MANUAL_GRANT)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("휴가 정책을 찾을 수 없습니다: " + name));
        }

        private List<VacationPolicy> findVacationPoliciesByNameAndType(String name, VacationType type) {
            return em.createQuery(
                            "SELECT vp FROM VacationPolicy vp WHERE vp.name = :name AND vp.vacationType = :type AND vp.isDeleted = :isDeleted AND vp.grantMethod = :onRequest", VacationPolicy.class)
                    .setParameter("name", name)
                    .setParameter("type", type)
                    .setParameter("isDeleted", YNType.N)
                    .setParameter("onRequest", GrantMethod.ON_REQUEST)
                    .getResultList();
        }

        private VacationPolicy findManualGrantPolicyByNameAndType(String name, VacationType type) {
            return em.createQuery(
                            "SELECT vp FROM VacationPolicy vp WHERE vp.name = :name AND vp.vacationType = :type AND vp.isDeleted = :isDeleted AND vp.grantMethod = :manualGrant", VacationPolicy.class)
                    .setParameter("name", name)
                    .setParameter("type", type)
                    .setParameter("isDeleted", YNType.N)
                    .setParameter("manualGrant", GrantMethod.MANUAL_GRANT)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("휴가 정책을 찾을 수 없습니다: " + name + ", " + type));
        }

        /**
         * 새로운 도메인 구조(VacationGrant, VacationUsage, VacationUsageDeduction)를 위한 예제 데이터 추가
         */
        public void initSetVacationGrant() {
            LocalDateTime now = LocalDateTime.now();

            // 유저 조회
            User user1 = em.find(User.class, "user1");
            User user2 = em.find(User.class, "user2");
            User user3 = em.find(User.class, "user3");
            User user4 = em.find(User.class, "user4");
            User user5 = em.find(User.class, "user5");
            User user6 = em.find(User.class, "user6");

            // 휴가 정책 조회
            VacationPolicy q1Policy = findVacationPolicyByName("1분기 연차");
            VacationPolicy q2Policy = findVacationPolicyByName("2분기 연차");
            VacationPolicy q3Policy = findVacationPolicyByName("3분기 연차");
            VacationPolicy q4Policy = findVacationPolicyByName("4분기 연차");
            VacationPolicy otPolicy = findManualGrantPolicyByNameAndType("OT(관리자용)", VacationType.OVERTIME);
            VacationPolicy maternityPolicy = findManualGrantPolicyByNameAndType("출산(관리자용)", VacationType.MATERNITY);

            // ===== user1 연차 부여 (현재 연도) =====
            saveVacationGrant(user1, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user1, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user1, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user1, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());
            // user1 OT 부여 (3건)
            saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());
            saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.2500"), now.getYear());
            saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());
            // user1 출산 휴가 부여 (사용 내역을 위해)
            saveVacationGrant(user1, maternityPolicy, VacationType.MATERNITY, "출산(관리자용)에 의한 휴가 부여", new BigDecimal("10.0000"), now.getYear());

            // ===== user2 연차 부여 (현재 연도) =====
            saveVacationGrant(user2, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user2, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user2, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user2, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());

            // ===== user3 연차 부여 (현재 연도) =====
            saveVacationGrant(user3, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user3, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user3, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user3, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());

            // ===== user4 연차 부여 (현재 연도) =====
            saveVacationGrant(user4, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user4, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user4, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user4, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());

            // ===== user5 연차 부여 (현재 연도) =====
            saveVacationGrant(user5, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user5, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user5, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user5, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());
            // user5 OT 부여 (3건)
            saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());
            saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());
            saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());

            // ===== user6 연차 부여 (현재 연도) =====
            saveVacationGrant(user6, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user6, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user6, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear());
            saveVacationGrant(user6, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear());
            // user6 OT 부여
            saveVacationGrant(user6, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear());

            // ===== user1 다음 연도 휴가 부여 =====
            saveVacationGrant(user1, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear()+1);
            saveVacationGrant(user1, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear()+1);
            saveVacationGrant(user1, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여", new BigDecimal("4.0000"), now.getYear()+1);
            saveVacationGrant(user1, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여", new BigDecimal("3.0000"), now.getYear()+1);
            // user1 다음 연도 OT 부여 (2건)
            saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.1250"), now.getYear()+1);
            saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여", new BigDecimal("0.3750"), now.getYear()+1);

            // ===== 휴가 사용 내역 마이그레이션 (VacationUsage + VacationUsageDeduction) =====
            // 모든 Grant를 다시 조회 (FIFO용)
            List<VacationGrant> user1Grants = findGrantsByUserAndType(user1, VacationType.ANNUAL);
            List<VacationGrant> user1MaternityGrants = findGrantsByUserAndType(user1, VacationType.MATERNITY);

            List<VacationGrant> user2Grants = findGrantsByUserAndType(user2, VacationType.ANNUAL);

            List<VacationGrant> user3Grants = findGrantsByUserAndType(user3, VacationType.ANNUAL);

            List<VacationGrant> user4Grants = findGrantsByUserAndType(user4, VacationType.ANNUAL);

            List<VacationGrant> user5Grants = findGrantsByUserAndType(user5, VacationType.ANNUAL);

            List<VacationGrant> user6Grants = findGrantsByUserAndType(user6, VacationType.ANNUAL);

            // user1 연차 사용 내역
            saveVacationUsageWithFIFO(user1, user1Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 1, 2, 0, 0),
                    LocalDateTime.of(2025, 1, 3, 23, 59, 59),
                    new BigDecimal("2.0000"));

            saveVacationUsageWithFIFO(user1, user1Grants, "1시간", VacationTimeType.ONETIMEOFF,
                    LocalDateTime.of(2025, 2, 3, 9, 0),
                    LocalDateTime.of(2025, 2, 3, 10, 0),
                    new BigDecimal("0.1250"));

            saveVacationUsageWithFIFO(user1, user1Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 3, 17, 0, 0),
                    LocalDateTime.of(2025, 3, 17, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user1, user1Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 10, 10, 0, 0),
                    LocalDateTime.of(2025, 10, 10, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user1, user1Grants, "오전반차", VacationTimeType.MORNINGOFF,
                    LocalDateTime.of(2025, 10, 15, 9, 0),
                    LocalDateTime.of(2025, 10, 15, 14, 0),
                    new BigDecimal("0.5000"));

            saveVacationUsageWithFIFO(user1, user1Grants, "오후반차", VacationTimeType.AFTERNOONOFF,
                    LocalDateTime.of(2025, 12, 19, 14, 0),
                    LocalDateTime.of(2025, 12, 19, 18, 0),
                    new BigDecimal("0.5000"));

            // user1 출산 휴가 사용 내역
            saveVacationUsageWithFIFO(user1, user1MaternityGrants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 11, 3, 0, 0),
                    LocalDateTime.of(2025, 11, 5, 23, 59, 59),
                    new BigDecimal("3.0000"));

            saveVacationUsageWithFIFO(user1, user1MaternityGrants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 8, 11, 0, 0),
                    LocalDateTime.of(2025, 8, 14, 23, 59, 59),
                    new BigDecimal("4.0000"));

            // user2 연차 사용 내역
            saveVacationUsageWithFIFO(user2, user2Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 4, 8, 0, 0),
                    LocalDateTime.of(2025, 4, 8, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user2, user2Grants, "1시간", VacationTimeType.ONETIMEOFF,
                    LocalDateTime.of(2025, 4, 9, 9, 0),
                    LocalDateTime.of(2025, 4, 9, 10, 0),
                    new BigDecimal("0.1250"));

            saveVacationUsageWithFIFO(user2, user2Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 7, 25, 0, 0),
                    LocalDateTime.of(2025, 7, 25, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user2, user2Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 8, 14, 0, 0),
                    LocalDateTime.of(2025, 8, 14, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user2, user2Grants, "3시간", VacationTimeType.THREETIMEOFF,
                    LocalDateTime.of(2025, 9, 8, 9, 0),
                    LocalDateTime.of(2025, 9, 8, 12, 0),
                    new BigDecimal("0.3750"));

            saveVacationUsageWithFIFO(user2, user2Grants, "오후반차", VacationTimeType.AFTERNOONOFF,
                    LocalDateTime.of(2025, 10, 10, 14, 0),
                    LocalDateTime.of(2025, 10, 10, 18, 0),
                    new BigDecimal("0.5000"));

            // user3 연차 사용 내역
            saveVacationUsageWithFIFO(user3, user3Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 1, 2, 0, 0),
                    LocalDateTime.of(2025, 1, 3, 23, 59, 59),
                    new BigDecimal("2.0000"));

            saveVacationUsageWithFIFO(user3, user3Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 3, 17, 0, 0),
                    LocalDateTime.of(2025, 3, 17, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user3, user3Grants, "2시간", VacationTimeType.TWOTIMEOFF,
                    LocalDateTime.of(2025, 4, 9, 9, 0),
                    LocalDateTime.of(2025, 4, 9, 11, 0),
                    new BigDecimal("0.2500"));

            saveVacationUsageWithFIFO(user3, user3Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 6, 2, 0, 0),
                    LocalDateTime.of(2025, 6, 2, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user3, user3Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 8, 14, 0, 0),
                    LocalDateTime.of(2025, 8, 14, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user3, user3Grants, "오후반차", VacationTimeType.AFTERNOONOFF,
                    LocalDateTime.of(2025, 10, 10, 14, 0),
                    LocalDateTime.of(2025, 10, 10, 18, 0),
                    new BigDecimal("0.5000"));

            // user4 연차 사용 내역
            saveVacationUsageWithFIFO(user4, user4Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 1, 2, 0, 0),
                    LocalDateTime.of(2025, 1, 3, 23, 59, 59),
                    new BigDecimal("2.0000"));

            saveVacationUsageWithFIFO(user4, user4Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 1, 31, 0, 0),
                    LocalDateTime.of(2025, 1, 31, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user4, user4Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 5, 7, 0, 0),
                    LocalDateTime.of(2025, 5, 9, 23, 59, 59),
                    new BigDecimal("3.0000"));

            // user5 연차 사용 내역
            saveVacationUsageWithFIFO(user5, user5Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 1, 2, 0, 0),
                    LocalDateTime.of(2025, 1, 3, 23, 59, 59),
                    new BigDecimal("2.0000"));

            saveVacationUsageWithFIFO(user5, user5Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 5, 7, 0, 0),
                    LocalDateTime.of(2025, 5, 9, 23, 59, 59),
                    new BigDecimal("3.0000"));

            saveVacationUsageWithFIFO(user5, user5Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 6, 2, 0, 0),
                    LocalDateTime.of(2025, 6, 5, 23, 59, 59),
                    new BigDecimal("3.0000"));

            saveVacationUsageWithFIFO(user5, user5Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 10, 10, 0, 0),
                    LocalDateTime.of(2025, 10, 10, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user5, user5Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 12, 26, 0, 0),
                    LocalDateTime.of(2025, 12, 26, 23, 59, 59),
                    new BigDecimal("1.0000"));

            // user6 연차 사용 내역
            saveVacationUsageWithFIFO(user6, user6Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 4, 8, 0, 0),
                    LocalDateTime.of(2025, 4, 8, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user6, user6Grants, "1시간", VacationTimeType.ONETIMEOFF,
                    LocalDateTime.of(2025, 4, 9, 9, 0),
                    LocalDateTime.of(2025, 4, 9, 10, 0),
                    new BigDecimal("0.1250"));

            saveVacationUsageWithFIFO(user6, user6Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 7, 25, 0, 0),
                    LocalDateTime.of(2025, 7, 25, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user6, user6Grants, "연차", VacationTimeType.DAYOFF,
                    LocalDateTime.of(2025, 8, 14, 0, 0),
                    LocalDateTime.of(2025, 8, 14, 23, 59, 59),
                    new BigDecimal("1.0000"));

            saveVacationUsageWithFIFO(user6, user6Grants, "3시간", VacationTimeType.THREETIMEOFF,
                    LocalDateTime.of(2025, 9, 8, 9, 0),
                    LocalDateTime.of(2025, 9, 8, 12, 0),
                    new BigDecimal("0.3750"));

            saveVacationUsageWithFIFO(user6, user6Grants, "오후반차", VacationTimeType.AFTERNOONOFF,
                    LocalDateTime.of(2025, 10, 10, 14, 0),
                    LocalDateTime.of(2025, 10, 10, 18, 0),
                    new BigDecimal("0.5000"));

            em.flush();
        }

        private List<VacationGrant> findGrantsByUserAndType(User user, VacationType type) {
            return em.createQuery(
                            "SELECT vg FROM VacationGrant vg " +
                                    "WHERE vg.user = :user AND vg.type = :type AND vg.isDeleted = :isDeleted " +
                                    "ORDER BY vg.expiryDate ASC, vg.grantDate ASC",
                            VacationGrant.class)
                    .setParameter("user", user)
                    .setParameter("type", type)
                    .setParameter("isDeleted", YNType.N)
                    .getResultList();
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

        public void saveVacationPolicy(String name, String desc, VacationType vacationType, GrantMethod grantMethod, BigDecimal grantTime, YNType isFlexibleGrant, YNType minuteGrantYn, RepeatUnit repeatUnit, Integer repeatInterval, Integer specificMonths, Integer specificDays, LocalDateTime firstGrantDate, YNType isRecurring, Integer maxGrantCount, EffectiveType effectiveType, ExpirationType expirationType, Integer approvalRequiredCount) {
            VacationPolicy policy;
            switch (grantMethod) {
                case MANUAL_GRANT -> policy = VacationPolicy.createManualGrantPolicy(name, desc, vacationType, grantTime, isFlexibleGrant, minuteGrantYn, effectiveType, expirationType);
                case REPEAT_GRANT -> policy = VacationPolicy.createRepeatGrantPolicy(name, desc, vacationType, grantTime, minuteGrantYn, repeatUnit, repeatInterval, specificMonths, specificDays, firstGrantDate, isRecurring, maxGrantCount, effectiveType, expirationType);
                case ON_REQUEST -> policy = VacationPolicy.createOnRequestPolicy(name, desc, vacationType, grantTime, isFlexibleGrant, minuteGrantYn, approvalRequiredCount, effectiveType, expirationType);
                default -> {
                    return;
                }
            }

            em.persist(policy);
            if (policy.getGrantMethod().equals(GrantMethod.MANUAL_GRANT)) {
                policy.updateCantDeleted();
            } else {
                policy.updateCanDeleted();
            }
        }

        private void saveUserVacationPolicy(User user, VacationPolicy vacationPolicy) {
            UserVacationPolicy userVacationPolicy = UserVacationPolicy.createUserVacationPolicy(user, vacationPolicy);
            em.persist(userVacationPolicy);
        }

        private void saveVacationGrant(User user, VacationPolicy policy, VacationType type, String desc, BigDecimal grantTime, int year) {
            LocalDateTime startDate;
            LocalDateTime expiryDate;

            // 휴가 유형에 따라 시작일과 만료일 설정
            if (type == VacationType.ANNUAL ||
                type == VacationType.OVERTIME ||
                type == VacationType.HEALTH ||
                type == VacationType.ARMY) {
                // 연차, 연장, 건강, 군: 해당 년도 1월 1일 ~ 12월 31일
                startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
                expiryDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            } else if (type == VacationType.MATERNITY ||
                       type == VacationType.WEDDING ||
                       type == VacationType.BEREAVEMENT) {
                // 출산, 결혼, 상조: 현재 -3부터 +6개월
                LocalDateTime now = LocalDateTime.now();
                startDate = LocalDateTime.of(now.getYear(), now.getMonthValue() - 3, now.getDayOfMonth(), 0, 0, 0);
                expiryDate = startDate.plusMonths(6).minusSeconds(1);
            } else {
                // 기타
                startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
                expiryDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            }

            VacationGrant grant = VacationGrant.createVacationGrant(
                    user, policy, desc, type, grantTime,
                    startDate, expiryDate
            );
            em.persist(grant);
        }

        private void saveVacationUsageWithFIFO(User user, List<VacationGrant> grants, String desc,
                                               VacationTimeType timeType, LocalDateTime startDate,
                                               LocalDateTime endDate, BigDecimal usedTime) {
            // VacationUsage 생성
            VacationUsage usage = VacationUsage.createVacationUsage(
                    user, desc, timeType, startDate, endDate, usedTime
            );
            em.persist(usage);

            // FIFO 차감
            BigDecimal remainingNeedTime = usedTime;
            for (VacationGrant grant : grants) {
                if (remainingNeedTime.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                if (grant.getRemainTime().compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // 이미 다 사용한 Grant는 스킵
                }

                BigDecimal deductibleTime = grant.getRemainTime().min(remainingNeedTime);

                if (deductibleTime.compareTo(BigDecimal.ZERO) > 0) {
                    // VacationUsageDeduction 생성
                    VacationUsageDeduction deduction = VacationUsageDeduction.createVacationUsageDeduction(
                            usage, grant, deductibleTime
                    );
                    em.persist(deduction);

                    // VacationGrant의 remainTime 차감
                    grant.deduct(deductibleTime);
                    remainingNeedTime = remainingNeedTime.subtract(deductibleTime);
                }
            }

            if (remainingNeedTime.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException("휴가 사용 시간이 부족합니다. User: " + user.getId() +
                        ", 필요: " + usedTime + ", 부족: " + remainingNeedTime);
            }
        }
    }
}
