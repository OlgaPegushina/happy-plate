package order.service.dto.feign;

import java.util.List;
import java.util.UUID;

public record MenuItemIdsRequest(List<UUID> ids) {}