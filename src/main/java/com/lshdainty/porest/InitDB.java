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
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.PermissionRepository;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;

import com.lshdainty.porest.vacation.domain.*;
import com.lshdainty.porest.vacation.type.*;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.type.CodeType;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class InitDB {

	private final InitService initService;

	@PostConstruct
	public void init() {
		initService.initSetRole();
		initService.initSetDepartment();
		initService.initSetMember();
		initService.initSetUserDepartment();
		initService.initSetSchedule();
		initService.initSetDues();
		initService.initSetVacationPolicy();
		initService.initSetUserVacationPlan();
		initService.initSetVacationGrant();
		initService.initSetHoliday();
		initService.initSetWorkCode();
		initService.initSetWorkHistory();
	}

	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {
		private final EntityManager em;
		private final RoleRepository roleRepository;
		private final PermissionRepository permissionRepository;
		private final BCryptPasswordEncoder passwordEncoder;

		// 멤버 변수로 저장하여 다른 init 메서드에서 재사용
		private User user1, user2, user3, user4, user5, user6;
		private Department dept, GMESJ, GMESM, DT, myDATA, tableau;
		private final Map<String, List<VacationPolicy>> policyMap = new HashMap<>();
		private final Map<String, WorkCode> workCodeMap = new HashMap<>();

		public void initSetMember() {
			// ==========================================
			// 1. 사용자 생성
			// ==========================================
			user1 = saveMember("user1", "이서준", "aaa@naver.com", LocalDate.of(1970, 7, 23),
					OriginCompanyType.SKAX, "9 ~ 18", YNType.N);
			user2 = saveMember("user2", "김서연", "bbb@naver.com", LocalDate.of(1970, 10, 26),
					OriginCompanyType.DTOL, "8 ~ 17", YNType.N);
			user3 = saveMember("user3", "김지후", "ccc@naver.com", LocalDate.of(1974, 1, 15),
					OriginCompanyType.INSIGHTON, "10 ~ 19", YNType.Y);
			user4 = saveMember("user4", "이준우", "ddd@naver.com", LocalDate.of(1980, 4, 30),
					OriginCompanyType.BIGXDATA, "9 ~ 18", YNType.N);
			user5 = saveMember("user5", "조민서", "eee@naver.com", LocalDate.of(1992, 12, 20),
					OriginCompanyType.CNTHOTH, "10 ~ 19", YNType.N);
			user6 = saveMember("user6", "이하은", "fff@naver.com", LocalDate.of(1885, 9, 2),
					OriginCompanyType.SKAX, "8 ~ 17", YNType.N);

			// ==========================================
			// 2. 역할 조회
			// ==========================================
			Role adminRole = roleRepository.findByCode("ADMIN")
					.orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
			Role managerRole = roleRepository.findByCode("MANAGER")
					.orElseThrow(() -> new IllegalStateException("MANAGER role not found"));
			Role userRole = roleRepository.findByCode("USER")
					.orElseThrow(() -> new IllegalStateException("USER role not found"));

			// ==========================================
			// 3. 사용자에게 역할 부여
			// ==========================================
			// user1: 관리자 (이서준) - ADMIN 역할
			user1.updateUser(user1.getName(), user1.getEmail(), List.of(adminRole), user1.getBirth(),
					user1.getCompany(), user1.getWorkTime(), user1.getLunarYN(), null, null, null, null);

			// user2: 일반 사용자 (김서연) - USER 역할
			user2.updateUser(user2.getName(), user2.getEmail(), List.of(userRole), user2.getBirth(),
					user2.getCompany(), user2.getWorkTime(), user2.getLunarYN(), null, null, null, null);

			// user3: 관리자 (김지후) - ADMIN 역할
			user3.updateUser(user3.getName(), user3.getEmail(), List.of(adminRole), user3.getBirth(),
					user3.getCompany(), user3.getWorkTime(), user3.getLunarYN(), null, null, null, null);

			// user4: 매니저 (이준우) - MANAGER 역할
			user4.updateUser(user4.getName(), user4.getEmail(), List.of(managerRole), user4.getBirth(),
					user4.getCompany(), user4.getWorkTime(), user4.getLunarYN(), null, null, null, null);

			// user5: 일반 사용자 (조민서) - USER 역할
			user5.updateUser(user5.getName(), user5.getEmail(), List.of(userRole), user5.getBirth(),
					user5.getCompany(), user5.getWorkTime(), user5.getLunarYN(), null, null, null, null);

			// user6: 매니저 (이하은) - MANAGER 역할
			user6.updateUser(user6.getName(), user6.getEmail(), List.of(managerRole), user6.getBirth(),
					user6.getCompany(), user6.getWorkTime(), user6.getLunarYN(), null, null, null, null);

			// ==========================================
			// 4. 회원가입 완료 처리
			// ==========================================
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

			dept = saveDepartment("dept", "생산운영", null, user6, 0L, "mes 생산운영 파트입니다.", null, company);
			saveDepartment("Olive", "Olive", dept, null, 1L, "울산 운영 부서입니다.", null, company);
			Department mes = saveDepartment("G-MES", "G-MES", dept, null, 1L, "G-MES 부서입니다.", null,
					company);
			GMESJ = saveDepartment("G-MESJ", "G-MESJ", mes, null, 2L, "정읍 G-MES 파트입니다.", null, company);
			GMESM = saveDepartment("G-MESM", "G-MESM", mes, null, 2L, "말련 G-MES 파트입니다.", null, company);
			saveDepartment("G-SCM", "G-SCM", dept, null, 1L, "G-SCM 부서입니다.", null, company);
			DT = saveDepartment("DT", "DT", dept, user3, 1L, "SKC DT 부서입니다.", null, company);
			myDATA = saveDepartment("myDATA", "myDATA", DT, null, 2L, "myDATA 파트입니다.", null, company);
			tableau = saveDepartment("Tableau", "Tableau", DT, null, 2L, "Tableau 파트입니다.", null, company);
			saveDepartment("AOI", "AOI", DT, null, 2L, "AOI 파트입니다.", null, company);
			saveDepartment("CMP", "CMP", dept, null, 1L, "CMP 부서입니다.", null, company);
		}

		public void initSetUserDepartment() {
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

		public void initSetHoliday() {
			saveHoliday("신정", LocalDate.of(2025, 1, 1), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🌅");
			saveHoliday("임시공휴일(설날)", LocalDate.of(2025, 1, 27), HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null,
					YNType.N, null);
			saveHoliday("설날연휴", LocalDate.of(2025, 1, 28), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2024, 12, 29),
					YNType.Y, "🧧");
			saveHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1),
					YNType.Y, "🧧");
			saveHoliday("설날연휴", LocalDate.of(2025, 1, 30), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 2),
					YNType.Y, "🧧");
			saveHoliday("삼일절", LocalDate.of(2025, 3, 1), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🇰🇷");
			saveHoliday("대체공휴일(삼일절)", LocalDate.of(2025, 3, 3), HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null,
					YNType.N, null);
			saveHoliday("근로자의 날", LocalDate.of(2025, 5, 1), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🧑‍💻");
			saveHoliday("어린이날", LocalDate.of(2025, 5, 5), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"👶");
			saveHoliday("석가탄신일", LocalDate.of(2025, 5, 5), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 4, 8),
					YNType.Y, "🪷");
			saveHoliday("대체공휴일(석가탄신일)", LocalDate.of(2025, 5, 6), HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null,
					YNType.N, null);
			saveHoliday("임시공휴일(제 21대 대선)", LocalDate.of(2025, 6, 3), HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N,
					null, YNType.N, "🗳");
			saveHoliday("현충일", LocalDate.of(2025, 6, 6), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🪖");
			saveHoliday("광복절", LocalDate.of(2025, 8, 15), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🇰🇷");
			saveHoliday("개천절", LocalDate.of(2025, 10, 3), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🫅");
			saveHoliday("추석연휴", LocalDate.of(2025, 10, 5), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 8, 14),
					YNType.Y, "🎑");
			saveHoliday("추석", LocalDate.of(2025, 10, 6), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 8, 15),
					YNType.Y, "🎑");
			saveHoliday("추석연휴", LocalDate.of(2025, 10, 7), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 8, 16),
					YNType.Y, "🎑");
			saveHoliday("대체공휴일(추석)", LocalDate.of(2025, 10, 8), HolidayType.SUBSTITUTE, CountryCode.KR, YNType.N, null,
					YNType.N, null);
			saveHoliday("한글날", LocalDate.of(2025, 10, 9), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"📚");
			saveHoliday("크리스마스", LocalDate.of(2025, 12, 25), HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y,
					"🎄");
		}

		public void initSetRole() {
			if (roleRepository.findAllRoles().isEmpty()) {
				// ==========================================
				// 1. Create Permissions
				// ==========================================

				// 1. 사용자 관리
				Permission userRead = Permission.createPermission("USER:READ", "사용자 조회", "본인 정보 조회", ResourceType.USER,
						ActionType.READ);
				permissionRepository.save(userRead);
				Permission userEdit = Permission.createPermission("USER:EDIT", "사용자 수정", "본인 정보 수정", ResourceType.USER,
						ActionType.EDIT);
				permissionRepository.save(userEdit);
				Permission userManage = Permission.createPermission("USER:MANAGE", "사용자 관리", "유저 생성/초대/삭제 및 전체 수정",
						ResourceType.USER, ActionType.MANAGE);
				permissionRepository.save(userManage);

				// 2. 휴가 관리
				Permission vacationRead = Permission.createPermission("VACATION:READ", "휴가 조회", "본인 휴가 조회",
						ResourceType.VACATION, ActionType.READ);
				permissionRepository.save(vacationRead);
				Permission vacationUse = Permission.createPermission("VACATION:USE", "휴가 사용", "휴가 사용/수정/취소",
						ResourceType.VACATION, ActionType.WRITE);
				permissionRepository.save(vacationUse);
				Permission vacationRequest = Permission.createPermission("VACATION:REQUEST", "휴가 신청", "휴가 신청 및 취소",
						ResourceType.VACATION, ActionType.REQUEST);
				permissionRepository.save(vacationRequest);
				Permission vacationApprove = Permission.createPermission("VACATION:APPROVE", "휴가 승인", "타인 휴가 승인/반려",
						ResourceType.VACATION, ActionType.APPROVE);
				permissionRepository.save(vacationApprove);
				Permission vacationGrant = Permission.createPermission("VACATION:GRANT", "휴가 부여", "휴가 강제 부여 및 회수",
						ResourceType.VACATION, ActionType.GRANT);
				permissionRepository.save(vacationGrant);
				Permission vacationManage = Permission.createPermission("VACATION:MANAGE", "휴가 전체 관리", "휴가 정책 관리 및 전체 사용자 휴가 요약 조회",
						ResourceType.VACATION, ActionType.MANAGE);
				permissionRepository.save(vacationManage);

				// 3. 업무 관리
				Permission workRead = Permission.createPermission("WORK:READ", "업무 조회", "본인 업무 조회", ResourceType.WORK,
						ActionType.READ);
				permissionRepository.save(workRead);
				Permission workWrite = Permission.createPermission("WORK:WRITE", "업무 작성", "본인 업무 작성/수정",
						ResourceType.WORK, ActionType.WRITE);
				permissionRepository.save(workWrite);
				Permission workManage = Permission.createPermission("WORK:MANAGE", "업무 관리", "전체 업무 수정/삭제 및 코드 관리",
						ResourceType.WORK, ActionType.MANAGE);
				permissionRepository.save(workManage);

				// 4. 일정 관리
				Permission scheduleRead = Permission.createPermission("SCHEDULE:READ", "일정 조회", "일정 조회",
						ResourceType.SCHEDULE, ActionType.READ);
				permissionRepository.save(scheduleRead);
				Permission scheduleWrite = Permission.createPermission("SCHEDULE:WRITE", "일정 작성", "일정 등록/수정",
						ResourceType.SCHEDULE, ActionType.WRITE);
				permissionRepository.save(scheduleWrite);
				Permission scheduleManage = Permission.createPermission("SCHEDULE:MANAGE", "일정 관리", "전체 일정 관리",
						ResourceType.SCHEDULE, ActionType.MANAGE);
				permissionRepository.save(scheduleManage);

				// 5. 회사/부서 관리
				Permission companyRead = Permission.createPermission("COMPANY:READ", "회사/부서 조회", "회사 및 부서 정보 조회",
						ResourceType.COMPANY, ActionType.READ);
				permissionRepository.save(companyRead);
				Permission companyManage = Permission.createPermission("COMPANY:MANAGE", "회사/부서 관리", "회사 및 부서 정보 관리",
						ResourceType.COMPANY, ActionType.MANAGE);
				permissionRepository.save(companyManage);

				// 6. 공휴일 관리
				Permission holidayRead = Permission.createPermission("HOLIDAY:READ", "공휴일 조회", "공휴일 조회",
						ResourceType.HOLIDAY, ActionType.READ);
				permissionRepository.save(holidayRead);
				Permission holidayManage = Permission.createPermission("HOLIDAY:MANAGE", "공휴일 관리", "공휴일 생성/수정/삭제",
						ResourceType.HOLIDAY, ActionType.MANAGE);
				permissionRepository.save(holidayManage);

				// 7. 회비 관리
				Permission duesRead = Permission.createPermission("DUES:READ", "회비 조회", "회비 내역 조회", ResourceType.DUES,
						ActionType.READ);
				permissionRepository.save(duesRead);
				Permission duesManage = Permission.createPermission("DUES:MANAGE", "회비 관리", "회비 관리 및 페이지 접근",
						ResourceType.DUES, ActionType.MANAGE);
				permissionRepository.save(duesManage);

                // 8. 사규 관리
                Permission regulationRead = Permission.createPermission("REGULATION:READ", "사규 조회", "사규 조회", ResourceType.REGULATION,
                        ActionType.READ);
                permissionRepository.save(regulationRead);

				// 9. 공지사항 관리
				Permission noticeRead = Permission.createPermission("NOTICE:READ", "공지사항 조회", "공지사항 조회",
						ResourceType.NOTICE, ActionType.READ);
				permissionRepository.save(noticeRead);
				Permission noticeManage = Permission.createPermission("NOTICE:MANAGE", "공지사항 관리", "공지사항 등록/수정/삭제",
						ResourceType.NOTICE, ActionType.MANAGE);
				permissionRepository.save(noticeManage);

				// 10. 권한 관리
				Permission roleManage = Permission.createPermission("ROLE:MANAGE", "권한 관리", "역할 및 권한 설정",
						ResourceType.ROLE, ActionType.MANAGE);
				permissionRepository.save(roleManage);

				// ==========================================
				// 2. Create Roles
				// ==========================================

				// ADMIN Role (모든 권한)
				Role adminRole = Role.createRole("ADMIN", "관리자", "전체 권한");
				adminRole.addPermission(userRead);
				adminRole.addPermission(userEdit);
				adminRole.addPermission(userManage);
				adminRole.addPermission(vacationRead);
				adminRole.addPermission(vacationUse);
				adminRole.addPermission(vacationRequest);
				adminRole.addPermission(vacationApprove);
				adminRole.addPermission(vacationGrant);
				adminRole.addPermission(vacationManage);
				adminRole.addPermission(workRead);
				adminRole.addPermission(workWrite);
				adminRole.addPermission(workManage);
				adminRole.addPermission(scheduleRead);
				adminRole.addPermission(scheduleWrite);
				adminRole.addPermission(scheduleManage);
				adminRole.addPermission(companyRead);
				adminRole.addPermission(companyManage);
				adminRole.addPermission(holidayRead);
				adminRole.addPermission(holidayManage);
				adminRole.addPermission(duesRead);
				adminRole.addPermission(duesManage);
				adminRole.addPermission(regulationRead);
				adminRole.addPermission(noticeRead);
				adminRole.addPermission(noticeManage);
				adminRole.addPermission(roleManage);
				roleRepository.save(adminRole);

				// MANAGER Role (팀장/관리자 권한)
				Role managerRole = Role.createRole("MANAGER", "매니저", "승인 및 조회 권한");
				managerRole.addPermission(userRead);
                managerRole.addPermission(userEdit);
				managerRole.addPermission(vacationRead);
				managerRole.addPermission(vacationUse);
				managerRole.addPermission(vacationRequest);
				managerRole.addPermission(vacationApprove);
				managerRole.addPermission(workRead);
				managerRole.addPermission(workWrite);
				managerRole.addPermission(workManage);
				managerRole.addPermission(scheduleRead);
				managerRole.addPermission(scheduleWrite);
				managerRole.addPermission(scheduleManage);
				managerRole.addPermission(companyRead);
				managerRole.addPermission(holidayRead);
				managerRole.addPermission(duesRead);
				managerRole.addPermission(regulationRead);
				managerRole.addPermission(noticeRead);
				roleRepository.save(managerRole);

				// USER Role (일반 사용자 권한)
				Role userRole = Role.createRole("USER", "일반 사용자", "기본 권한");
				userRole.addPermission(userRead);
				userRole.addPermission(userEdit);
				userRole.addPermission(vacationRead);
				userRole.addPermission(vacationUse);
				userRole.addPermission(vacationRequest);
				userRole.addPermission(workRead);
				userRole.addPermission(workWrite);
				userRole.addPermission(scheduleRead);
				userRole.addPermission(scheduleWrite);
				userRole.addPermission(companyRead);
				userRole.addPermission(holidayRead);
				userRole.addPermission(duesRead);
				userRole.addPermission(regulationRead);
				userRole.addPermission(noticeRead);
				roleRepository.save(userRole);
			}
		}

		public void initSetSchedule() {
			LocalDateTime now = LocalDateTime.now();
			saveSchedule(user1, "교육", ScheduleType.EDUCATION,
					LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
					LocalDateTime.of(now.getYear(), 5, 3, 23, 59, 59));
			saveSchedule(user1, "출장", ScheduleType.BUSINESSTRIP,
					LocalDateTime.of(now.getYear(), 3, 30, 0, 0, 0),
					LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59));
			saveSchedule(user1, "생일", ScheduleType.BIRTHDAY,
					LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
					LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
			saveSchedule(user1, "출장", ScheduleType.BUSINESSTRIP,
					LocalDateTime.of(now.getYear(), 5, 1, 0, 0, 0),
					LocalDateTime.of(now.getYear(), 5, 1, 23, 59, 59));
		}

		public void initSetDues() {
			saveDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 4), "생일비");
			saveDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 4), "생일비");
			saveDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 4), "생일비");
			saveDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 4), "생일비");
			saveDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, LocalDate.of(2025, 1, 31), "생일비 출금");
			saveDues("이하은", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 2, 4), "생일비");
			saveDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 2, 4), "생일비");
			saveDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 2, 4), "생일비");
			saveDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 2, 4), "생일비");
			saveDues("조민서", 30000L, DuesType.BIRTH, DuesCalcType.MINUS, LocalDate.of(2025, 2, 28), "생일비 출금");
			saveDues("이서준", 30000L, DuesType.OPERATION, DuesCalcType.PLUS, LocalDate.of(2025, 1, 28), "운영비 입금");
			saveDues("김서연", 30000L, DuesType.OPERATION, DuesCalcType.PLUS, LocalDate.of(2025, 4, 28), "운영비 입금");
			saveDues("김지후", 10000L, DuesType.FINE, DuesCalcType.PLUS, LocalDate.of(2025, 7, 28), "운영비 입금");
			saveDues("조민서", 20000L, DuesType.FINE, DuesCalcType.PLUS, LocalDate.of(2025, 7, 28), "운영비 출금");
			saveDues("이준우", 10000L, DuesType.FINE, DuesCalcType.PLUS, LocalDate.of(2025, 7, 28), "운영비 출금");
			saveDues("이하은", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, LocalDate.of(2025, 7, 28), "운영비 출금");
			saveDues("조민서", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, LocalDate.of(2025, 7, 28), "운영비 출금");
			saveDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 7, 4), "생일비");
		}

		public void initSetVacationPolicy() {
			LocalDateTime now = LocalDateTime.now();

			// 관리자 부여용 휴가정책 (MANUAL_GRANT - firstGrantDate, isRecurring, maxGrantCount 모두
			// null)
			// isFlexibleGrant = Y (관리자가 직접 시간을 지정하므로 가변 부여)
			saveVacationPolicy("연차(관리자용)",
					"연차 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 1분기 4일, 2분기 4일, 3분기 4일, 4분기 3일이 기본 값입니다.",
					VacationType.ANNUAL, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.START_OF_YEAR,
					ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("OT(관리자용)",
					"연장 근무에 대한 보상 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 1시간 단위로 부여합니다. 예) 1시간 50분 근무 -> 1시간 부여, 2시간 10분 근무 -> 2시간 부여",
					VacationType.OVERTIME, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.START_OF_YEAR,
					ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("건강검진", "건강검진 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 반차가 기본 값입니다.",
					VacationType.HEALTH, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.START_OF_YEAR,
					ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("예비군(관리자용)",
					"예비군 훈련에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 동원(3일), 동미참(1일), 민방위(1일), 민방위(반차)가 있습니다.",
					VacationType.ARMY, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.START_OF_YEAR,
					ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("결혼(관리자용)", "결혼에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 5일이 기본 값입니다.",
					VacationType.WEDDING, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.SIX_MONTHS_AFTER_GRANT, null);
			saveVacationPolicy("출산(관리자용)", "출산에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 10일이 기본 값입니다.",
					VacationType.MATERNITY, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N,
					null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.SIX_MONTHS_AFTER_GRANT, null);
			saveVacationPolicy("조사(관리자용)", "부친상, 모친상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 5일이 기본 값입니다.",
					VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N,
					null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.ONE_MONTHS_AFTER_GRANT, null);
			saveVacationPolicy("조사(관리자용)",
					"빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다. 관리자가 직접 휴가를 부여하는 정책입니다. 3일이 기본 값입니다.",
					VacationType.BEREAVEMENT, GrantMethod.MANUAL_GRANT, null, YNType.Y, YNType.N,
					null, null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.ONE_MONTHS_AFTER_GRANT, null);

			// ===== 반복 부여 휴가 정책 (REPEAT_GRANT) =====

			// YEARLY 예제들
			saveVacationPolicy("연차", "연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("15.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("1분기 연차", "1분기 연차 정책입니다. 매년 1월 1일 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 1, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("2분기 연차", "2분기 연차 정책입니다. 매년 4월 1일 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 4, 1, LocalDateTime.of(now.getYear(), 4, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("3분기 연차", "3분기 연차 정책입니다. 매년 7월 1일 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("4.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 7, 1, LocalDateTime.of(now.getYear(), 7, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("4분기 연차", "4분기 연차 정책입니다. 매년 10월 1일 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 10, 1, LocalDateTime.of(now.getYear(), 10, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("생일 휴가", "매년 생일에 자동 부여되는 휴가입니다. 매년 3월 15일에 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 3, 15, LocalDateTime.of(now.getYear(), 3, 15, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("하계 휴가", "매년 6월에 자동 부여되는 하계 휴가입니다. 첫 부여일의 일자(15일) 사용.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 1, 6, null, LocalDateTime.of(now.getYear(), 6, 15, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("7년 근속 휴가", "7년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("5.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 7, 1, 1, LocalDateTime.of(now.getYear() + 7, 1, 1, 0, 0),
					YNType.N, 1, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("3년 근속 휴가", "3년 근속 시 1회 부여되는 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N,
					RepeatUnit.YEARLY, 3, 1, 1, LocalDateTime.of(now.getYear() + 3, 1, 1, 0, 0),
					YNType.N, 1, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

			// MONTHLY 예제들
			saveVacationPolicy("매월 리프레시 휴가", "매월 1일 자동 부여되는 리프레시 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), null, YNType.N,
					RepeatUnit.MONTHLY, 1, null, 1,
					LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0), YNType.Y, null,
					EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("매월 정기 휴가", "매월 15일 자동 부여되는 정기 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("0.5000"), null, YNType.N,
					RepeatUnit.MONTHLY, 1, null, 15,
					LocalDateTime.of(now.getYear(), now.getMonthValue(), 15, 0, 0), YNType.Y, null,
					EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("2개월마다 휴가", "2개월마다 첫 부여일의 일자에 자동 부여되는 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N,
					RepeatUnit.MONTHLY, 2, null, null,
					LocalDateTime.of(now.getYear(), now.getMonthValue(), 10, 0, 0), YNType.Y, null,
					EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

			// QUARTERLY 예제들
			saveVacationPolicy("분기별 휴가", "매 분기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N,
					RepeatUnit.QUARTERLY, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("분기별 건강 휴가", "매 분기 15일에 자동 부여되는 건강 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("1.5000"), null, YNType.N,
					RepeatUnit.QUARTERLY, 1, null, 15, LocalDateTime.of(now.getYear(), 1, 15, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("분기별 특별 휴가", "매 분기 첫 부여일의 일자(20일)에 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("1.0000"), null, YNType.N,
					RepeatUnit.QUARTERLY, 1, null, null,
					LocalDateTime.of(now.getYear(), 1, 20, 0, 0), YNType.Y, null,
					EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

			// HALF 예제들
			saveVacationPolicy("반기별 휴가", "매 반기 1일에 자동 부여되는 휴가입니다.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("3.0000"), null, YNType.N,
					RepeatUnit.HALF, 1, null, 1, LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("반기별 리프레시", "매 반기 31일에 자동 부여되는 휴가입니다. 월말이 31일 미만이면 해당 월 마지막 날 부여.",
					VacationType.ANNUAL, GrantMethod.REPEAT_GRANT, new BigDecimal("2.5000"), null,
					YNType.N, RepeatUnit.HALF, 1, null, 31,
					LocalDateTime.of(now.getYear(), 1, 31, 0, 0), YNType.Y, null,
					EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);
			saveVacationPolicy("반기별 특별 휴가", "매 반기 첫 부여일의 일자(15일)에 자동 부여.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("2.0000"), null, YNType.N,
					RepeatUnit.HALF, 1, null, null, LocalDateTime.of(now.getYear(), 1, 15, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

			// DAILY 예제
			saveVacationPolicy("매일 포인트 휴가", "매일 자동 부여되는 포인트 휴가입니다. 0.1일씩 적립.", VacationType.ANNUAL,
					GrantMethod.REPEAT_GRANT, new BigDecimal("0.1000"), null, YNType.N,
					RepeatUnit.DAILY, 1, null, null, LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
					YNType.Y, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, null);

			// 구성원 신청용 휴가 정책 (ON_REQUEST - firstGrantDate, isRecurring, maxGrantCount 모두
			// null)
			// isFlexibleGrant = N (고정 시간 부여), isFlexibleGrant = Y (가변 부여, 예: OT는 시간 계산)
			saveVacationPolicy("동원훈련", "동원 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST,
					new BigDecimal("3.0000"), YNType.N, YNType.N, null, null, null, null, null,
					null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
			saveVacationPolicy("동미참훈련", "동미참 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST,
					new BigDecimal("1.0000"), YNType.N, YNType.N, null, null, null, null, null,
					null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
			saveVacationPolicy("예비군", "예비군 훈련에 대한 휴가 정책입니다.", VacationType.ARMY, GrantMethod.ON_REQUEST,
					new BigDecimal("1.0000"), YNType.N, YNType.N, null, null, null, null, null,
					null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
			saveVacationPolicy("예비군(반차)", "예비군 훈련에 대한 반차 휴가 정책입니다.", VacationType.ARMY,
					GrantMethod.ON_REQUEST, new BigDecimal("0.5000"), YNType.N, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.START_OF_YEAR,
					ExpirationType.END_OF_YEAR, 1);
			saveVacationPolicy("OT", "연장 근무에 대한 보상 휴가 정책입니다. 구성원이 직접 신청하는 휴가 정책입니다.", VacationType.OVERTIME,
					GrantMethod.ON_REQUEST, null, YNType.Y, YNType.Y, null, null, null, null, null,
					null, null, EffectiveType.START_OF_YEAR, ExpirationType.END_OF_YEAR, 1);
			saveVacationPolicy("결혼", "결혼에 대한 휴가 정책입니다.", VacationType.WEDDING, GrantMethod.ON_REQUEST,
					new BigDecimal("5.0000"), YNType.N, YNType.N, null, null, null, null, null,
					null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT,
					1);
			saveVacationPolicy("출산", "출산에 대한 휴가 정책입니다.", VacationType.MATERNITY, GrantMethod.ON_REQUEST,
					new BigDecimal("10.0000"), YNType.N, YNType.N, null, null, null, null, null,
					null, null, EffectiveType.IMMEDIATELY, ExpirationType.SIX_MONTHS_AFTER_GRANT,
					1);
			saveVacationPolicy("조사", "부친상, 모친상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT,
					GrantMethod.ON_REQUEST, new BigDecimal("5.0000"), YNType.N, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.ONE_MONTHS_AFTER_GRANT, 1);
			saveVacationPolicy("조사", "빙부상, 빙모상, 시부상, 시모상에 대한 휴가 정책입니다.", VacationType.BEREAVEMENT,
					GrantMethod.ON_REQUEST, new BigDecimal("3.0000"), YNType.N, YNType.N, null,
					null, null, null, null, null, null, EffectiveType.IMMEDIATELY,
					ExpirationType.ONE_MONTHS_AFTER_GRANT, 1);
		}

		public void initSetUserVacationPlan() {
			// 1. 기본 플랜 생성 (DEFAULT)
			VacationPlan defaultPlan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "모든 구성원에게 적용되는 기본 휴가 플랜");
			em.persist(defaultPlan);

			// 2. 7년 근속자용 플랜 생성 (SENIOR)
			VacationPlan seniorPlan = VacationPlan.createPlan("SENIOR", "7년 근속자 플랜", "7년 이상 근속자에게 추가 적용되는 휴가 플랜");
			em.persist(seniorPlan);

			// 3. DEFAULT 플랜에 정책 연결
			int sortOrder = 1;
			// 반복 부여 휴가 정책: 분기별 연차
			addPolicyToPlan(defaultPlan, policyMap.get("1분기 연차").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("2분기 연차").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("3분기 연차").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("4분기 연차").get(0), sortOrder++);
			// 구성원 신청용 휴가 정책
			addPolicyToPlan(defaultPlan, policyMap.get("동원훈련").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("동미참훈련").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("예비군").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("예비군(반차)").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("OT").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("결혼").get(0), sortOrder++);
			addPolicyToPlan(defaultPlan, policyMap.get("출산").get(0), sortOrder++);
			// 조사 정책들 추가
			List<VacationPolicy> bereavementPolicies = policyMap.get("조사");
			if (bereavementPolicies != null) {
				for (VacationPolicy policy : bereavementPolicies) {
					addPolicyToPlan(defaultPlan, policy, sortOrder++);
				}
			}

			// 4. SENIOR 플랜에 7년 근속 휴가 정책 추가
			addPolicyToPlan(seniorPlan, policyMap.get("7년 근속 휴가").get(0), 1);

			// 5. user1, user3~6에게 DEFAULT 플랜 할당
			assignPlanToUser(user1, defaultPlan);
			assignPlanToUser(user3, defaultPlan);
			assignPlanToUser(user4, defaultPlan);
			assignPlanToUser(user5, defaultPlan);
			assignPlanToUser(user6, defaultPlan);

			// 6. user2에게 DEFAULT + SENIOR 플랜 할당 (7년 근속자)
			assignPlanToUser(user2, defaultPlan);
			assignPlanToUser(user2, seniorPlan);
		}

		public void initSetVacationGrant() {
			LocalDateTime now = LocalDateTime.now();

			// 휴가 정책 조회
			VacationPolicy q1Policy = policyMap.get("1분기 연차").get(0);
			VacationPolicy q2Policy = policyMap.get("2분기 연차").get(0);
			VacationPolicy q3Policy = policyMap.get("3분기 연차").get(0);
			VacationPolicy q4Policy = policyMap.get("4분기 연차").get(0);
			VacationPolicy otPolicy = policyMap.get("OT(관리자용)").get(0);
			VacationPolicy maternityPolicy = policyMap.get("출산(관리자용)").get(0);

			List<VacationGrant> user1Grants = new ArrayList<>();
			List<VacationGrant> user1MaternityGrants = new ArrayList<>();
			List<VacationGrant> user2Grants = new ArrayList<>();
			List<VacationGrant> user3Grants = new ArrayList<>();
			List<VacationGrant> user4Grants = new ArrayList<>();
			List<VacationGrant> user5Grants = new ArrayList<>();
			List<VacationGrant> user6Grants = new ArrayList<>();

			// ===== user1 연차 부여 (현재 연도) =====
			user1Grants.add(saveVacationGrant(user1, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user1Grants.add(saveVacationGrant(user1, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user1Grants.add(saveVacationGrant(user1, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user1Grants.add(saveVacationGrant(user1, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));
			// user1 OT 부여 (3건)
			user1Grants.add(saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));
			user1Grants.add(saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.2500"), now.getYear()));
			user1Grants.add(saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));
			// user1 출산 휴가 부여 (사용 내역을 위해)
			user1MaternityGrants.add(saveVacationGrant(user1, maternityPolicy, VacationType.MATERNITY,
					"출산(관리자용)에 의한 휴가 부여", new BigDecimal("10.0000"), now.getYear()));

			// ===== user2 연차 부여 (현재 연도) =====
			user2Grants.add(saveVacationGrant(user2, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user2Grants.add(saveVacationGrant(user2, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user2Grants.add(saveVacationGrant(user2, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user2Grants.add(saveVacationGrant(user2, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));

			// ===== user3 연차 부여 (현재 연도) =====
			user3Grants.add(saveVacationGrant(user3, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user3Grants.add(saveVacationGrant(user3, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user3Grants.add(saveVacationGrant(user3, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user3Grants.add(saveVacationGrant(user3, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));

			// ===== user4 연차 부여 (현재 연도) =====
			user4Grants.add(saveVacationGrant(user4, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user4Grants.add(saveVacationGrant(user4, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user4Grants.add(saveVacationGrant(user4, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user4Grants.add(saveVacationGrant(user4, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));

			// ===== user5 연차 부여 (현재 연도) =====
			user5Grants.add(saveVacationGrant(user5, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user5Grants.add(saveVacationGrant(user5, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user5Grants.add(saveVacationGrant(user5, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user5Grants.add(saveVacationGrant(user5, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));
			// user5 OT 부여 (3건)
			user5Grants.add(saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));
			user5Grants.add(saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));
			user5Grants.add(saveVacationGrant(user5, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));

			// ===== user6 연차 부여 (현재 연도) =====
			user6Grants.add(saveVacationGrant(user6, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user6Grants.add(saveVacationGrant(user6, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user6Grants.add(saveVacationGrant(user6, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear()));
			user6Grants.add(saveVacationGrant(user6, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear()));
			// user6 OT 부여
			user6Grants.add(saveVacationGrant(user6, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear()));

			// ===== user1 다음 연도 휴가 부여 =====
			user1Grants.add(saveVacationGrant(user1, q1Policy, VacationType.ANNUAL, "1분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear() + 1));
			user1Grants.add(saveVacationGrant(user1, q2Policy, VacationType.ANNUAL, "2분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear() + 1));
			user1Grants.add(saveVacationGrant(user1, q3Policy, VacationType.ANNUAL, "3분기 연차에 의한 휴가 부여",
					new BigDecimal("4.0000"), now.getYear() + 1));
			user1Grants.add(saveVacationGrant(user1, q4Policy, VacationType.ANNUAL, "4분기 연차에 의한 휴가 부여",
					new BigDecimal("3.0000"), now.getYear() + 1));
			// user1 다음 연도 OT 부여 (2건)
			user1Grants.add(saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.1250"), now.getYear() + 1));
			user1Grants.add(saveVacationGrant(user1, otPolicy, VacationType.OVERTIME, "OT(관리자용)에 의한 휴가 부여",
					new BigDecimal("0.3750"), now.getYear() + 1));

			// ===== 휴가 사용 내역 마이그레이션 (VacationUsage + VacationUsageDeduction) =====

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

		public void initSetWorkCode() {
			WorkCode group = saveWorkCode("work_group", "업무 그룹", CodeType.LABEL, null, 0);

			WorkCode assignment = saveWorkCode("assignment", "과제", CodeType.OPTION, group, 1);
			WorkCode assignmentWorkPart = saveWorkCode("work_part", "업무 파트", CodeType.LABEL, assignment, 0);

			saveWorkCode("assignment_1", "업무이력 개발", CodeType.OPTION, assignmentWorkPart, 1);
			saveWorkCode("assignment_2", "휴가 사용 개발", CodeType.OPTION, assignmentWorkPart, 2);
			saveWorkCode("assignment_3", "권한 로직 개발", CodeType.OPTION, assignmentWorkPart, 3);

			WorkCode operation = saveWorkCode("operation", "운영", CodeType.OPTION, group, 2);
			WorkCode operationWorkPart = saveWorkCode("work_part", "업무 파트", CodeType.LABEL, operation, 0);

			saveWorkCode("operation_1", "시스템1 운영", CodeType.OPTION, operationWorkPart, 1);
			saveWorkCode("operation_2", "시스템2 운영", CodeType.OPTION, operationWorkPart, 2);
			saveWorkCode("operation_3", "시스템3 운영", CodeType.OPTION, operationWorkPart, 3);
			saveWorkCode("operation_4", "시스템4 운영", CodeType.OPTION, operationWorkPart, 4);
			saveWorkCode("operation_5", "시스템5 운영", CodeType.OPTION, operationWorkPart, 5);

			WorkCode project = saveWorkCode("project", "프로젝트", CodeType.OPTION, group, 3);
			WorkCode projectWorkPart = saveWorkCode("work_part", "업무 파트", CodeType.LABEL, project, 0);

			saveWorkCode("project_1", "신규 hr 개발 프로젝트", CodeType.OPTION, projectWorkPart, 1);

			WorkCode etc = saveWorkCode("etc", "기타", CodeType.OPTION, group, 4);
			WorkCode etcWorkPart = saveWorkCode("work_part", "업무 파트", CodeType.LABEL, etc, 0);

			saveWorkCode("etc_1", "기타", CodeType.OPTION, etcWorkPart, 1);

			WorkCode division = saveWorkCode("work_division", "업무 구분", CodeType.LABEL, null, 0);

			saveWorkCode("division_1", "회의", CodeType.OPTION, division, 1);
			saveWorkCode("division_2", "문서작성", CodeType.OPTION, division, 2);
			saveWorkCode("division_3", "개발", CodeType.OPTION, division, 3);
			saveWorkCode("division_4", "테스트", CodeType.OPTION, division, 4);
			saveWorkCode("division_5", "교육", CodeType.OPTION, division, 5);
			saveWorkCode("division_6", "휴가", CodeType.OPTION, division, 6);
		}

		public User saveMember(String id, String name, String email, LocalDate birth, OriginCompanyType company,
				String workTime, YNType lunar) {
			String encodedPassword = passwordEncoder.encode("1234");
			User user = User.createUser(id, encodedPassword, name, email, birth, company, workTime, lunar,
					null, null, CountryCode.KR);
			em.persist(user);
			return user;
		}

		public Department saveDepartment(String name, String nameKR, Department parent, User leader, Long level,
				String desc,
				String color, Company company) {
			Department department = Department.createDepartment(name, nameKR, parent, leader, level,
					desc,
					color, company);
			em.persist(department);
			return department;
		}

		public Holiday saveHoliday(String name, LocalDate date, HolidayType type, CountryCode countryCode,
				YNType lunarYN, LocalDate lunarDate, YNType isRecurring, String icon) {
			Holiday holiday = Holiday.createHoliday(name, date, type, countryCode, lunarYN, lunarDate,
					isRecurring, icon);
			em.persist(holiday);
			return holiday;
		}

		public Schedule saveSchedule(User user, String desc, ScheduleType type, LocalDateTime startDate,
				LocalDateTime endDate) {
			Schedule schedule = Schedule.createSchedule(user, desc, type, startDate, endDate);
			em.persist(schedule);
			return schedule;
		}

		public Dues saveDues(String userName, Long amount, DuesType type, DuesCalcType calc, LocalDate date,
				String detail) {
			Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);
			em.persist(dues);
			return dues;
		}

		public VacationPolicy saveVacationPolicy(String name, String desc, VacationType vacationType,
				GrantMethod grantMethod, BigDecimal grantTime, YNType isFlexibleGrant,
				YNType minuteGrantYn, RepeatUnit repeatUnit, Integer repeatInterval,
				Integer specificMonths, Integer specificDays, LocalDateTime firstGrantDate,
				YNType isRecurring, Integer maxGrantCount, EffectiveType effectiveType,
				ExpirationType expirationType, Integer approvalRequiredCount) {
			VacationPolicy policy;
			switch (grantMethod) {
				case MANUAL_GRANT -> policy = VacationPolicy.createManualGrantPolicy(name, desc,
						vacationType, grantTime, isFlexibleGrant, minuteGrantYn, effectiveType,
						expirationType);
				case REPEAT_GRANT -> policy = VacationPolicy.createRepeatGrantPolicy(name, desc,
						vacationType, grantTime, minuteGrantYn, repeatUnit, repeatInterval,
						specificMonths, specificDays, firstGrantDate, isRecurring,
						maxGrantCount, effectiveType, expirationType);
				case ON_REQUEST -> policy = VacationPolicy.createOnRequestPolicy(name, desc,
						vacationType, grantTime, isFlexibleGrant, minuteGrantYn,
						approvalRequiredCount, effectiveType, expirationType);
				default -> {
					return null;
				}
			}

			em.persist(policy);
			if (policy.getGrantMethod().equals(GrantMethod.MANUAL_GRANT)) {
				policy.updateCantDeleted();
			} else {
				policy.updateCanDeleted();
			}

			policyMap.computeIfAbsent(name, k -> new ArrayList<>()).add(policy);

			return policy;
		}

		public WorkCode saveWorkCode(String code, String name, CodeType type, WorkCode parent,
				Integer orderSeq) {
			WorkCode workCode = WorkCode.createWorkCode(code, name, type, parent, orderSeq);
			em.persist(workCode);
			workCodeMap.put(code, workCode);
			return workCode;
		}

		private WorkHistory saveWorkHistory(LocalDate date, User user, WorkCode group, WorkCode part,
				WorkCode division, BigDecimal hours, String content) {
			WorkHistory workHistory = WorkHistory.createWorkHistory(date, user, group, part, division,
					hours, content);
			em.persist(workHistory);
			return workHistory;
		}

		public void initSetWorkHistory() {
			// Group -> Part 관계 정의
			Map<String, List<String>> groupPartMap = new HashMap<>();
			groupPartMap.put("assignment", List.of("assignment_1", "assignment_2", "assignment_3"));
			groupPartMap.put("operation", List.of("operation_1", "operation_2", "operation_3",
					"operation_4", "operation_5"));
			groupPartMap.put("project", List.of("project_1"));
			groupPartMap.put("etc", List.of("etc_1"));

			List<String> groups = List.of("assignment", "operation", "project", "etc");
			List<String> divisions = List.of("division_1", "division_2", "division_3", "division_4",
					"division_5", "division_6");
			List<User> users = List.of(user1, user2, user3, user4, user5, user6);
			Random random = new Random();

			for (int i = 0; i < 100; i++) {
				// Random User
				User user = users.get(random.nextInt(users.size()));

				// Random Group
				String groupCodeStr = groups.get(random.nextInt(groups.size()));
				WorkCode group = workCodeMap.get(groupCodeStr);

				// Valid Part for Group
				List<String> parts = groupPartMap.get(groupCodeStr);
				String partCodeStr = parts.get(random.nextInt(parts.size()));
				WorkCode part = workCodeMap.get(partCodeStr);

				// Random Division
				String divisionCodeStr = divisions.get(random.nextInt(divisions.size()));
				WorkCode division = workCodeMap.get(divisionCodeStr);

				// Random Date (2025년 내)
				LocalDate date = LocalDate.of(2025, random.nextInt(12) + 1, random.nextInt(28) + 1);

				// Random Hours (1 ~ 8)
				BigDecimal hours = new BigDecimal(random.nextInt(8) + 1);

				// Content
				String content = "업무 이력 테스트 데이터 " + (i + 1);

				saveWorkHistory(date, user, group, part, division, hours, content);
			}
		}

		private void addPolicyToPlan(VacationPlan plan, VacationPolicy policy, int sortOrder) {
			VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, sortOrder, YNType.N);
			em.persist(planPolicy);
		}

		private void assignPlanToUser(User user, VacationPlan plan) {
			UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
			em.persist(userPlan);

			// REPEAT_GRANT 정책에 대해 VacationGrantSchedule 생성
			for (VacationPlanPolicy planPolicy : plan.getVacationPlanPolicies()) {
				VacationPolicy policy = planPolicy.getVacationPolicy();
				if (policy.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
					VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, policy);
					em.persist(schedule);
				}
			}
		}

		private VacationGrant saveVacationGrant(User user, VacationPolicy policy, VacationType type,
				String desc, BigDecimal grantTime, int year) {
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
				startDate = LocalDateTime.of(now.getYear(), now.getMonthValue() - 3,
						now.getDayOfMonth(), 0, 0, 0);
				expiryDate = startDate.plusMonths(6).minusSeconds(1);
			} else {
				// 기타
				startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
				expiryDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
			}

			VacationGrant grant = VacationGrant.createVacationGrant(
					user, policy, desc, type, grantTime,
					startDate, expiryDate);
			em.persist(grant);
			return grant;
		}

		private VacationUsage saveVacationUsageWithFIFO(User user, List<VacationGrant> grants, String desc,
				VacationTimeType timeType, LocalDateTime startDate,
				LocalDateTime endDate, BigDecimal usedTime) {
			// VacationUsage 생성
			VacationUsage usage = VacationUsage.createVacationUsage(
					user, desc, timeType, startDate, endDate, usedTime);
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
					VacationUsageDeduction deduction = VacationUsageDeduction
							.createVacationUsageDeduction(
									usage, grant, deductibleTime);
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
			return usage;
		}
	}
}
