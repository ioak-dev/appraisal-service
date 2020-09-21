package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalResponse;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalGoal;
import com.westernacher.internal.feedback.domain.MigrationAppraisal;
import com.westernacher.internal.feedback.domain.MigrationAppraisalPerson;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MigrationService {
    public MigrationAppraisalResponse migrate(List<MigrationAppraisal> appraisalList, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder) {
        MigrationAppraisalResponse response = new MigrationAppraisalResponse();
        migrateAppraisal(response, appraisalList.get(0));
        return response;
    }

    public Map<String, MigrationAppraisalPerson> getPersonMap(List<MigrationAppraisalPerson> migrationAppraisalPeople) {
        Map<String, MigrationAppraisalPerson> personMap = new HashMap<>();
        for (MigrationAppraisalPerson person:migrationAppraisalPeople) {
            personMap.put(person.getId(), person);
        }
        return personMap;
    }

    private void migrateAppraisal(MigrationAppraisalResponse response, MigrationAppraisal appraisal) {

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
