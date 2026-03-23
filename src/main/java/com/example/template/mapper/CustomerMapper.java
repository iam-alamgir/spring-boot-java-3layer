package com.example.template.mapper;

import com.example.template.dto.CustomerResponse;
import com.example.template.repository.entity.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toResponse(CustomerEntity entity);
}
