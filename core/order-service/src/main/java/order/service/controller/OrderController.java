package order.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import order.service.dto.order.OrderCreateDto;
import order.service.dto.order.OrderDto;
import order.service.dto.order.OrderStatusUpdateDto;
import order.service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody @Valid OrderCreateDto dto) {
        return orderService.createDraft(dto);
    }

    @GetMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto getById(@PathVariable("id") UUID id) {
        return orderService.getById(id);
    }

    @PutMapping("/orders/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto updateStatus(@PathVariable UUID id,
                                 @RequestBody @Valid OrderStatusUpdateDto dto) {
        return orderService.updateStatus(id, dto.status());
    }
}