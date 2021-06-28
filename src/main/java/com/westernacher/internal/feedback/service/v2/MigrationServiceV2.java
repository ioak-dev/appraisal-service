package com.westernacher.internal.feedback.service.v2;

import com.westernacher.internal.feedback.domain.v2.GetAndLoadOutput;
import com.westernacher.internal.feedback.domain.v2.MigrationOutputV2;

public interface MigrationServiceV2 {

    MigrationOutputV2 getAppraisalData(String cycleId);

    void loadAppraisalData(MigrationOutputV2 appraisalData);

    GetAndLoadOutput geMigrationOutputCount();
}
