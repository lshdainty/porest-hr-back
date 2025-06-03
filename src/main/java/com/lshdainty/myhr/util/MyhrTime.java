package com.lshdainty.myhr.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyhrTime {
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
     * @return start ~ end 사이의 모든 날짜들
     */
    public static List<LocalDate> getBetweenDates(LocalDateTime start, LocalDateTime end) {
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
     * @return 요일에 해당하는 모든 날짜들
     */
    public static List<LocalDate> getBetweenDatesByDayOfWeek(LocalDateTime start, LocalDateTime end, int[] daysOfWeek) {
        List<DayOfWeek> targetDays = new ArrayList<>();
        for (int day : daysOfWeek) {
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
}
