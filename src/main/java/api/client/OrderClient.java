package api.client;

import api.model.OrderRequest;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class OrderClient extends BaseSpecClient {
    private static final String ORDER_PATH = "api/orders";

    @SuppressWarnings("UnusedReturnValue")
    public Response createOrder(OrderRequest orderRequest, String accessToken) {
        var request = given().spec(getBaseSpec());
        if (accessToken != null && !accessToken.isEmpty()) {

            request.header("Authorization", accessToken);
        }
        return request.body(orderRequest).post(ORDER_PATH);
    }
}
