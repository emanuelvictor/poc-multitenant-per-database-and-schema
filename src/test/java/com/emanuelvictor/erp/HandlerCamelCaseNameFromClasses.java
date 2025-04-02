package com.emanuelvictor.erp;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

public class HandlerCamelCaseNameFromClasses extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForClass(Class<?> classeDeTeste) {
        return substituirCamelCase(super.generateDisplayNameForClass(classeDeTeste));
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> classesFilhas) {
        return substituirCamelCase(super.generateDisplayNameForNestedClass(classesFilhas));
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testeDeClasse, Method testeDoMetodo) {
        return substituirCamelCase(testeDoMetodo.getName()) +
                DisplayNameGenerator.parameterTypesAsString(testeDoMetodo);
    }

    private static String substituirCamelCase(String classeOuMetodoEmCamelCase) {
        StringBuilder resultado = new StringBuilder();
        resultado.append(classeOuMetodoEmCamelCase.charAt(0));
        for (int i = 1; i < classeOuMetodoEmCamelCase.length(); i++) {
            if (Character.isUpperCase(classeOuMetodoEmCamelCase.charAt(i))) {
                resultado.append(' ');
                resultado.append(Character.toLowerCase(classeOuMetodoEmCamelCase.charAt(i)));
            } else {
                resultado.append(classeOuMetodoEmCamelCase.charAt(i));
            }
        }
        return resultado.toString();
    }
}