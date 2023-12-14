package com.project.undefined.acceptance.job;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.project.undefined.acceptance.AcceptanceTest;
import com.project.undefined.acceptance.utils.RestAssuredUtils;
import com.project.undefined.common.dto.response.ErrorResponse;
import com.project.undefined.company.entity.Company;
import com.project.undefined.company.repository.CompanyRepository;
import com.project.undefined.job.dto.request.CreateJobRequest;
import com.project.undefined.job.dto.response.JobResponse;
import com.project.undefined.job.entity.Job;
import com.project.undefined.job.repository.JobRepository;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@DisplayName("Job API 인수 테스트")
public class JobTest extends AcceptanceTest {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Test
    @DisplayName("등록된 모든 Job 목록을 조회한다.")
    void getAll_ok() {
        // given
        final List<Long> jobIds = getJobIds(Pageable.unpaged());

        // when
        final ExtractableResponse<Response> response = given().log().all()
            .when()
            .get("/jobs/")
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

    @Test
    @DisplayName("id와 일치한 Job의 세부 정보를 조회한다.")
    void get_invalidJobId_badRequest() {
        // given
        final Long jobId = getJobIds(Pageable.ofSize(1)).get(0);

        // when
        final ExtractableResponse<Response> response = given().log().all()
            .when()
            .get("/jobs/" + jobId)
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        final JobResponse job = RestAssuredUtils.extract(response, JobResponse.class);
        assertThat(job).hasFieldOrPropertyWithValue("id", jobId);
    }

    @Test
    @DisplayName("id와 일치한 Job이 없으면 400 상태를 반환한다.")
    void get_ok() {
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
        assertThat(error.getMessage()).isEqualTo("일치하는 Job이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Job을 생성한다.")
    void create_created() {
        // given
        final Long companyId = getCompanyIds(Pageable.ofSize(1)).get(0);
        final CreateJobRequest request = new CreateJobRequest(companyId, "backend");

        // when
        final ExtractableResponse<Response> response = given().log().all()
            .when()
            .body(request)
            .contentType(ContentType.JSON)
            .post("/jobs/")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        final String location = response.header("Location");
        final String jobId = location.split("/jobs/")[1];
        assertThat(jobId).isNotBlank();
    }

    @Test
    @DisplayName("companyId가 유효하지 않으면 400 상태를 반환한다")
    void create_invalidCompanyId_badRequest() {
        // given
        final Long notExsitCompanyId = 1_000_000L;
        final CreateJobRequest request = new CreateJobRequest(notExsitCompanyId, "backend");

        // when
        final ExtractableResponse<Response> response = given().log().all()
            .when()
            .body(request)
            .contentType(ContentType.JSON)
            .post("/jobs/")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("일치하는 Company가 존재하지 않습니다.");
    }

    private List<Long> getJobIds(final Pageable pageable) {
        final Page<Job> jobs = jobRepository.findAll(pageable);
        return jobs.getContent()
            .stream()
            .map(Job::getId)
            .toList();
    }

    private List<Long> getCompanyIds(final Pageable pageable) {
        final Page<Company> companies = companyRepository.findAll(pageable);
        return companies.getContent()
            .stream()
            .map(Company::getId)
            .toList();
    }
}
