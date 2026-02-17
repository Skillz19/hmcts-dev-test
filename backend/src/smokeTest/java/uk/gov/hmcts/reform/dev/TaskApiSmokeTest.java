package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiSmokeTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void shouldExposeHealthProbes() {
        Response liveness = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/actuator/health/liveness")
                .then()
                .extract().response();

        Assertions.assertEquals(200, liveness.statusCode());
        Assertions.assertEquals("UP", liveness.jsonPath().getString("status"));

        Response readiness = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/actuator/health/readiness")
                .then()
                .extract().response();

        Assertions.assertEquals(200, readiness.statusCode());
        Assertions.assertEquals("UP", readiness.jsonPath().getString("status"));
    }

    @Test
    void shouldReturnPagedTasksResponse() {
        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("sortBy", "ID")
                .queryParam("direction", "ASC")
                .when()
                .get("/tasks")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNotNull(response.jsonPath().getList("items"));
        Assertions.assertEquals(0, response.jsonPath().getInt("page"));
        Assertions.assertEquals(5, response.jsonPath().getInt("size"));
    }

    @Test
    void shouldCreateReadAndDeleteTask() {
        String uniqueTitle = "smoke-" + UUID.randomUUID();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "title": "%s",
                          "description": "Smoke test task",
                          "status": "PENDING",
                          "dueDate": "2030-01-01T10:00:00"
                        }
                        """.formatted(uniqueTitle))
                .when()
                .post("/tasks")
                .then()
                .extract().response();

        Assertions.assertEquals(201, createResponse.statusCode());
        Long taskId = createResponse.jsonPath().getLong("id");
        Assertions.assertNotNull(taskId);

        Response fetchResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/tasks/{id}", taskId)
                .then()
                .extract().response();

        Assertions.assertEquals(200, fetchResponse.statusCode());
        Assertions.assertEquals(uniqueTitle, fetchResponse.jsonPath().getString("title"));

        Response deleteResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/tasks/{id}", taskId)
                .then()
                .extract().response();

        Assertions.assertEquals(204, deleteResponse.statusCode());

        Response fetchAfterDelete = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/tasks/{id}", taskId)
                .then()
                .extract().response();

        Assertions.assertEquals(404, fetchAfterDelete.statusCode());
    }
}
