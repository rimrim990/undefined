package com.project.undefined.job.service;

import com.project.undefined.common.exception.JobException;
import com.project.undefined.job.dto.request.CreateStageRequest;
import com.project.undefined.job.dto.response.StageResponse;
import com.project.undefined.job.entity.Job;
import com.project.undefined.job.entity.Stage;
import com.project.undefined.job.repository.JobRepository;
import com.project.undefined.job.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StageService {

    private final JobRepository jobRepository;
    private final StageRepository stageRepository;


    public StageResponse create(final CreateStageRequest request) {
        final Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new JobException("일치하는 Job이 존재하지 않습니다"));

        final Stage stage = Stage.of(request.getName(), job);
        stageRepository.save(stage);
        return StageResponse.from(stage);
    }
}
