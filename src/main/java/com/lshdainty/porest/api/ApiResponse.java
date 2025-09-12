package com.lshdainty.porest.api;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Collection;

@Slf4j
@Getter
@RequiredArgsConstructor
@Builder
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final int count;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "success", getCount(data), data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(HttpStatus.OK.value(), "success", 0, null);
    }

    private static <T> int getCount(T data) {
        if (data instanceof Object[]) { // array
            return ((Object[]) data).length;
        } else if (data instanceof Collection) {    // List, Set, Map
            return ((Collection<?>) data).size();
        } else {
            // not thing to do
            return 0;
        }
    }
}
