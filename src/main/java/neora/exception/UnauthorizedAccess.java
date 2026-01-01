package neora.exception;

public class UnauthorizedAccess extends RuntimeException {
  public UnauthorizedAccess(String message) {
    super(message);
  }
}
