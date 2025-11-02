package io.baxter.authentication.tests.api.controllers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@ExtendWith(OutputCaptureExtension.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebFluxTest
public @interface ControllerTest {
    @AliasFor(annotation = WebFluxTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}

