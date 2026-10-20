package order.service.feign;

import interaction.api.dto.MenuItemIdsRequest;
import interaction.api.dto.MenuItemSnapshotDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @PostMapping("/internal/menu-items/by-ids")
    List<MenuItemSnapshotDto> getMenuItemsByIds(@RequestBody MenuItemIdsRequest req);
}