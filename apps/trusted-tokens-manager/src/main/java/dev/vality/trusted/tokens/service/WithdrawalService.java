package dev.vality.trusted.tokens.service;

import dev.vality.damsel.fraudbusters.Withdrawal;

import java.util.List;

public interface WithdrawalService {

    void saveAll(List<Withdrawal> withdrawals);

}
