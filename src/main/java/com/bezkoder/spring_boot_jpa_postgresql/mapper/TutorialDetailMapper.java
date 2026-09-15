package com.bezkoder.spring_boot_jpa_postgresql.mapper;

import java.util.List;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.bezkoder.spring_boot_jpa_postgresql.dto.TutorialDetailDto;
import com.bezkoder.spring_boot_jpa_postgresql.model.TutorialDetail;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TutorialDetailMapper {

    // This back reference is already omitted from JSON
    @Mapping(target = "tutorial", ignore = true)
    TutorialDetailDto toDto(TutorialDetail detail);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tutorial", ignore = true)
    TutorialDetail toEntity(TutorialDetailDto detailDto);

    List<TutorialDetailDto> toDtoList(List<TutorialDetail> details);

    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(TutorialDetailDto detailDto, @MappingTarget TutorialDetail detail);
}
