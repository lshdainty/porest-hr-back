package com.lshdainty.myhr.util;

import org.springframework.context.MessageSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PorestTime {
    /**
     * startDate와 endDate 순서 체크 함수<br>
     * startDate > endDate : true<br>
     * startDate <= endDate : false
     *
     * @param start 시작시간(LocalDateTime)
     * @param end 종료시간(LocalDateTime)
     * @return boolean
     */
    public static boolean isAfterThanEndDate(LocalDateTime start, LocalDateTime end) {
        return start.isAfter(end);
    }

    /**
     * startDate, endDate 사이의<br>
     * 모든 날짜들의 목록을 반환
     *
     * @param start 시작시간(LocalDateTime)
     * @param end 종료시간(LocalDateTime)
     * @param ms MessageSource
     * @return start ~ end 사이의 모든 날짜들
     */
    public static List<LocalDate> getBetweenDates(LocalDateTime start, LocalDateTime end, MessageSource ms) {
        if (start.isAfter(end)) throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null));
        return start.toLocalDate().datesUntil(end.toLocalDate().plusDays(1))
                .collect(Collectors.toList());
    }

    /**
     * 시작, 끝 기간에 해당하는 모든 날짜 중<br>
     * 사용자가 선택한 요일에 해당하는 모든 날짜를 반환하는 함수
     *
     * @param start 시작시간(LocalDateTime)
     * @param end 종료시간(LocalDateTime)
     * @param daysOfWeek int로 된 요일 리스트 (1 월요일 ~ 7 일요일)
     * @param ms MessageSource
     * @return 요일에 해당하는 모든 날짜들
     */
    public static List<LocalDate> getBetweenDatesByDayOfWeek(LocalDateTime start, LocalDateTime end, int[] daysOfWeek, MessageSource ms) {
        List<DayOfWeek> targetDays = new ArrayList<>();
        for (int day : daysOfWeek) {
            if (day < 1 || day > 7) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.dayOfWeek", null, null));
            }
            targetDays.add(DayOfWeek.of(day));
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate checkDay = start.toLocalDate();
        while (!checkDay.isAfter(end.toLocalDate())) {
            if (targetDays.contains(checkDay.getDayOfWeek())) {
                dates.add(checkDay);
            }
            checkDay = checkDay.plusDays(1);
        }

        return dates;
    }

    /**
     * 원본 날짜 집합에서 특정 날짜 집합을 더한 후<br>
     * List로 변환하여 반환
     *
     * @param sourceDates 원본 날짜 리스트들
     * @param targetDates 추가할 날짜 리스트들
     * @return source + target을 한 날짜 리스트 반환
     */
    public static List<LocalDate> addAllDates(List<LocalDate> sourceDates, List<LocalDate> targetDates) {
        Set<LocalDate> sourceSet = new HashSet<>(sourceDates);
        Set<LocalDate> targetSet = new HashSet<>(targetDates);
        sourceSet.addAll(targetSet);
        return sourceSet.stream()
                .map(LocalDate::from)
                .collect(Collectors.toList());
    }

    /**
     * 원본 날짜 집합에서 특정 날짜 집합을 뺀 후<br>
     * List로 변환하여 반환
     *
     * @param sourceDates 원본 날짜 리스트들
     * @param targetDates 제외할 날짜 리스트들
     * @return source - target을 한 날짜 리스트 반환
     */
    public static List<LocalDate> removeAllDates(List<LocalDate> sourceDates, List<LocalDate> targetDates) {
        Set<LocalDate> sourceSet = new HashSet<>(sourceDates);
        Set<LocalDate> targetSet = new HashSet<>(targetDates);
        sourceSet.removeAll(targetSet);
        return sourceSet.stream()
                .map(LocalDate::from)
                .collect(Collectors.toList());
    }

    /**
     * List의 시간 값 중에<br>
     * 가장 큰 시간 값을 반환하는 함수
     *
     * @param dateTimes 시간 리스트
     * @param ms MessageSource
     * @return LocalDateTime 타입의 가장 큰 시간
     */
    public static LocalDateTime findMaxDateTime(List<LocalDateTime> dateTimes, MessageSource ms) {
        if (dateTimes == null || dateTimes.isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.parameter.null", null, null));
        }

        return dateTimes.stream()
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new NoSuchElementException(ms.getMessage("error.notfound.max", null, null)));
    }

    /**
     * List의 시간 값 중에<br>
     * 가장 작은 시간 값을 반환하는 함수
     *
     * @param dateTimes 시간 리스트
     * @param ms MessageSource
     * @return LocalDateTime 타입의 가장 작은 시간
     */
    public static LocalDateTime findMinDateTime(List<LocalDateTime> dateTimes, MessageSource ms) {
        if (dateTimes == null || dateTimes.isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.parameter.null", null, null));
        }

        return dateTimes.stream()
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NoSuchElementException(ms.getMessage("error.notfound.min", null, null)));
    }
}
