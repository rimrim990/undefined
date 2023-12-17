package com.project.undefined.job.controller;

import com.project.undefined.common.dto.response.EmptyResponse;
import com.project.undefined.job.dto.request.CreateStageRequest;
import com.project.undefined.job.dto.request.UpdateStageRequest;
import com.project.undefined.job.dto.response.StageResponse;
import com.project.undefined.job.service.StageService;
import com.project.undefined.retrospect.dto.request.CreateRetrospectRequest;
import com.project.undefined.retrospect.dto.response.RetrospectResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${apiPrefix}/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @PostMapping("/")
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateStageRequest request) {
        final StageResponse stageResponse = stageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
            .location(URI.create("/stages/" + stageResponse.getId()))
            .build();
    }

    @PostMapping("/{id}/retrospects")
    public ResponseEntity<Void> createdRelatedRetrospect(@PathVariable final Long id,
            @Valid @RequestBody final CreateRetrospectRequest request) {
        final RetrospectResponse retrospectResponse = stageService.attachRetrospect(id, request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
            .location(URI.create("/retrospects/" + retrospectResponse.getId()))
            .build();
    }

    @GetMapping("/{id}/retrospects")
    public ResponseEntity<?> getRelatedRetrospects(@PathVariable final Long id) {
       final RetrospectResponse retrospectResponse = stageService.getRelatedRetrospect(id);
       if (Objects.isNull(retrospectResponse)) {
           return ResponseEntity.ok(new EmptyResponse());
       }
       return ResponseEntity.ok(retrospectResponse);
    }

    @GetMapping("/{id}")
    public StageResponse get(@PathVariable final Long id) {
        return stageService.get(id);
    }

    @PatchMapping("/{id}")
    public StageResponse updateState(@PathVariable final Long id,
            @Valid @RequestBody final UpdateStageRequest request) {
        return stageService.updateState(id, request);
    }
}
