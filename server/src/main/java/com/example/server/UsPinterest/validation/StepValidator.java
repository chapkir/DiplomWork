package com.example.server.UsPinterest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StepValidator implements ConstraintValidator<Step, Double> {
    private double step;

    @Override
    public void initialize(Step constraintAnnotation) {
        this.step = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        double divided = value / step;
        return Math.abs(divided - Math.round(divided)) < 1e-8;
    }
}