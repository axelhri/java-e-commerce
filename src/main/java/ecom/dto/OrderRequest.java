package ecom.dto;

import java.util.Set;
import java.util.UUID;

public record OrderRequest(Set<UUID> productIds) {}
