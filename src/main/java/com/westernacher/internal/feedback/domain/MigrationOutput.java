package com.westernacher.internal.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationOutput {
    List<AppraisalReview> appraisalReviews;
    List<AppraisalGoal> appraisalGoals;
    List<AppraisalRole> appraisalRoles;
    List<AppraisalReviewGoal> appraisalReviewGoals;
    List<AppraisalReviewMaster> appraisalReviewMasters;
    List<Person> persons;
}
