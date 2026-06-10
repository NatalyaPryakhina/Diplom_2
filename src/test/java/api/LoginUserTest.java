package api;

import api.client.UserClient;
import api.model.User;
import api.model.UserCredentials;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class LoginUserTest {
    private UserClient userClient;
    private String accessToken;
    private User createdUser;

    @Before
    public void setUp() {
        userClient = new UserClient();

        String uniqueId = String.valueOf(System.currentTimeMillis());
        createdUser = User.builder()
                .email("login_user_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response createResponse = userClient.createUser(createdUser);
        createResponse.then().statusCode(SC_OK); // Гарантирует создание юзера перед тестом
        accessToken = createResponse.path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Успешный вход в систему с валидными данными")
    public void testLoginExistingUserSuccess() {
        UserCredentials credentials = UserCredentials.from(createdUser);
        Response loginResponse = userClient.loginUser(credentials);

        loginResponse.then().statusCode(SC_OK)
                .body("success", is(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверным логином (email)")
    @Description("Проверка возврата ошибки при попытке входа с незарегистрированным email")
    public void testLoginWithInvalidEmailFail() {
        UserCredentials badCredentials = UserCredentials.builder()
                .email("not_exists_user_123@yandex.ru")
                .password(createdUser.getPassword())
                .build();

        Response response = userClient.loginUser(badCredentials);

        response.then().statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Проверка возврата ошибки при попытке входа с невалидным паролем существующего пользователя")
    public void testLoginWithInvalidPasswordFail() {
        UserCredentials badCredentials = UserCredentials.builder()
                .email(createdUser.getEmail())
                .password("wrong_password_999")
                .build();

        Response response = userClient.loginUser(badCredentials);

        response.then().statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
