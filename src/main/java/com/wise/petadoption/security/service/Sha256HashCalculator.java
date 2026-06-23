package com.wise.petadoption.security.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class Sha256HashCalculator implements HashCalculator {
    @Override
    public String calculate(String content) {
        return DigestUtils.sha256Hex(content);
    }
}
