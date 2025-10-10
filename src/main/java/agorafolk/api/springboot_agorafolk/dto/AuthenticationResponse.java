package agorafolk.api.springboot_agorafolk.dto;

import java.util.UUID;

public record AuthenticationResponse (String token, UUID id) {}
