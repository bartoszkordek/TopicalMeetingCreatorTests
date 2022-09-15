package api;

import api.model.request.MeetingProposition;
import api.model.request.UserRegistration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class TopicalMeetingCreatorTest {


    @BeforeAll
    public static void setUp() throws IOException {
        InputStream input = TopicalMeetingCreatorTest.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        properties.load(input);
        RestAssured.baseURI = properties.getProperty("base.uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
    }

    @BeforeEach
    public void cleanUpApiData() {
        given().contentType("application/json")
                .when().post("/maintenance:reset")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Happy Path Test - meeting was created")
    public void meetingCreated_whenValidRequests() {
        MeetingProposition meetingPropositionDetails = new MeetingProposition(
                "Cultural impact on national economic growth",
                "This lecture examines the cultural hypothesis regarding national economic prosperity.",
                1,
                2,
                "ECONOMY"
        );

        String meetingPropositionId = createMeetingProposition(meetingPropositionDetails);

        String userId =  getUserIdWhenRegistered(registerUser(new UserRegistration(
                "mark@example.pl",
                "2000-09-08",
                "mark123",
                List.of("ECONOMY"))));

        sign(meetingPropositionId, userId);

        String meetingId = getMeetingId(meetingPropositionId);

        getAndValidateMeetingDetails(meetingId, meetingPropositionId, meetingPropositionDetails, userId);
    }


    @Test
    @DisplayName("Negative Test - user was not created when younger than 16 years old")
    public void userNotCreated_whenUserYoungerThan16() {
        Response response = registerUser(new UserRegistration(
                "mark@example.pl",
                "2020-01-01",
                "mark123",
                List.of("ECONOMY")));
        validateNegativeRegisterUserResponse(
                response, 422, "User is too young should be at least 16 years old.");
    }


    private String createMeetingProposition(MeetingProposition propositionData) {
        Response response = given().contentType(ContentType.JSON).body(
                        Map.of("title", propositionData.getTitle(),
                                "description", propositionData.getDescription(),
                                "minimumNumberOfParticipants", propositionData.getMinimumNumberOfParticipants(),
                                "maximumSignedUpUsers", propositionData.getMaximumSignedUpUsers(),
                                "topic", propositionData.getTopic()))
                .when().post("/meeting-propositions");

        JsonPath jsonPathEvaluator = response.jsonPath();
        return jsonPathEvaluator.getString("id");
    }

    private Response registerUser(UserRegistration userRegistrationData){
        return given().contentType(ContentType.JSON).body(
                        Map.of("email", userRegistrationData.getEmail(),
                                "birthDate", userRegistrationData.getBirthDate(),
                                "login", userRegistrationData.getLogin(),
                                "topics", userRegistrationData.getTopics()))
                .when().post("/users");
    }

    private String getUserIdWhenRegistered(Response response){
        JsonPath jsonPathEvaluator = response.jsonPath();
        if(response.getStatusCode() == 201)
            return jsonPathEvaluator.getString("id");
        return null;
    }

    private void validateNegativeRegisterUserResponse(Response response, int expectedStatus, String expectedErrorMessage){
        JsonPath jsonPath = response.jsonPath();
        int actualStatusFromBody =  jsonPath.getInt("status");
        String actualErrorMessage = jsonPath.getString("message");
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedStatus, response.statusCode()),
                () -> Assertions.assertEquals(expectedStatus, actualStatusFromBody),
                () -> Assertions.assertEquals(expectedErrorMessage, actualErrorMessage)
        );
    }


    private void sign(String meetingPropositionId, String userId){
        given().contentType(ContentType.JSON).pathParams(
                        Map.of("MEETING_PROPOSITION_ID",
                                meetingPropositionId, "USER_ID", userId))
                .when().patch("/meeting-propositions/{MEETING_PROPOSITION_ID}/sign-up-user/{USER_ID}")
                .then().statusCode(200);
    }

    private String getMeetingId(String meetingPropositionId){
        Response response = given().contentType(ContentType.JSON).pathParam("MEETING_PROPOSITION_ID", meetingPropositionId)
                .when().get("/meeting-propositions/{MEETING_PROPOSITION_ID}");
        JsonPath jsonPathEvaluator = response.jsonPath();
        return jsonPathEvaluator.getString("meetingId");
    }

    private void getAndValidateMeetingDetails(
            String meetingId,
            String meetingPropositionId,
            MeetingProposition meetingPropositionDetails,
            String userId)
    {
        given().contentType(ContentType.JSON).pathParam("MEETING_ID", meetingId)
                .when().get("/meetings/{MEETING_ID}")
                .then().assertThat().statusCode(200)
                .body("title", equalTo(meetingPropositionDetails.getTitle()))
                .body("description", equalTo(meetingPropositionDetails.getDescription()))
                .body("topic", equalTo(meetingPropositionDetails.getTopic()))
                .body("maximumSignedUpUsers", equalTo(meetingPropositionDetails.getMaximumSignedUpUsers()))
                .body("propositionId", equalTo(meetingPropositionId))
                .body("usersIds", contains(userId));
    }

}