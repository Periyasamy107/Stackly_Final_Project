package com.example.bank.file.mapper;

import com.example.bank.file.dto.response.FileMetadataResponse;
import com.example.bank.file.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {

    @Mapping(
            target = "customerId",
            source = "customer.id"
    )
    @Mapping(
            target = "uploadedByUserId",
            source = "uploadedByUser.id"
    )
    FileMetadataResponse toResponse(FileMetadata fileMetadata);
}