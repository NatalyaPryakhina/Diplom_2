package api;

import api.client.UserClient;
import api.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CreateUserTest {
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
        response.then().statusCode(SC_OK);
        accessToken = response.path("accessToken");

        response.then()
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
        response1.then().statusCode(SC_OK);
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
}
