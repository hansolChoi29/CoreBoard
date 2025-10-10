package common.exception;


import com.example.coreboard.domain.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("글로벌과 공통 포맷이 잘 되는지 확인하는 테스트")
public class TestController {
    // 실패 - 사용자 실수 공통 포맷
    @Test
    void ApiResponseAndGlobalTest() {
        ApiResponse<Object> responseFail = ApiResponse.fail("로그인 실패!");

        System.out.println("message: " + responseFail.getMessage());

        // 서버 에러 글로벌 공통 포맷
        ApiResponse<Object> responseError = ApiResponse.error("서버에러");
        System.out.println("message: " + responseError.getMessage());
    }
}
