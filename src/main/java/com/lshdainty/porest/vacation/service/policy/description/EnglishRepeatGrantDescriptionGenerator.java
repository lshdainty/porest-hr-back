package com.lshdainty.porest.vacation.service.policy.description;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * English repeat grant policy description generator
 * Examples:
 * - "Grant annually on January 1st"
 * - "Grant every 2 years on January 1st"
 * - "Grant monthly on the 15th"
 * - "Grant quarterly on the 1st"
 * - "Grant semi-annually"
 */
@Component
public class EnglishRepeatGrantDescriptionGenerator implements RepeatGrantDescriptionGenerator {

    private static final String[] MONTH_NAMES = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

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

        StringBuilder description = new StringBuilder("Grant ");

        // 1. 반복 단위와 간격
        if (repeatInterval != null && repeatInterval > 1) {
            description.append("every ").append(repeatInterval).append(" ").append(getRepeatUnitPlural(repeatUnit));
        } else {
            description.append(getRepeatUnitName(repeatUnit));
        }

        // 2. 특정 월/일 (반복 단위에 따라)
        switch (repeatUnit) {
            case YEARLY:
                if (specificMonths != null) {
                    description.append(" on ").append(MONTH_NAMES[specificMonths]);
                    if (specificDays != null) {
                        description.append(" ").append(getOrdinal(specificDays));
                    }
                } else if (specificDays != null) {
                    description.append(" on the ").append(getOrdinal(specificDays));
                }
                break;

            case MONTHLY:
                if (specificDays != null) {
                    description.append(" on the ").append(getOrdinal(specificDays));
                }
                break;

            case QUARTERLY:
            case HALF:
                if (specificDays != null) {
                    description.append(" on the ").append(getOrdinal(specificDays));
                }
                break;

            case DAILY:
                break;
        }

        // 3. 1회성 부여 여부
        if (YNType.isN(isRecurring) && maxGrantCount != null) {
            LocalDateTime firstGrantDate = policy.getFirstGrantDate();
            if (firstGrantDate != null && repeatUnit == RepeatUnit.YEARLY && repeatInterval != null) {
                int yearsLater = repeatInterval * (maxGrantCount - 1);
                if (yearsLater > 0) {
                    description.append(", after ").append(yearsLater).append(" year(s)");
                }
            }
            description.append(", ").append(maxGrantCount).append(" time(s) only");
        }

        return description.toString();
    }

    private String getRepeatUnitName(RepeatUnit repeatUnit) {
        switch (repeatUnit) {
            case YEARLY:
                return "annually";
            case MONTHLY:
                return "monthly";
            case DAILY:
                return "daily";
            case QUARTERLY:
                return "quarterly";
            case HALF:
                return "semi-annually";
            default:
                return "";
        }
    }

    private String getRepeatUnitPlural(RepeatUnit repeatUnit) {
        switch (repeatUnit) {
            case YEARLY:
                return "years";
            case MONTHLY:
                return "months";
            case DAILY:
                return "days";
            case QUARTERLY:
                return "quarters";
            case HALF:
                return "half-years";
            default:
                return "";
        }
    }

    private String getOrdinal(int number) {
        if (number >= 11 && number <= 13) {
            return number + "th";
        }
        switch (number % 10) {
            case 1:
                return number + "st";
            case 2:
                return number + "nd";
            case 3:
                return number + "rd";
            default:
                return number + "th";
        }
    }
}
