const { randomUUID } = require('crypto');
const CARD_TOKEN = "aggr_test_token";
const IP = "192.1.1.1";
const EMAIL = "test_unique@vality.dev";

module.exports.create =
    function (id, ip, email, fingerprint, cardToken, partyId, shopId, amount, currency) {
        return {
            id: id || randomUUID(),
            customer: {
                name: "Test Test",
                device: {
                    ip: ip || IP,
                    fingerprint: fingerprint
                },
                contact: {
                    email: email || EMAIL,
                    phone: "79111111111"
                }
            },
            cash: {
                amount: amount || 10000,
                currency: currency || "RUB"
            },
            paymentResource: {
                type: "BankCard",
                bin: "427640",
                lastDigits: "6395",
                countryCode: "RUS",
                cardToken: cardToken || CARD_TOKEN,
                bankName: "bank",
                cardType: "debit",
                paymentSystem: "visa"
            },
            merchant: {
                id: partyId,
                shop: {
                    id: shopId,
                    name: "ShopName",
                    category: "category",
                    location: "location"
                }
            },
            provider: {
                id: "provider",
                terminalId: "term",
                country: "RUS"
            },
            payerType: "payment_resource",
            tokenMobileProvider: null,
            createdAt: new Date().toISOString(),
            description: "Test product"
        };
    }