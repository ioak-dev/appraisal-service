package com.westernacher.internal.feedback.domain.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "v2goal")
public class Goal {
    @Id
    private String id;
    private int order;
    private String criteria;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;

}
