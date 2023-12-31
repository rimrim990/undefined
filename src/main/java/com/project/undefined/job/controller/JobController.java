package com.project.undefined.job.controller;

import com.project.undefined.job.dto.request.CreateJobRequest;
import com.project.undefined.job.dto.response.JobResponse;
import com.project.undefined.job.dto.response.StageResponse;
import com.project.undefined.job.service.JobService;
import com.project.undefined.job.service.StageService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${apiPrefix}/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final StageService stageService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateJobRequest request) {
        final JobResponse jobResponse = jobService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/jobs/" + jobResponse.getId()))
            .build();
    }

    @GetMapping
    public List<JobResponse> getAll() {
        return jobService.getAll();
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable final Long id)  {
        return jobService.get(id);
    }

    @GetMapping("/{id}/stages")
    public List<StageResponse> getRelatedStages(@PathVariable final Long id) {
        return stageService.getJobStages(id);
    }
}
