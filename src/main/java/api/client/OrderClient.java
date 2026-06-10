package api.client;

import api.model.OrderRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;

public class OrderClient extends BaseSpecClient {
    private static final String ORDER_PATH = "api/orders";
    private static final String INGREDIENTS_PATH = "api/ingredients";
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
        return given().spec(getBaseSpec()).get(INGREDIENTS_PATH);
    }

    @Step("Получить ID первого доступного ингредиента из списка")
    public String getFirstIngredientId() {
        Response response = getIngredients();
        if (response.statusCode() == SC_OK) {

            String ingredientId = response.path("data[0]._id");
            if (ingredientId != null && !ingredientId.isEmpty()) {
                return ingredientId;
            }
        }

        return "61c0c5b71d1f82001bdae51d";
    }
}
