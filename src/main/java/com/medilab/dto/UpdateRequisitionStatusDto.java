package com.medilab.dto;

import com.medilab.entity.SampleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRequisitionStatusDto {

    @NotNull(message = "Status cannot be null")
    private SampleStatus status;

}