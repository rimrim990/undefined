package com.project.undefined.job.dto.response;

import com.project.undefined.job.entity.Stage;
import com.project.undefined.job.entity.Stage.State;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StageResponse {

    private Long id;
    private String name;
    private State state;

    public static StageResponse from(final Stage stage) {
        return new StageResponse(stage.getId(), stage.getName(), stage.getState());
    }
}
