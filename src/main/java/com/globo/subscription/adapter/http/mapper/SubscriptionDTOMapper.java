package com.globo.subscription.adapter.http.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRequest;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;

@Mapper(componentModel = "spring")
public interface SubscriptionDTOMapper {

    @Mapping(source = "usuarioId", target = "user.id")
    @Mapping(source = "plano", target = "plan")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Subscription toDomain(SubscriptionRequest request);

    @Mapping(source = "user.id", target = "usuarioId")
    @Mapping(source = "plan", target = "plano")
    @Mapping(source = "startDate", target = "dataInicio")
    @Mapping(source = "expirationDate", target = "dataExpiracao")
    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    SubscriptionResponse toResponse(Subscription subscription);

    @Named("mapStatus")
    default String mapStatus(SubscriptionStatus status) {
        if (status == null) return null;
        return switch (status) {
            case ACTIVE -> "ATIVA";
            case INACTIVE -> "INATIVA";
            case CANCELED -> "CANCELADA";
        };
    }
}
