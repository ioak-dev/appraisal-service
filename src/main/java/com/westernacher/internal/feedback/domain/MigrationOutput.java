package com.westernacher.internal.feedback.domain;

import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
