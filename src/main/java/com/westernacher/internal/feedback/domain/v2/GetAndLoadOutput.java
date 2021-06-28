package com.westernacher.internal.feedback.domain.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAndLoadOutput {

    private Long appraisalHeaderCount;
    private Long appraisalLongCount;
    private Long goalEmployeeCount;
}
