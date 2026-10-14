const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
const paymentFactory = require("../factory/payment_factory.js");
const should = chai.should();
chai.use(chaiHttp);

const FB_API_URL = config.fbApi.url;
const INSPECT_PATH = config.fbApi.inspectPath;

module.exports.inspectPayment =
    function (done, checkResponse, email, ip, fingerprint, cardToken, partyId, shopId, amount, currency) {
        let id = Math.floor(Math.random() * 999);
        let TEST_INSPECTOR_PAYMENT_REQ_HIGH = {
            payment: paymentFactory.create(
                id,
                ip,
                email,
                fingerprint,
                cardToken,
                partyId,
                shopId,
                amount,
                currency
            )
        };

        chai.request(FB_API_URL, checkResponse)
            .post(INSPECT_PATH)
            .send(TEST_INSPECTOR_PAYMENT_REQ_HIGH)
            .end(function (err, res) {
                if (err) {
                    console.log(err.text);
                    done(err);
                }
                should.not.exist(err);
                checkResponse(res);
                done()
            });
    }