const referenceService = require('./service/reference_service.js');
const templateService = require('./service/template_service.js');
const paymentService = require('./service/payment_service.js');
const inspectorService = require('./service/inspector_service.js');

const chai = require("chai");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);
const config = require("../config");

const testTimeout = config.testTimeout;

const TEMPLATE_ID = "test-templ-aggr-id";
const SHOP_ID = "shop-aggr-id";
const PARTY_ID = "party-aggr-id";
const CARD_TOKEN = "aggr_test_token";
const AGGR_FINGERPRINT = "aggr_fingerprint";

const TEMPLATE = "rule: unique(\"ip\", \"email\", 30, days, \"party_id\", \"shop_id\") > 1 " +
    " OR count(\"card_token\", 30, days, \"party_id\", \"shop_id\") > 2 " +
    "OR (sum(\"fingerprint\", 30, days, \"party_id\", \"shop_id\") > 20000 " +
    " AND currency() = \"RUB\")" +
    " -> decline;";

function generateIp(prefix) {
    return prefix + Math.floor(Math.random() * 999);
}

function generateCardToken() {
    return CARD_TOKEN + Math.floor(Math.random() * 999);
}

function generateEmail(prefix) {
    return prefix + Math.floor(Math.random() * 999);
}

function generateFingerprint() {
    return AGGR_FINGERPRINT + Math.floor(Math.random() * 999);
}

const status = "processed";
describe('Test for check aggregates', function () {
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
            }, TEMPLATE, TEMPLATE_ID);
    });

    it('it should create a new reference for template', function (done) {
        referenceService.create(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.length.should.be.eql(1);
                res.body.result.should.does.include(PARTY_ID + "_" + SHOP_ID);
            }, PARTY_ID, SHOP_ID, TEMPLATE_ID);
    });

    let ipUniq = generateIp("192.1.1.");
    let cardToken = generateCardToken();
    let fingerprint = generateFingerprint();
    let uniqueEmail = generateEmail("test_unique_1");

    it('it should inspect that payment have HIGH risk for UNIQUE', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('high');
            },
            uniqueEmail, ipUniq, fingerprint, cardToken, PARTY_ID, SHOP_ID);
    });

    it('it should upload payment to history', function (done) {
        paymentService.uploadPayment(done,
            (res) => {
                res.should.have.status(201);
            },
            status, uniqueEmail, ipUniq, cardToken, fingerprint, PARTY_ID, SHOP_ID);
    });

    const anotherUniqueEmail = generateEmail('test_unique_2');

    it('it should inspect that payment have FATAL risk for UNIQUE', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('fatal');
            }, anotherUniqueEmail, ipUniq, fingerprint, cardToken, PARTY_ID, SHOP_ID);
    });

    it('it should upload payment to history', function (done) {
        paymentService.uploadPayment(done, (res) => {
                res.should.have.status(201);
            },
            status, anotherUniqueEmail, ipUniq, cardToken, fingerprint, PARTY_ID, SHOP_ID);
    });

    const ipCount = generateIp("192.1.2.");
    const emailCount = generateEmail('test_count');

    it('it should inspect that payment have FATAL risk for COUNT', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('fatal');
            }, emailCount, ipCount, fingerprint, cardToken, PARTY_ID, SHOP_ID);
    });

    const emailSum = generateEmail('test_sum');
    let cardTokenSum = generateCardToken();

    it('it should inspect that payment have FATAL risk for SUM', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('fatal');
            }, emailSum, ipCount, fingerprint, cardTokenSum, PARTY_ID, SHOP_ID);
    });

});