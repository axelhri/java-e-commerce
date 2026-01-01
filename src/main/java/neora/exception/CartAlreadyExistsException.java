package neora.exception;

public class CartAlreadyExistsException extends RuntimeException {
  public CartAlreadyExistsException(String message) {
    super(message);
  }
}
