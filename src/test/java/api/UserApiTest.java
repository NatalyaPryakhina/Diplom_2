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

public class UserApiTest {
    private UserClient userClient;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
    }

    @After
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Успешная регистрация нового пользователя со всеми заполненными полями")
    public void testCreateUniqueUserSuccess() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("user_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response response = userClient.createUser(user);
        accessToken = response.path("accessToken");

        response.then().statusCode(SC_OK)
                .body("success", is(true))
                .body("user.email", equalTo(user.getEmail().toLowerCase()));
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Проверка запрета на повторную регистрацию пользователя с существующим email")
    public void testCreateDuplicateUserFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("dup_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response response1 = userClient.createUser(user);
        accessToken = response1.path("accessToken");

        Response response2 = userClient.createUser(user);
        response2.then().statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без поля email")
    @Description("Проверка запрета регистрации, если поле email не заполнено")
    public void testCreateUserMissingEmailFail() {
        User user = User.builder()
                .email("")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response response = userClient.createUser(user);
        response.then().statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без поля password")
    @Description("Проверка запрета регистрации, если поле password не заполнено")
    public void testCreateUserMissingPasswordFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("missing_pass_" + uniqueId + "@yandex.ru")
                .password("")
                .name("Natalya")
                .build();

        Response response = userClient.createUser(user);
        response.then().statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без поля name")
    @Description("Проверка запрета регистрации, если поле name не заполнено")
    public void testCreateUserMissingNameFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("missing_name_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("")
                .build();

        Response response = userClient.createUser(user);
        response.then().statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Успешный вход в систему с валидными данными")
    public void testLoginExistingUserSuccess() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("login_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");

        Response loginResponse = userClient.loginUser(UserCredentials.from(user));
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
                .password("pass1234")
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
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .email("login_bad_pass_" + uniqueId + "@yandex.ru")
                .password("pass1234")
                .name("Natalya")
                .build();

        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");

        UserCredentials badCredentials = UserCredentials.builder()
                .email(user.getEmail())
                .password("wrong_password_999")
                .build();

        Response response = userClient.loginUser(badCredentials);
        response.then().statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
