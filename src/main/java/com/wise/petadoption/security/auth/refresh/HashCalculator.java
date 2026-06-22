package com.wise.petadoption.security.auth.refresh;

public interface HashCalculator {
    String calculate(String content);
}
