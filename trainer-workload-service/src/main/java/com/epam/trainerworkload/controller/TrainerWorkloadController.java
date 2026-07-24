package com.epam.trainerworkload.controller;
import com.epam.trainerworkload.dto.MonthlyWorkloadResponse;
import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workload-events")
@RequiredArgsConstructor
@Validated
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping
    public ResponseEntity<Void> updateWorkload(
            @Valid @RequestBody TrainerWorkloadRequest request
    ) {
        trainerWorkloadService.updateWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlyWorkload(
            @PathVariable
            @NotBlank
            String username,

            @RequestParam
            @Min(1)
            int year,

            @RequestParam
            @Min(1)
            @Max(12)
            int month
    ) {
        MonthlyWorkloadResponse response =
                trainerWorkloadService.getMonthlyWorkload(
                        username,
                        year,
                        month
                );

        return ResponseEntity.ok(response);
    }
}