package catalog.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "menu_items", schema = "catalog",
        indexes = {
                @Index(name = "ix_menu_items_restaurant_id", columnList = "restaurant_id"),
                @Index(name = "ix_menu_items_category_id", columnList = "category_id"),
                @Index(name = "ix_menu_items_restaurant_available", columnList = "restaurant_id, available")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Setter
    @Column(nullable = false, length = 200)
    private String name;

    @Setter
    @Column(length = 2000)
    private String description;

    @Setter
    @Embedded
    @Valid
    @NotNull(message = "Total must not be null")
    private Money price;

    @Setter
    @Column(nullable = false)
    private boolean available = true;

    @Setter
    @Column(name = "sort_order", nullable = false)
    @Min(0)
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public MenuItem(
            Restaurant restaurant,
            MenuCategory category,
            String name,
            String description,
            Money price
    ) {
        this.restaurant = restaurant;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        MenuItem menuItem = (MenuItem) o;
        return id != null && Objects.equals(id, menuItem.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}