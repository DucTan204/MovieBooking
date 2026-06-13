// com/moviebooking/dto/PricingRuleDTO.java
package com.moviebooking.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PricingRuleDTO {
    private Long      id;
    private Long      cinemaId;
    private String    cinemaName;  // chỉ read
    private String    seatType;    // NORMAL | VIP | COUPLE
    private String    audienceType;// ADULT | CHILD | STUDENT | SENIOR
    private BigDecimal basePrice;
    private Integer   discountPercent;
}