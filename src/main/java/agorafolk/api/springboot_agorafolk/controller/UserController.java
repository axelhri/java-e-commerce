package agorafolk.api.springboot_agorafolk.controller;

import agorafolk.api.springboot_agorafolk.dto.ApiResponse;
import agorafolk.api.springboot_agorafolk.dto.ChangePassword;
import agorafolk.api.springboot_agorafolk.interfaces.UserServiceInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
  public ResponseEntity<ApiResponse> changePassword(
      @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePassword dto) {
    userService.changePassword(userDetails.getUsername(), dto);
    return ResponseEntity.ok(new ApiResponse(true, "Your password has been changed successfully"));
  }
}
