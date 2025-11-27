package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.ChangePassword;
import ecom.interfaces.UserServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
  private final UserServiceInterface userService;

  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<ChangePassword>> changePassword(
      @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePassword dto) {
    userService.changePassword(userDetails.getUsername(), dto);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Your password has been changed successfully",
                dto));
  }
}
