const chai = require("chai");
const config = require("../../config");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);

const GROUP_PATH = config.fbManagement.groupPath;
const FB_MGMNT_URL = config.fbManagement.url;

module.exports.create = function (done, groupId, whiteListTemplateId, greyListTemplateId, blackListTemplateId) {

    let TEST_GROUP = {
        groupId: groupId,
        modifiedByUser: "test-user",
        priorityTemplates: [
            {
                id: whiteListTemplateId,
                lastUpdateTime: "2019-08-24T14:15:22Z",
                priority: 0
            },
            {
                id: greyListTemplateId,
                lastUpdateTime: "2019-08-24T14:15:22Z",
                priority: 2
            },
            {
                id: blackListTemplateId,
                lastUpdateTime: "2019-08-24T14:15:22Z",
                priority: 1
            }]
    };

    chai.request(FB_MGMNT_URL)
        .post(GROUP_PATH)
        .send(TEST_GROUP)
        .end(function (err) {
            if (err) {
                console.log(err.text);
                done(err);
            }

            done()
        });
}