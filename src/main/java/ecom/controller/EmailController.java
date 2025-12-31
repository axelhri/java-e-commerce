package ecom.controller;

import ecom.dto.ApiRestResponse;
import ecom.interfaces.EmailServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "Email", description = "Endpoints for email related operations")
public class EmailController {
  private final EmailServiceInterface emailService;

  @Operation(
      summary = "Confirm email address",
      description = "Confirms user's email address using a token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Email confirmed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired token",
            content = @Content)
      })
  @GetMapping("/confirm")
  public ResponseEntity<ApiRestResponse<String>> confirmEmail(
      @Parameter(description = "Confirmation token received via email", required = true)
          @RequestParam
          String token) {
    emailService.confirmEmail(token);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Email confirmed successfully",
                "Email confirmed successfully"));
  }
}
