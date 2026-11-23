package dev.vality.trusted.tokens.service;

import dev.vality.damsel.fraudbusters.Payout;

import java.util.List;

public interface PayoutService {

    void saveAll(List<Payout> payouts);

}
