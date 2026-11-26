import { Chargeback } from '../../../api/fb-management/swagger-codegen/model/chargeback';
import { ClientInfo } from '../../../api/fb-management/swagger-codegen/model/clientInfo';
import { FraudPayment } from '../../../api/fb-management/swagger-codegen/model/fraudPayment';
import { InspectResult } from '../../../api/fb-management/swagger-codegen/model/inspectResult';
import { ModelError } from '../../../api/fb-management/swagger-codegen/model/modelError';
import { Payment } from '../../../api/fb-management/swagger-codegen/model/payment';
import { ProviderInfo } from '../../../api/fb-management/swagger-codegen/model/providerInfo';
import { Refund } from '../../../api/fb-management/swagger-codegen/model/refund';
import { Withdrawal } from '../../../api/fb-management/swagger-codegen/model/withdrawal';
import { WithdrawalInspectResult } from '../../../api/fb-management/swagger-codegen/model/withdrawalInspectResult';
import { hasDetail } from '../../utils/has-detail';

export interface HistoricalDetail {
    label: string;
    value: string;
    /** Identifiers and tokens read better in a monospace font. */
    mono?: boolean;
}

/** One record of any historical tab, flattened for the shared table, details and CSV export. */
export interface HistoricalRow<T = unknown> {
    /** Unique within the loaded page: selection and expansion are keyed by it. */
    key: string;
    id: string;
    status?: string;
    partyId?: string;
    /** Shop ID for payments, wallet ID for payouts. */
    scopeId?: string;
    email?: string;
    userId?: string;
    /** Amount in the minor units supplied by the API. */
    amount?: number;
    currency?: string;
    /** Payment system and BIN, e.g. "VISA · 424242". */
    method?: string;
    category?: string;
    code?: string;
    fraudType?: string;
    template?: string;
    rule?: string;
    eventTime?: Date;
    details: HistoricalDetail[];
    source: T;
}

function text(value: unknown): string {
    return hasDetail(value) ? String(value).trim() : '';
}

function detail(label: string, value: unknown, mono = false): HistoricalDetail | null {
    const shown = text(value);
    return shown ? { label, value: shown, ...(mono ? { mono } : {}) } : null;
}

function details(...items: (HistoricalDetail | null)[]): HistoricalDetail[] {
    return items.filter((item): item is HistoricalDetail => item !== null);
}

/** "424242 •••• 4242", or whichever part is known. */
export function maskedCard(bin?: string, lastDigits?: string): string {
    const first = text(bin);
    const last = text(lastDigits);
    if (!first && !last) {
        return '';
    }
    return [first, '••••', last].filter(Boolean).join(' ');
}

export function formatMinorAmount(amount?: number, currency?: string): string {
    if (amount === null || amount === undefined) {
        return '';
    }
    const code = currency?.trim();
    const digits =
        code && /^[a-z]{3}$/i.test(code)
            ? new Intl.NumberFormat('en-US', { style: 'currency', currency: code }).resolvedOptions()
                  .maximumFractionDigits
            : 2;
    return (amount / 10 ** digits).toLocaleString('en-US', {
        minimumFractionDigits: digits,
        maximumFractionDigits: digits,
    });
}

export function formatAmount(amount?: number, currency?: string): string {
    const formatted = formatMinorAmount(amount, currency);
    return formatted && currency ? `${formatted} ${currency}` : formatted;
}

/**
 * Payment tools arrive as Thrift text, e.g.
 * `BankCard(token:…, payment_system:PaymentSystemRef(id:visa), bin:427640, last_digits:6395, category:)`.
 * Shows "Bank card · visa · 427640 •••• 6395" instead; unknown shapes stay as they are.
 */
export function formatPaymentTool(raw?: string): string {
    const value = text(raw);
    const kind = /^([A-Za-z]+)\(/.exec(value)?.[1];
    if (!kind) {
        return value;
    }
    const field = (name: string) => new RegExp(`\\b${name}:([^,()\\s]+)`).exec(value)?.[1] ?? '';
    const label = kind.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/^./, (first) => first.toUpperCase());
    const name = label.charAt(0) + label.slice(1).toLowerCase();
    return [name, field('id'), maskedCard(field('bin'), field('last_digits'))].filter(Boolean).join(' · ');
}

