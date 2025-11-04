package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.validator.PasswordMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = PasswordMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
  String message() default "Passwords do not match";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
