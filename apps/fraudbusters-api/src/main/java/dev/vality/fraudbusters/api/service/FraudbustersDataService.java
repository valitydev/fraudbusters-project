package dev.vality.fraudbusters.api.service;

import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.api.exceptions.RemoteInvocationException;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudbustersDataService {

    private final PaymentServiceSrv.Iface paymentServiceSrv;

    public void insertChargebacks(List<Chargeback> chargebacks) {
        try {
            paymentServiceSrv.insertChargebacks(chargebacks);
        } catch (TException ex) {
            throw new RemoteInvocationException(ex);
        }
    }

    public void insertFraudPayments(List<FraudPayment> fraudPayments) {
        try {
            paymentServiceSrv.insertFraudPayments(fraudPayments);
        } catch (TException ex) {
            throw new RemoteInvocationException(ex);
        }
    }

    public void insertPaymentsChanges(List<Payment> payments) {
        try {
            paymentServiceSrv.insertPayments(payments);
        } catch (TException ex) {
            throw new RemoteInvocationException(ex);
        }
    }

    public void insertRefunds(List<Refund> refunds) {
        try {
            paymentServiceSrv.insertRefunds(refunds);
        } catch (TException ex) {
            throw new RemoteInvocationException(ex);
        }
    }

    public void insertWithdrawals(List<Withdrawal> withdrawals) {
        try {
            paymentServiceSrv.insertWithdrawals(withdrawals);
        } catch (TException ex) {
            throw new RemoteInvocationException(ex);
        }
    }
}