function method(system?: string, bin?: string): string {
    return [text(system), text(bin)].filter(Boolean).join(' · ');
}

function errorText(error?: ModelError): string {
    return [text(error?.errorCode), text(error?.errorReason)].filter(Boolean).join(': ');
}

function flag(value?: boolean): string {
    return value === undefined || value === null ? '' : value ? 'Yes' : 'No';
}

function clientDetails(client?: ClientInfo): (HistoricalDetail | null)[] {
    return [
        detail('Fingerprint', client?.fingerprint, true),
        detail('Client IP', client?.ip, true),
        detail('Customer ID', client?.customerId, true),
    ];
}

function providerDetails(provider?: ProviderInfo): (HistoricalDetail | null)[] {
    return [
        detail('Provider', provider?.providerId, true),
        detail('Terminal', provider?.terminalId, true),
        detail('Provider country', provider?.country),
    ];
}

export function paymentDetails(payment: Payment): HistoricalDetail[] {
    return details(
        payment.paymentId && payment.paymentId !== payment.id ? detail('Payment ID', payment.paymentId, true) : null,
        detail('Card token', payment.cardToken, true),
        detail('Card', maskedCard(payment.bin, payment.lastDigits), true),
        detail('Payment tool', formatPaymentTool(payment.paymentTool)),
        detail('Payment country', payment.paymentCountry),
        ...clientDetails(payment.clientInfo),
        ...providerDetails(payment.provider),
        detail('Payer type', payment.payerType),
        detail('Mobile', flag(payment.mobile)),
        detail('Recurrent', flag(payment.recurrent)),
        detail('Error', errorText(payment.error))
    );
}

function paymentFields(payment: Payment) {
    return {
        id: payment.id ?? '',
        status: payment.status,
        partyId: payment.merchantInfo?.partyId,
        scopeId: payment.merchantInfo?.shopId,
        email: payment.clientInfo?.email,
        userId: payment.clientInfo?.userId,
        amount: payment.amount,
        currency: payment.currency,
        method: method(payment.paymentSystem, payment.bin),
        eventTime: payment.eventTime,
    };
}

/** Rule engine results keep the evaluated payment; templates are "RULE_NOT_CHECKED" when none matched. */
export function inspectResultRow(result: InspectResult, index: number): HistoricalRow<InspectResult> {
    const payment = result.payment ?? {};
    const template = result.checkedTemplate === 'RULE_NOT_CHECKED' ? '' : text(result.checkedTemplate);
    return {
        ...paymentFields(payment),
        key: `${payment.id}#${index}`,
        status: result.resultStatus,
        template,
        rule: template ? text(result.ruleChecked) : '',
        details: details(
            detail('Amount', formatAmount(payment.amount, payment.currency)),
            detail('Payment status', payment.status),
            detail('Method', method(payment.paymentSystem, payment.bin)),
            detail('Notification rules', (result.notificationsRule ?? []).join(', ')),
            ...paymentDetails(payment)
        ),
        source: result,
    };
}

export function paymentRow(payment: Payment, index: number): HistoricalRow<Payment> {
    return {
        ...paymentFields(payment),
        key: `${payment.id}#${index}`,
        details: paymentDetails(payment),
        source: payment,
    };
}

export function refundRow(refund: Refund, index: number): HistoricalRow<Refund> {
    return {
        key: `${refund.id}#${index}`,
        id: refund.id ?? '',
        status: refund.status,
        partyId: refund.merchantInfo?.partyId,
        scopeId: refund.merchantInfo?.shopId,
        email: refund.clientInfo?.email,
        userId: refund.clientInfo?.userId,
        amount: refund.amount,
        currency: refund.currency,
        method: formatPaymentTool(refund.paymentTool),
        eventTime: refund.eventTime,
        details: details(
            detail('Payment ID', refund.paymentId, true),
            detail('Payment tool', formatPaymentTool(refund.paymentTool)),
            ...clientDetails(refund.clientInfo),
            ...providerDetails(refund.provider),
            detail('Payer type', refund.payerType),
            detail('Error', errorText(refund.error))
        ),
        source: refund,
    };
}

