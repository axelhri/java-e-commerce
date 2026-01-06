package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.ApiRestResponse;
import neora.dto.ChangePassword;
import neora.interfaces.UserServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "Endpoints for user profile management")
@Slf4j
public class UserController {
  private final UserServiceInterface userService;

  @Operation(
      summary = "Change password",
      description = "Allows an authenticated user to change their password.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input (e.g. passwords do not match, weak password)",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated or wrong current password",
            content = @Content)
      })
  @PatchMapping("/password")
  public ResponseEntity<ApiRestResponse<ChangePassword>> changePassword(
      @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePassword dto) {
    log.info("Received request to change password for user: {}", userDetails.getUsername());
    userService.changePassword(userDetails.getUsername(), dto);
    log.info("Password changed successfully for user: {}", userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Your password has been changed successfully",
                dto));
  }
}
