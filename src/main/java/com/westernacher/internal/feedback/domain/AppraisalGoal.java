package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appraisal.goal")
public class AppraisalGoal {
    @Id
    private String id;
    private String job;

    @Encrypted
    private String group;

    @Encrypted
    private String criteria;

    @Encrypted
    private Float weightage;

    @Encrypted
    private String description;
    private String cycleId;

    @Encrypted
    private int order;
    private String cu;
}
