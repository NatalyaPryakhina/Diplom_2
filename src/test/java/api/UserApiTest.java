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
        User user = new User("user_" + uniqueId + "@yandex.ru", "pass1234", "Natalya");

        Response response = userClient.createUser(user);


        accessToken = response.path("accessToken");

        response.then().statusCode(200)
                .body("success", is(true))
                .body("user.email", equalTo(user.getEmail().toLowerCase()));
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Проверка запрета на повторную регистрацию пользователя с существующим email")
    public void testCreateDuplicateUserFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("dup_" + uniqueId + "@yandex.ru", "pass1234", "Natalya");

        Response response1 = userClient.createUser(user);
        accessToken = response1.path("accessToken");

        Response response2 = userClient.createUser(user);
        response2.then().statusCode(403)
                .body("success", is(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя при незаполненном обязательном поле")
    @Description("Проверка запрета регистрации, если поле name не заполнено")
    public void testCreateUserMissingFieldFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("missing_" + uniqueId + "@yandex.ru", "pass1234", "");

        Response response = userClient.createUser(user);
        response.then().statusCode(403)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Успешный вход в систему с валидными данными")
    public void testLoginExistingUserSuccess() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("login_" + uniqueId + "@yandex.ru", "pass1234", "Natalya");

        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");

        Response loginResponse = userClient.loginUser(UserCredentials.from(user));
        loginResponse.then().statusCode(200)
                .body("success", is(true))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверным логином и паролем")
    @Description("Проверка возврата ошибки при попытке входа с невалидным паролем")
    public void testLoginWithInvalidCredentialsFail() {
        UserCredentials badCredentials = new UserCredentials("not_exists_user_123@yandex.ru", "bad_pass");

        Response response = userClient.loginUser(badCredentials);
        response.then().statusCode(401)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
