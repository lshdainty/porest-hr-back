package com.lshdainty.porest.schedule.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.util.PorestTime;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.repository.ScheduleRepository;
import com.lshdainty.porest.schedule.service.dto.ScheduleServiceDto;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Long registSchedule(ScheduleServiceDto data) {
        log.debug("일정 등록 시작: userId={}, type={}", data.getUserId(), data.getType());
        // 유저 조회
        User user = userService.checkUserExist(data.getUserId());

        if (PorestTime.isAfterThanEndDate(data.getStartDate(), data.getEndDate())) {
            log.warn("일정 등록 실패 - 시작일이 종료일보다 이후: startDate={}, endDate={}", data.getStartDate(), data.getEndDate());
            throw new InvalidValueException(ErrorCode.SCHEDULE_INVALID_DATE);
        }

        Schedule schedule = Schedule.createSchedule(
                user,
                data.getDesc(),
                data.getType(),
                data.getStartDate(),
                data.getEndDate()
        );

        // 휴가 등록
        scheduleRepository.save(schedule);
        log.info("일정 등록 완료: scheduleId={}, userId={}", schedule.getId(), data.getUserId());

        return schedule.getId();
    }

    @Override
    public List<Schedule> searchSchedulesByUser(String userId) {
        log.debug("사용자별 일정 조회: userId={}", userId);
        return scheduleRepository.findSchedulesByUserId(userId);
    }

    @Override
    public List<Schedule> searchSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        log.debug("기간별 일정 조회: start={}, end={}", start, end);
        if (PorestTime.isAfterThanEndDate(start, end)) {
            log.warn("일정 조회 실패 - 시작일이 종료일보다 이후: start={}, end={}", start, end);
            throw new InvalidValueException(ErrorCode.SCHEDULE_INVALID_DATE);
        }
        return scheduleRepository.findSchedulesByPeriod(start, end);
    }

    @Override
    @Transactional
    public Long updateSchedule(Long scheduleId, ScheduleServiceDto data) {
        log.debug("일정 수정 시작: scheduleId={}", scheduleId);
        // 1. 기존 스케줄 삭제
        deleteSchedule(scheduleId);

        // 2. 새로운 스케줄 등록
        Long newScheduleId = registSchedule(data);

        log.info("스케줄 수정 완료 - 기존 ID: {}, 새로운 ID: {}", scheduleId, newScheduleId);

        return newScheduleId;
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        log.debug("일정 삭제 시작: scheduleId={}", scheduleId);
        Schedule schedule = checkScheduleExist(scheduleId);

        if (schedule.getEndDate().isBefore(LocalDateTime.now())) {
            log.warn("일정 삭제 실패 - 종료일이 현재보다 이전: scheduleId={}, endDate={}", scheduleId, schedule.getEndDate());
            throw new BusinessRuleViolationException(ErrorCode.SCHEDULE_INVALID_DATE);
        }

        schedule.deleteSchedule();
        log.info("일정 삭제 완료: scheduleId={}", scheduleId);
    }

    @Override
    public Schedule checkScheduleExist(Long scheduleId) {
        Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isEmpty() || YNType.isY(schedule.get().getIsDeleted())) {
            log.warn("일정 조회 실패 - 존재하지 않는 일정: scheduleId={}", scheduleId);
            throw new EntityNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        return schedule.get();
    }
}
