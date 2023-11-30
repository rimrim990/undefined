package com.project.undefined;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.ActiveProfiles;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@ActiveProfiles("test")
public @interface TestContext {

}
