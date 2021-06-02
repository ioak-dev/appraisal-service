package com.westernacher.internal.feedback.domain.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appraisal.long")
public class AppraisalLong {
    @Id
    private String id;
    private String headerId;
    private int orderId;
    private int rating;
    private String comment;
}
