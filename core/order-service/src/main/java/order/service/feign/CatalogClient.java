package order.service.feign;

import order.service.dto.feign.MenuItemIdsRequest;
import order.service.dto.feign.MenuItemSnapshotDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalogClient", url = "${app.catalog.base-url}")
public interface CatalogClient {

    @PostMapping("/internal/menu-items/by-ids")
    List<MenuItemSnapshotDto> getMenuItemsByIds(@RequestBody MenuItemIdsRequest req);
}