package org.example.mappers;

import org.example.domain.model.Client;
import org.example.domain.model.SalesManager;
import org.example.domain.model.SystemAdmin;
import org.example.domain.model.User;
import org.example.domain.model.WarehouseAdmin;
import org.example.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "userType", source = "user", qualifiedByName = "mapUserType")
    UserResponse toResponse(User user);

    @Named("mapUserType")
    default String mapUserType(User user) {
        if (user instanceof Client) {
            return "CLIENT";
        }
        if (user instanceof SalesManager) {
            return "SALES_MANAGER";
        }
        if (user instanceof WarehouseAdmin) {
            return "WAREHOUSE_ADMIN";
        }
        if (user instanceof SystemAdmin) {
            return "SYSTEM_ADMIN";
        }
        return "UNKNOWN";
    }
}
