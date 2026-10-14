const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
const should = chai.should();
chai.use(chaiHttp);

const REFERENCE_PATH = config.fbManagement.referencePath;
const GROUP_PATH = config.fbManagement.groupPath;
const FB_MGMNT_URL = config.fbManagement.url;

module.exports.create = function (done, checkResponse, partyId, shopId, templateId) {
    let TEST_REFERENCES = [
        {
            id: "ref_" + Math.floor(Math.random() * 10000),
            lastUpdateDate: "2022-04-15T10:30:30",
            modifiedByUser: "test-user",
            shopId: shopId,
            partyId: partyId,
            templateId: templateId
        }
    ];

    chai.request(FB_MGMNT_URL)
        .post(REFERENCE_PATH)
        .send(TEST_REFERENCES)
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

module.exports.createGroup = function (done, checkResponse, groupId, groupReferenceId, shopId, partyId) {
    let TEST_GROUP = [{
        groupId: groupId,
        id: groupReferenceId,
        lastUpdateDate: "2022-04-15T10:30:30",
        modifiedByUser: "test-user",
        shopId: shopId,
        partyId: partyId
    }];

    chai.request(FB_MGMNT_URL)
        .post(GROUP_PATH + '/' + groupId + '/references')
        .send(TEST_GROUP)
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