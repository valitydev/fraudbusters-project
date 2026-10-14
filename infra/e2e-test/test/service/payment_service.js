const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
const paymentFactory = require("../factory/payment_factory.js");
const should = chai.should();
chai.use(chaiHttp);

const HISTORY_PAYMENT_PATH = config.fbApi.historyPaymentsPath;
const FB_API_URL = config.fbApi.url;

module.exports.uploadPayment = function (done, checkResponse, status, email, ip, cardToken, fingerprint, partyId, shopId, paymentId) {
        let UPLOAD_PAYMENT_DATA = {
            paymentsChanges: [
                {
                    payment: paymentFactory.create(
                        paymentId,
                        ip,
                        email,
                        fingerprint,
                        cardToken,
                        partyId,
                        shopId
                    ),
                    paymentStatus: status,
                    eventTime: (new Date()).toJSON(),
                }
            ]
        };

        chai.request(FB_API_URL)
            .post(HISTORY_PAYMENT_PATH)
            .send(UPLOAD_PAYMENT_DATA)
            .end(function (err, res) {
                if (err) {
                    console.log(err.text);
                    done(err);
                }
                should.not.exist(err);
                checkResponse(res);
                setTimeout(() => done(), 10000);
            });
}