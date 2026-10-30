package order.service.controller;

import interaction.api.dto.OrderStatusUpdateDto;
import interaction.api.enums.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import order.service.dto.order.OrderAdminDto;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderDto;
import order.service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders/{id}")
    public OrderDto getById(@PathVariable UUID id) {
        return orderService.getById(id);
    }

    @GetMapping("/orders")
    public Page<OrderAdminDto> getAll(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.getOrdersForAdmin(status, pageable);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody @Valid OrderCreateDto dto) {
        return orderService.createDraft(dto);
    }

    @PutMapping("/orders/{id}/status")
    public OrderDto updateStatus(@PathVariable UUID id,
                                 @RequestBody @Valid OrderStatusUpdateDto dto) {
        return orderService.adminUpdateStatus(id, dto.status());
    }
}