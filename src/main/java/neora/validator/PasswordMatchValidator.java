package neora.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import neora.dto.ChangePassword;
import neora.interfaces.PasswordMatch;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, ChangePassword> {
  @Override
  public boolean isValid(ChangePassword dto, ConstraintValidatorContext context) {
    if (dto == null) return true;

    boolean matches = dto.newPassword().equals(dto.confirmPassword());
    if (!matches) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Passwords do not match")
          .addPropertyNode("confirmPassword")
          .addConstraintViolation();
    }
    return matches;
  }
}
