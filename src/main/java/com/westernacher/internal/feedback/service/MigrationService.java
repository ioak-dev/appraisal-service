package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.controller.representation.MigrationAppraisalResponse;
import com.westernacher.internal.feedback.domain.*;
import java.util.List;
import java.util.Map;

public interface MigrationService {

    Map<String, MigrationAppraisalPerson> getPersonMap(List<MigrationAppraisalPerson> migrationAppraisalPeople);

    MigrationAppraisalResponse migrate(String cycleId, List<MigrationAppraisal> appraisalList, Map<String, MigrationAppraisalPerson> personMap, Map<String, Integer> goalOrder);

    void migrateToNewDb(MigrationOutput output);
}
