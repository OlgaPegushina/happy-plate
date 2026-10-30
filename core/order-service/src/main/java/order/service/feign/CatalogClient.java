package order.service.feign;

import interaction.api.dto.MenuItemIdsRequest;
import interaction.api.dto.MenuItemSnapshotDto;
import order.service.config.FeignInternalTokenConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalog-service", configuration = FeignInternalTokenConfig.class)
public interface CatalogClient {

    @PostMapping("/internal/menu-items/by-ids")
    List<MenuItemSnapshotDto> getByIds(@RequestBody MenuItemIdsRequest req);
}