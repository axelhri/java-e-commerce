package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.interfaces.EmailServiceInterface;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailController {
  private final EmailServiceInterface emailService;

  @GetMapping("/confirm")
  public ResponseEntity<ApiResponse<String>> confirmEmail(@RequestParam String token) {
    emailService.confirmEmail(token);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Email confirmed successfully",
                "Email confirmed successfully"));
  }
}
