package com.bezkoder.spring_boot_jpa_postgresql.mapper;

import java.util.List;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.bezkoder.spring_boot_jpa_postgresql.dto.CourseDto;
import com.bezkoder.spring_boot_jpa_postgresql.model.Course;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CourseMapper {

    // The inverse collection is already hidden with @JsonIgnore.
    @Mapping(target = "tutorials", ignore = true)
    CourseDto toDto(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tutorials", ignore = true)
    Course toEntity(CourseDto courseDto);

    List<CourseDto> toDtoList(List<Course> courses);

    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(CourseDto courseDto, @MappingTarget Course course);
}
