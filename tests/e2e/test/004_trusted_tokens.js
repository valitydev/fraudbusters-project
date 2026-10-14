const chai = require("chai");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);
const config = require("../config");

const templateService = require("./service/template_service.js");
const referenceService = require("./service/reference_service.js");
const inspectorService = require("./service/inspector_service.js");
const paymentService = require("./service/payment_service.js");

const testTimeout = config.testTimeout;

const TEMPLATE_ID = "test-trustes-templ-id";
const SHOP_ID = "shop-trusted-id";
const PARTY_ID = "party-trusted-id";
const CARD_TOKEN = "trusted_test_token_";
const TEMPLATE = "rule: isTrusted(paymentsConditions(condition(\"RUB\" ,1 ,1 ,1 ))) -> accept;";

function generateCardToken() {
    return CARD_TOKEN + Math.floor(Math.random() * 999);
}

describe('Test for check trusted tokens', function () {
    this.timeout(testTimeout);

    it('it should create a new template', function (done) {
        templateService.create(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("errors");
                res.body.should.have.property("id");
                res.body.should.have.property("template");
                res.body.id.should.not.be.null;
                res.body.template.should.not.be.null;
                res.body.id.should.equal(TEMPLATE_ID);
                res.body.template.should.equal(TEMPLATE);
            },
            TEMPLATE,
            TEMPLATE_ID);
    });

    it('it should create a new reference for template', function (done) {
        referenceService.create(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.result.length.should.be.eql(1);
                res.body.result.should.does.include(PARTY_ID + "_" + SHOP_ID);
            }, PARTY_ID, SHOP_ID, TEMPLATE_ID);
    });

    let cardToken = generateCardToken();

    it('it should inspect that payment have default HIGH risk', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('high');
            },
            "test@mail.ru",
            "123.123.123.123",
            "xxxxx",
            cardToken,
            PARTY_ID,
            SHOP_ID,
            100);
    });

    it('it should upload payment to history', function (done) {
        paymentService.uploadPayment(done,
            (res) => {
                res.should.have.status(201);
            },
            "captured",
            "test@mail.ru",
            "123.123.123.123",
            cardToken,
            "xxxxx",
            PARTY_ID,
            SHOP_ID,
            "invoice_id_1");
    });

    it('it should upload payment to history', function (done) {
        paymentService.uploadPayment(done,
            (res) => {
                res.should.have.status(201);
            },
            "captured",
            "test@mail.ru",
            "123.123.123.123",
            cardToken,
            "xxxxx",
            PARTY_ID,
            SHOP_ID,
            "invoice_id_1.1");
    });

    it('it should inspect that payment have LOW risk', function (done) {
        setTimeout(() => inspectorService.inspectPayment(done,
                (res) => {
                    res.should.have.status(200);
                    res.should.be.json;
                    res.body.should.be.a("object");
                    res.body.should.have.property("result");
                    res.body.result.should.equal('low');
                },
                "test@mail.ru",
                "123.123.123.123",
                "xxxxx",
                cardToken,
                PARTY_ID,
                SHOP_ID,
                1000),
            5000);

    });
});