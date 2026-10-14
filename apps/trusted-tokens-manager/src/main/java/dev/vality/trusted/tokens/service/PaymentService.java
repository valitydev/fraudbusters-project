package dev.vality.trusted.tokens.service;

import dev.vality.damsel.fraudbusters.Payment;

import java.util.List;

public interface PaymentService {

    void saveAll(List<Payment> payments);

}
