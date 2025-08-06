package nl.markpost.demo.authentication.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.handler.BaseCustomExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CustomExceptionHandler extends BaseCustomExceptionHandler {

}
