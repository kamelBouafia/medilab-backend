package com.medilab.mapper;

import org.mapstruct.Mapper;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface DateTimeMapper {

    default String asString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toString();
    }

    default OffsetDateTime asOffsetDateTime(String string) {
        if (string == null) {
            return null;
        }
        return OffsetDateTime.parse(string);
    }
}
