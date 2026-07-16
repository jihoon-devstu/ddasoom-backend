package com.paw.ddasoom.animal.util;

import org.springframework.core.convert.converter.Converter;

import com.paw.ddasoom.animal.domain.AnimalKind;

public class AnimalKindConverter implements Converter<String, AnimalKind> {

    @Override
    public AnimalKind convert(String value) {
        if (value == null || value.isBlank()) return null;
        return AnimalKind.fromCode(value);
    }
}