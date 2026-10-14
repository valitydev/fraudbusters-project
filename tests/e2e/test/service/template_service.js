const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
const should = chai.should();
chai.use(chaiHttp);

const TEMPLATE_PATH = config.fbManagement.templatePath;
const FB_MGMNT_URL = config.fbManagement.url;

module.exports.create = function (done, checkResponse, template, id) {

    let TEST_TEMPLATE = {
        id: id,
        lastUpdateDate: "2022-04-15T10:30:00",
        modifiedByUser: "test-user",
        template: template
    };

    chai.request(FB_MGMNT_URL)
        .post(TEMPLATE_PATH)
        .send(TEST_TEMPLATE)
        .end(function (err, res) {
            if (err) {
                console.log(err.text);
                console.log(res);
                done(err);
            }
            should.not.exist(err);
            should.not.exist(res.body.errors)
            checkResponse(res);
            done()
        });
}