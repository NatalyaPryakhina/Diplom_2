package api.client;

import api.model.OrderRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class OrderClient extends BaseSpecClient {
    private static final String ORDER_PATH = "api/orders";
    private static final String AUTH_HEADER = "Authorization";

    @Step("Создать заказ")

    public Response createOrder(OrderRequest orderRequest, String accessToken) {
        var request = given().spec(getBaseSpec());
        if (accessToken != null && !accessToken.isEmpty()) {
            request.header(AUTH_HEADER, accessToken);
        }
        return request.body(orderRequest).post(ORDER_PATH);
    }


    @Step("Получить список доступных ингредиентов")
    public Response getIngredients() {
        return given().spec(getBaseSpec()).get("api/ingredients");
    }

}

