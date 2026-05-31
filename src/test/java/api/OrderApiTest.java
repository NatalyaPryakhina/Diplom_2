package api;

import api.client.OrderClient;
import api.client.UserClient;
import api.model.OrderRequest;
import api.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderApiTest {
    private OrderClient orderClient;
    private UserClient userClient;
    private String accessToken;
    private List<String> validIngredients;

    @Before
    public void setUp() {
        orderClient = new OrderClient();
        userClient = new UserClient();
        validIngredients = new ArrayList<>();

        Response ingredientsResponse = given()
                .spec(orderClient.getBaseSpec())
                .get("api/ingredients");

        if (ingredientsResponse.statusCode() == 200) {
            String dynamicHash = ingredientsResponse.path("data[0]._id");
            if (dynamicHash != null && !dynamicHash.isEmpty()) {
                validIngredients.add(dynamicHash);
            } else {
                validIngredients.add("60d3b41abdacab0026a733c6");
            }
        } else {
            validIngredients.add("60d3b41abdacab0026a733c6");
        }
    }

    @After
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Успешное создание заказа авторизованным в системе пользователем с ингредиентами")
    public void testCreateOrderWithAuthSuccess() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("order_auth_" + uniqueId + "@yandex.ru", "pass123", "Natalya");
        Response registerResponse = userClient.createUser(user);

        registerResponse.then().statusCode(200);
        accessToken = registerResponse.path("accessToken");

        OrderRequest orderRequest = new OrderRequest(validIngredients);
        Response response = orderClient.createOrder(orderRequest, accessToken);

        response.then().statusCode(200)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка поведения системы при создании заказа неавторизованным пользователем")
    public void testCreateOrderWithoutAuthFail() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);
        Response response = orderClient.createOrder(orderRequest, "");


        response.then().statusCode(200)
                .body("success", is(true));
    }



    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Возврат ошибки 400 Bad Request при передаче пустого списка ингредиентов")
    public void testCreateOrderWithoutIngredientsFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("order_empty_" + uniqueId + "@yandex.ru", "pass123", "Natalya");
        Response registerResponse = userClient.createUser(user);

        registerResponse.then().statusCode(200);
        accessToken = registerResponse.path("accessToken");

        OrderRequest orderRequest = new OrderRequest(new ArrayList<>());
        Response response = orderClient.createOrder(orderRequest, accessToken);

        response.then().statusCode(400)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Возврат ошибки 500 Internal Server Error при использовании невалидного хэша")
    public void testCreateOrderWithInvalidHashFail() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        User user = new User("order_hash_" + uniqueId + "@yandex.ru", "pass123", "Natalya");
        Response registerResponse = userClient.createUser(user);

        registerResponse.then().statusCode(200);
        accessToken = registerResponse.path("accessToken");

        List<String> invalidIngredients = new ArrayList<>();
        invalidIngredients.add("invalid_hash_value_12345");

        OrderRequest orderRequest = new OrderRequest(invalidIngredients);
        Response response = orderClient.createOrder(orderRequest, accessToken);

        response.then().statusCode(500);
    }
}
