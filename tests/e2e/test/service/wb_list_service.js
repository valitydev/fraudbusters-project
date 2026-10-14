const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
const should = chai.should();
chai.use(chaiHttp);

const LISTS_PATH = config.fbManagement.listsPath;
const FB_MGMNT_URL = config.fbManagement.url;

module.exports.create = function (done, checkResponse, listType, listName, partyId, shopId, value, countInfo) {

    let LIST_TEMPLATE = {
        listType: listType,
        records: [
            {
                countInfo: countInfo,
                listRecord: {
                    listName: listName,
                    partyId: partyId,
                    shopId: shopId,
                    value: value
                }
            }
        ]
    };

    chai.request(FB_MGMNT_URL)
        .post(LISTS_PATH)
        .send(LIST_TEMPLATE)
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