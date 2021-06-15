package com.westernacher.internal.feedback.domain.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "goal.employee")
public class GoalEmployee {
    @Id
    private String id;
    private String employeeId;
    private int orderId;
    private String description;
    private Date createdDate;

    @CreatedDate
    private Date auditCreateDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
