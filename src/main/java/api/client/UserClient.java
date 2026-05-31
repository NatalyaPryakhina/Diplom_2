package api.client;

import api.model.User;
import api.model.UserCredentials;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class UserClient extends BaseSpecClient {
    private static final String REGISTER_PATH = "api/auth/register";
    private static final String LOGIN_PATH = "api/auth/login";
    private static final String USER_PATH = "api/auth/user";
    private static final String AUTH_HEADER = "Authorization";

    @SuppressWarnings("UnusedReturnValue")
    public Response createUser(User user) {
        return given().spec(getBaseSpec()).body(user).post(REGISTER_PATH);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Response loginUser(UserCredentials credentials) {
        return given().spec(getBaseSpec()).body(credentials).post(LOGIN_PATH);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Response deleteUser(String accessToken) {
        return given().spec(getBaseSpec())
                .header(AUTH_HEADER, accessToken)
                .delete(USER_PATH);
    }
}

