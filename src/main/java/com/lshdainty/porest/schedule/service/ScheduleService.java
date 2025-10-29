package com.lshdainty.porest.schedule.service;

import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.repository.ScheduleRepositoryImpl;
import com.lshdainty.porest.schedule.service.dto.ScheduleServiceDto;
import com.lshdainty.porest.common.util.PorestTime;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleService {
    private final MessageSource ms;
    private final ScheduleRepositoryImpl scheduleRepositoryImpl;
    private final UserService userService;

    @Transactional
    public Long registSchedule(ScheduleServiceDto data) {
        // 유저 조회
        User user = userService.checkUserExist(data.getUserId());

        if (PorestTime.isAfterThanEndDate(data.getStartDate(), data.getEndDate())) { throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null)); }

        Schedule schedule = Schedule.createSchedule(
                user,
                data.getDesc(),
                data.getType(),
                data.getStartDate(),
                data.getEndDate()
        );

        // 휴가 등록
        scheduleRepositoryImpl.save(schedule);

        return schedule.getId();
    }

    public List<Schedule> searchSchedulesByUser(String userId) {
        return scheduleRepositoryImpl.findSchedulesByUserId(userId);
    }

    public List<Schedule> searchSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        if (PorestTime.isAfterThanEndDate(start, end)) { throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null)); }
        return scheduleRepositoryImpl.findSchedulesByPeriod(start, end);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = checkScheduleExist(scheduleId);

        if (schedule.getEndDate().isBefore(LocalDateTime.now())) { throw new IllegalArgumentException(ms.getMessage("error.validate.delete.isBeforeThanNow", null, null)); }

        schedule.deleteSchedule();
    }

    public Schedule checkScheduleExist(Long scheduleId) {
        Optional<Schedule> schedule = scheduleRepositoryImpl.findById(scheduleId);
        if (schedule.isEmpty() || schedule.get().getIsDeleted().equals("Y")) { throw new IllegalArgumentException(ms.getMessage("error.notfound.schedule", null, null)); }
        return schedule.get();
    }
}
