package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiFunctionalTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void shouldCreateAndFetchTaskById() {
        String title = "functional-create-" + UUID.randomUUID();

        Response createResponse = createTask(title, "PENDING", "2030-06-01T10:00:00");
        Assertions.assertEquals(201, createResponse.statusCode());

        Long id = createResponse.jsonPath().getLong("id");
        Assertions.assertNotNull(id);
        Assertions.assertEquals(title, createResponse.jsonPath().getString("title"));
        Assertions.assertEquals("PENDING", createResponse.jsonPath().getString("status"));

        Response getResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/tasks/{id}", id)
                .then()
                .extract().response();

        Assertions.assertEquals(200, getResponse.statusCode());
        Assertions.assertEquals(id, getResponse.jsonPath().getLong("id"));
        Assertions.assertEquals(title, getResponse.jsonPath().getString("title"));
    }

    @Test
    void shouldReturn400ForInvalidPaginationArguments() {
        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("page", -1)
                .queryParam("size", 0)
                .when()
                .get("/tasks")
                .then()
                .extract().response();

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getString("message").contains("page must be >= 0")
                || response.jsonPath().getString("message").contains("size must be between 1 and 100"));
    }

    @Test
    void shouldRejectInvalidCompletedStateTransition() {
        String title = "functional-state-" + UUID.randomUUID();
        Response created = createTask(title, "COMPLETED", "2030-07-01T10:00:00");
        Long id = created.jsonPath().getLong("id");

        String invalidUpdateBody = """
                {
                  "title": "%s",
                  "description": "Attempt invalid state transition",
                  "status": "PENDING",
                  "dueDate": "2030-07-01T10:00:00"
                }
                """.formatted(title);

        Response patchResponse = given()
                .contentType(ContentType.JSON)
                .body(invalidUpdateBody)
                .when()
                .patch("/tasks/{id}", id)
                .then()
                .extract().response();

        Assertions.assertEquals(409, patchResponse.statusCode());
        Assertions.assertTrue(
                patchResponse.jsonPath().getString("message").contains("Cannot move task from COMPLETED"));
    }

    @Test
    void shouldSupportPagingSortingAndMetadata() {
        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("sortBy", "ID")
                .queryParam("direction", "DESC")
                .when()
                .get("/tasks")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(0, response.jsonPath().getInt("page"));
        Assertions.assertEquals(5, response.jsonPath().getInt("size"));
        Assertions.assertNotNull(response.jsonPath().getLong("totalElements"));

        List<Integer> ids = response.jsonPath().getList("items.id");
        if (ids != null && ids.size() > 1) {
            Assertions.assertTrue(ids.get(0) >= ids.get(1));
        }
    }

    private Response createTask(String title, String status, String dueDate) {
        String requestBody = """
                {
                  "title": "%s",
                  "description": "Created by functional test",
                  "status": "%s",
                  "dueDate": "%s"
                }
                """.formatted(title, status, dueDate);

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/tasks")
                .then()
                .extract().response();
    }
}
