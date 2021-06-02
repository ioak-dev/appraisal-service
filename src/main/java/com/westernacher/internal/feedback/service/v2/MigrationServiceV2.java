package com.westernacher.internal.feedback.service.v2;

import com.westernacher.internal.feedback.domain.v2.MigrationOutputV2;

import java.util.List;
import java.util.Map;

public interface MigrationServiceV2 {

    void migratePrerequisiteData();

    MigrationOutputV2 getAppraisalData(String cycleId);

    void loadAppraisalData(MigrationOutputV2 appraisalData);
}
