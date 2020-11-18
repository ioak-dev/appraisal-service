package com.westernacher.internal.feedback.domain;

import com.bol.secure.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "goal")
public class Goal {
    @Id
    private String id;
    private String job;
    private String cu;

    @Encrypted
    private String group;

    @Encrypted
    private String criteria;

    @Encrypted
    private float weightage;

    @Encrypted
    private String description;

    @Encrypted
    private int order;
}
