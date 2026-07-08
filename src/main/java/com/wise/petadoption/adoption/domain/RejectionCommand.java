package com.wise.petadoption.adoption.domain;

public record RejectionCommand (
        Long id,
        String decisionComment
){
}
