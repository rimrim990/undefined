package com.project.undefined.acceptance.job;

import static com.project.undefined.acceptance.utils.ApiDocumentUtils.getDocumentRequest;
import static com.project.undefined.acceptance.utils.ApiDocumentUtils.getDocumentResponse;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

import com.project.undefined.acceptance.AcceptanceTest;
import com.project.undefined.acceptance.utils.DataUtils;
import com.project.undefined.acceptance.utils.RestAssuredUtils;
import com.project.undefined.common.dto.response.ErrorResponse;
import com.project.undefined.common.exception.ErrorCode;
import com.project.undefined.company.entity.Company;
import com.project.undefined.company.repository.CompanyRepository;
import com.project.undefined.job.dto.request.CreateJobRequest;
import com.project.undefined.job.dto.response.JobResponse;
import com.project.undefined.job.dto.response.StageResponse;
import com.project.undefined.job.entity.Job;
import com.project.undefined.job.entity.Stage;
import com.project.undefined.job.repository.JobRepository;
import com.project.undefined.job.repository.StageRepository;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

@DisplayName("Job API 인수 테스트")
public class JobTest extends AcceptanceTest {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Nested
    class create {

        @Test
        @DisplayName("Job을 생성한다.")
        void create_created() {
            // given
            final Long companyId = DataUtils.findAnyId(companyRepository, Company::getId);
            final CreateJobRequest request = new CreateJobRequest(companyId, "backend");

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("job/create",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("companyId").type(JsonFieldType.NUMBER).description("company 아이디"),
                        fieldWithPath("position").type(JsonFieldType.STRING).description("포지션")
                    )
                ))
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/jobs")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            final Long jobId = RestAssuredUtils.parseLocationId(response, "/jobs/");
            assertThat(jobRepository.existsById(jobId)).isTrue();
        }

        @Test
        @DisplayName("companyId가 유효하지 않으면 400 상태를 반환한다")
        void create_invalidCompanyId_badRequest() {
            // given
            final Long notExistCompanyId = 1_000_000L;
            final CreateJobRequest request = new CreateJobRequest(notExistCompanyId, "backend");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/jobs")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo(ErrorCode.NON_MATCH_COMPANY.getMessage());
        }

        @Test
        @DisplayName("companyId가 null이면 400 상태를 반환한다")
        void create_nullCompanyId_badRequest() {
            // given
            final CreateJobRequest request = new CreateJobRequest(null, "backend");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/jobs")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo("companyId 은(는) 필수 입력 값입니다.");
        }

        @Test
        @DisplayName("position이 공백이면 400 상태를 반환한다")
        void create_blankPosition_badRequest() {
            // given
            final CreateJobRequest request = new CreateJobRequest(1L, "");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/jobs")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo("position 은(는) 필수 입력 값이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.");
        }
    }

    @Nested
    class getAll {

        @Test
        @DisplayName("등록된 모든 Job 목록을 조회한다.")
        void getAll_ok() {
            // given
            final List<Long> jobIds = DataUtils.findAllIds(jobRepository, Job::getId);

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("job/getAll",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        fieldWithPath("[]").type(JsonFieldType.ARRAY).description("job 리스트"),
                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("아이디"),
                        fieldWithPath("[].companyId").type(JsonFieldType.NUMBER).description("company 아이디"),
                        fieldWithPath("[].position").type(JsonFieldType.STRING).description("이름")
                    )
                ))
                .when()
                .get("/jobs")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            final List<Long> extractedIds = RestAssuredUtils.extractAsList(response, JobResponse.class)
                .stream()
                .map(JobResponse::getId)
                .toList();
            assertThat(extractedIds).containsExactlyElementsOf(jobIds);
        }
    }

    @Nested
    class get {

        @Test
        @DisplayName("id와 일치한 Job의 세부 정보를 조회한다.")
        void get_ok() {
            // given
            final Long jobId = DataUtils.findAnyId(jobRepository, Job::getId);

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("job/get",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("아이디")
                    ),
                    responseFields(
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("아이디"),
                        fieldWithPath("companyId").type(JsonFieldType.NUMBER).description("company 아이디"),
                        fieldWithPath("position").type(JsonFieldType.STRING).description("포지션")
                    )
                ))
                .when()
                .get("/jobs/{id}", + jobId)
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            final JobResponse job = RestAssuredUtils.extract(response, JobResponse.class);
            assertThat(job).hasFieldOrPropertyWithValue("id", jobId);
        }

        @Test
        @DisplayName("id와 일치한 Job이 없으면 400 상태를 반환한다.")
        void get_invalidJobId_badRequest() {
            // given
            final long notExistJobId = 1_000_000;

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .get("/jobs/" + notExistJobId)
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo(ErrorCode.NON_MATCH_JOB.getMessage());
        }
    }

    @Nested
    class getRelatedStages {

        @Test
        @DisplayName("id와 연관된 Stage 목록을 조회한다")
        void getRelatedStages_ok() {
            // given
            final Job job = DataUtils.findAny(jobRepository);
            final List<Long> relatedStageIds = findRelatedStageIds(job);

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("job/getRelatedStages",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        fieldWithPath("[]").type(JsonFieldType.ARRAY).description("stage 리스트"),
                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("아이디"),
                        fieldWithPath("[].jobId").type(JsonFieldType.NUMBER).description("job 아이디"),
                        fieldWithPath("[].name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("[].state").type(JsonFieldType.STRING).description("상태")
                    )
                ))
                .when()
                .get("/jobs/{id}/stages", job.getId())
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

            final List<Long> resultStageIds = RestAssuredUtils.extractAsList(response, StageResponse.class)
                .stream()
                .map(StageResponse::getId)
                .toList();
            assertThat(resultStageIds).containsExactlyElementsOf(relatedStageIds);
        }

        @Test
        @DisplayName("id와 일치한 Job이 없으면 400 상태를 반환한다.")
        void getRelatedStages_invalidJobId_badRequest() {
            // given
            final long notExistJobId = 1_000_000;

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .get("/jobs/" + notExistJobId + "/stages")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo(ErrorCode.NON_MATCH_JOB.getMessage());
        }
    }

    private List<Long> findRelatedStageIds(final Job job) {
        return stageRepository.findByJob(job)
            .stream()
            .map(Stage::getId)
            .toList();
    }
}
