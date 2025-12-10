package com.lshdainty.porest.vacation.service.policy.description;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 한국어 반복 부여 정책 설명 생성기
 * 예시:
 * - "매년 1월 1일 부여"
 * - "2년 간격으로 매년 1월 1일 부여"
 * - "매월 15일 부여"
 * - "분기마다 1일 부여"
 * - "반기마다 부여"
 */
@Component
public class KoreanRepeatGrantDescriptionGenerator implements RepeatGrantDescriptionGenerator {

    @Override
    public String generate(VacationPolicy policy) {
        if (policy == null || policy.getGrantMethod() != GrantMethod.REPEAT_GRANT) {
            return null;
        }

        RepeatUnit repeatUnit = policy.getRepeatUnit();
        Integer repeatInterval = policy.getRepeatInterval();
        Integer specificMonths = policy.getSpecificMonths();
        Integer specificDays = policy.getSpecificDays();
        YNType isRecurring = policy.getIsRecurring();
        Integer maxGrantCount = policy.getMaxGrantCount();

        StringBuilder description = new StringBuilder();

        // 1. 반복 간격 (2년 이상일 경우만 표시)
        if (repeatInterval != null && repeatInterval > 1) {
            description.append(repeatInterval).append(getRepeatUnitSuffix(repeatUnit)).append(" 간격으로 ");
        }

        // 2. 반복 단위 (매년, 매월, 매일, 분기, 반기)
        if (repeatUnit != null) {
            description.append(getRepeatUnitName(repeatUnit));
        }

        // 3. 특정 월/일 (반복 단위에 따라)
        switch (repeatUnit) {
            case YEARLY:
                if (specificMonths != null) {
                    description.append(" ").append(specificMonths).append("월");
                }
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case MONTHLY:
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case QUARTERLY:
            case HALF:
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case DAILY:
                break;
        }

        // 4. "부여" 추가
        description.append(" 부여");

        // 5. 1회성 부여 여부
        if (YNType.isN(isRecurring) && maxGrantCount != null) {
            LocalDateTime firstGrantDate = policy.getFirstGrantDate();
            if (firstGrantDate != null && repeatUnit == RepeatUnit.YEARLY && repeatInterval != null) {
                int yearsLater = repeatInterval * (maxGrantCount - 1);
                if (yearsLater > 0) {
                    description.append(", ").append(yearsLater).append("년 후");
                }
            }
            description.append(" ").append(maxGrantCount).append("회 부여");
        }

        return description.toString();
    }

    private String getRepeatUnitName(RepeatUnit repeatUnit) {
        switch (repeatUnit) {
            case YEARLY:
                return "매년";
            case MONTHLY:
                return "매월";
            case DAILY:
                return "매일";
            case QUARTERLY:
                return "분기마다";
            case HALF:
                return "반기마다";
            default:
                return "";
        }
    }

    private String getRepeatUnitSuffix(RepeatUnit repeatUnit) {
        switch (repeatUnit) {
            case YEARLY:
                return "년";
            case MONTHLY:
                return "개월";
            case DAILY:
                return "일";
            case QUARTERLY:
                return "분기";
            case HALF:
                return "반기";
            default:
                return "";
        }
    }
}
