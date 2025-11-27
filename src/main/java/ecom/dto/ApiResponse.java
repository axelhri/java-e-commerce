package ecom.dto;

import java.time.Instant;

public record ApiResponse<T>(Instant timestamp, int status, String message, T data) {}
