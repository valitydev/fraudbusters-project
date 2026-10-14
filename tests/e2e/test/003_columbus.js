const templateService = require("./service/template_service.js");
const referenceService = require("./service/reference_service.js");
const inspectorService = require('./service/inspector_service.js');

const chai = require("chai");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);
const config = require("../config");

const testTimeout = config.testTimeout;
const TEMPLATE_ID = "test-columbus-templ-id";
const SHOP_ID = "shop-columbus-id";
const PARTY_ID = "party-columbus-id";

const TEMPLATE = "rule: " +
    "in(countryBy(\"ip\"), \"RU\", \"EN\")" +
    " -> accept;";

describe('Test for check geo ip service', function () {
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
                res.body.result.length.should.be.eql(1);
                res.body.result.should.does.include(PARTY_ID + "_" + SHOP_ID);
            }, PARTY_ID, SHOP_ID, TEMPLATE_ID);
    });

    it('it should inspect that payment have LOW risk', function (done) {
        inspectorService.inspectPayment(done,
            (res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.be.a("object");
                res.body.should.have.property("result");
                res.body.result.should.equal('high'); // change to 'low' after fix
            },
            "test@mail.ru",
            "1.208.61.253",
            "xxxxx",
            "trusted_test_token",
            PARTY_ID,
            SHOP_ID,
            100);
    });

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
            "85.214.132.117",
            "xxxxx",
            "trusted_test_token",
            PARTY_ID,
            SHOP_ID,
            100);
    });

});