export function chargebackRow(chargeback: Chargeback, index: number): HistoricalRow<Chargeback> {
    return {
        key: `${chargeback.id}#${index}`,
        id: chargeback.id ?? '',
        status: chargeback.status,
        partyId: chargeback.merchantInfo?.partyId,
        scopeId: chargeback.merchantInfo?.shopId,
        email: chargeback.clientInfo?.email,
        userId: chargeback.clientInfo?.userId,
        amount: chargeback.amount,
        currency: chargeback.currency,
        category: chargeback.category,
        code: chargeback.chargebackCode,
        eventTime: chargeback.eventTime,
        details: details(
            detail('Payment ID', chargeback.paymentId, true),
            detail('Disputed amount', formatAmount(chargeback.amount, chargeback.currency)),
            detail('Payment tool', formatPaymentTool(chargeback.paymentTool)),
            ...clientDetails(chargeback.clientInfo),
            ...providerDetails(chargeback.provider),
            detail('Payer type', chargeback.payerType)
        ),
        source: chargeback,
    };
}

export function fraudPaymentRow(fraud: FraudPayment, index: number): HistoricalRow<FraudPayment> {
    const payment = fraud.payment ?? {};
    return {
        ...paymentFields(payment),
        key: `${payment.id}#${index}`,
        fraudType: fraud.type,
        details: details(detail('Comment', fraud.comment), ...paymentDetails(payment)),
        source: fraud,
    };
}

function destination(type?: string, system?: string, bin?: string): string {
    const kind = type === Withdrawal.DestinationTypeEnum.BankCard ? 'Bank card' : text(type);
    return [kind, method(system, bin)].filter(Boolean).join(' · ');
}

export function payoutRow(payout: Withdrawal, index: number): HistoricalRow<Withdrawal> {
    return {
        key: `${payout.id}#${index}`,
        id: payout.id ?? '',
        status: payout.status,
        partyId: payout.partyId,
        scopeId: payout.walletId,
        email: payout.clientInfo?.email,
        userId: payout.clientInfo?.userId,
        amount: payout.amount,
        currency: payout.currency,
        method: destination(payout.destinationType, payout.paymentSystem, payout.bin),
        eventTime: payout.eventTime,
        details: details(
            detail('Card token', payout.cardToken, true),
            detail('Card', maskedCard(payout.bin, payout.lastDigits), true),
            detail('Issuer country', payout.issuerCountry),
            detail('Account ID', payout.accountId, true),
            detail('Account currency', payout.accountCurrency),
            ...clientDetails(payout.clientInfo),
            ...providerDetails(payout.provider),
            detail('Error', errorText(payout.error))
        ),
        source: payout,
    };
}

export function payoutInspectResultRow(
    result: WithdrawalInspectResult,
    index: number
): HistoricalRow<WithdrawalInspectResult> {
    const template = result.checkedTemplate === 'RULE_NOT_CHECKED' ? '' : text(result.checkedTemplate);
    return {
        key: `${result.withdrawalId}#${index}`,
        id: result.withdrawalId ?? '',
        status: result.resultStatus,
        partyId: result.partyId,
        scopeId: result.walletId,
        email: result.clientInfo?.email,
        userId: result.clientInfo?.userId,
        amount: result.amount,
        currency: result.currency,
        method: destination(result.destinationType, result.paymentSystem, result.bin),
        template,
        rule: template ? text(result.ruleChecked) : '',
        eventTime: result.eventTime,
        details: details(
            detail('Card token', result.cardToken, true),
            detail('Card', maskedCard(result.bin, result.lastDigits), true),
            detail('Issuer country', result.issuerCountry),
            ...clientDetails(result.clientInfo)
        ),
        source: result,
    };
}

export { statusTone } from '../../../shared/utils/status-tone';
