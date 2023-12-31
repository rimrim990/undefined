package com.project.undefined.acceptance.company;

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
import com.project.undefined.company.dto.request.CreateCompanyRequest;
import com.project.undefined.company.dto.response.CompanyResponse;
import com.project.undefined.company.entity.Company;
import com.project.undefined.company.repository.CompanyRepository;
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

@DisplayName("Company API 인수 테스트")
public class CompanyTest extends AcceptanceTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Nested
    class getAll {

        @Test
        @DisplayName("등록된 모든 Company 목록을 조회한다")
        void getAll_ok() {
            // given
            final List<Long> companyIds = DataUtils.findAllIds(companyRepository, Company::getId);

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("company/getAll",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        fieldWithPath("[]").type(JsonFieldType.ARRAY).description("company 리스트"),
                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("아이디"),
                        fieldWithPath("[].name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("[].series").type(JsonFieldType.STRING).description("투자 단계"),
                        fieldWithPath("[].region").type(JsonFieldType.STRING).description("지역")
                    )
                ))
                .when()
                .get("/companies")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            final List<Long> extractedIds = RestAssuredUtils.extractAsList(response, Company.class)
                .stream()
                .map(Company::getId)
                .toList();
            assertThat(extractedIds).containsExactlyElementsOf(companyIds);
        }
    }

    @Nested
    class get {

        @Test
        @DisplayName("유효하지 않은 id로 Company를 조회하면 400 상태를 반환한다.")
        void get_invalidId_badRequest() {
            // given
            long invalidCompanyId = 10_000_000L;

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .get("/companies/" + invalidCompanyId)
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo(ErrorCode.NON_MATCH_COMPANY.getMessage());
        }

        @Test
        @DisplayName("id로 Company 상세 정보를 조회한다.")
        void get_ok() {
            // given
            Long companyId = DataUtils.findAnyId(companyRepository, Company::getId);

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("company/get",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    pathParameters(
                        parameterWithName("id").description("아이디")
                    ),
                    responseFields(
                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("아이디"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("series").type(JsonFieldType.STRING).description("투자 단계"),
                        fieldWithPath("region").type(JsonFieldType.STRING).description("지역")
                    )
                ))
                .when()
                .get("/companies/{id}", companyId)
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            final CompanyResponse company = RestAssuredUtils.extract(response, CompanyResponse.class);
            assertThat(company.getId()).isEqualTo(companyId);
        }
    }

    @Nested
    class create {

        @Test
        @DisplayName("Company 생성에 성공하면 201 상태를 반환한다.")
        void create_created() {
            // given
            final CreateCompanyRequest request = new CreateCompanyRequest("test", "B", "SEOUL");

            // when
            final ExtractableResponse<Response> response = given(spec).log().all()
                .filter(document("company/create",
                    getDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                        fieldWithPath("series").type(JsonFieldType.STRING).description("투자 단계"),
                        fieldWithPath("region").type(JsonFieldType.STRING).description("지역")
                    )
                ))
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/companies")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            final Long companyId = RestAssuredUtils.parseLocationId(response, "/companies/");
            assertThat(companyRepository.existsById(companyId)).isTrue();
        }

        @Test
        @DisplayName("잘못된 Series 값을 넘기면 400 상태를 반환한다")
        void create_invalidSeries_badRequest() {
            // given
            final CreateCompanyRequest request = new CreateCompanyRequest("test", "invalid", "SEOUL");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/companies")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo("series 필드에 유효한 값을 입력해야 합니다.");
        }

        @Test
        @DisplayName("잘못된 Region 값을 넘기면 400 상태를 반환한다")
        void create_invalidRegion_badRequest() {
            // given
            final CreateCompanyRequest request = new CreateCompanyRequest("test", "A", "invalid");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/companies")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo("region 필드에 유효한 값을 입력해야 합니다.");
        }

        @Test
        @DisplayName("name이 공백이면 400 상태를 반환한다")
        void create_blankName_statue400() {
            // given
            final CreateCompanyRequest request = new CreateCompanyRequest("", "A", "Seoul");

            // when
            final ExtractableResponse<Response> response = given().log().all()
                .when()
                .body(request)
                .contentType(ContentType.JSON)
                .post("/companies")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            final ErrorResponse error = RestAssuredUtils.extract(response, ErrorResponse.class);
            assertThat(error.getMessage()).isEqualTo("name 은(는) 필수 입력 값이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.");
        }
    }
}
