package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalResponse;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalGoal;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MigrationService {
    public MigrationAppraisalResponse migrate(List<Appraisal> appraisalList) {
        MigrationAppraisalResponse response = new MigrationAppraisalResponse();
        migrateAppraisal(response, appraisalList.get(0));
        return response;
    }

    private void migrateAppraisal(MigrationAppraisalResponse response, Appraisal appraisal) {

        appraisal.getSectiononeResponse().forEach(group -> {
            AppraisalGoal appraisalGoal = new AppraisalGoal();
            appraisalGoal.setId((new ObjectId()).toString());
            appraisalGoal.setGroup(group.getGroup());
            group.getResponse().forEach(criteria -> {
                appraisalGoal.setCriteria(criteria.getCriteria());
                appraisalGoal.setDescription(criteria.getDescription());
                appraisalGoal.setWeightage(criteria.getWeightage());
                appraisalGoal.setCycleId("cycleid");
            });

            response.addAppraisalGoal(appraisalGoal);
        });
    }
}
