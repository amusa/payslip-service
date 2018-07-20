/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.service.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maliska
 */
public class ValidatorProcessor {

    List<Validator> validators;

    public ValidatorProcessor() {
        this.validators = new ArrayList<>();
    }

    public ValidatorProcessor(List<Validator> validators) {
        super();
        this.validators = validators;
    }

    public void add(Validator validator) {
        this.validators.add(validator);
    }

    public void process() throws Exception {
        for (Validator validator : validators) {
            validator.validate();
        }
    }

}
