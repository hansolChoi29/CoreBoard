package common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/hello")
    public void hello(){
        throw new RuntimeException("예외처리 메시지를 입력해 주세요.");
    }

    @ExceptionHandler(RuntimeException.class)
    public Object error(RuntimeException e){
        return e.getMessage();
    }
}
