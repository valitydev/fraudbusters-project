import http from 'k6/http';
import { decodeReply, encodeCall, TYPE } from '../lib/thrift.js';
import { withdrawalContextFields } from '../fixtures/withdrawal.js';

const RISK_SCORES = { 0: 'trusted', 1: 'low', 100: 'high', 9999: 'fatal' };

function woodyId() {
  return `k6${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`;
}

export function payoutInspectorClient(connection) {
  function call(method, args, stage) {
    const traceId = woodyId();
    const response = http.post(connection.inspectorUrl, encodeCall(method, 1, args), {
      headers: {
        'Content-Type': 'application/x-thrift',
        Accept: 'application/x-thrift',
        'woody.trace-id': traceId,
        'woody.span-id': traceId,
        'woody.parent-id': 'undefined',
      },
      responseType: 'binary',
      timeout: connection.requestTimeout,
      tags: { stage, name: stage },
      responseCallback: http.expectedStatuses({ min: 200, max: 599 }),
    });
    const failure = `HTTP ${response.status}, woody.error-type=${response.headers['Woody.error-type'] || ''}, `
      + `woody.error-reason=${response.headers['Woody.error-reason'] || ''}`;
    if (!response.body || response.body.byteLength === 0) return { status: response.status, failure };
    try {
      const reply = decodeReply(response.body);
      if (reply.applicationError) {
        return { status: response.status, failure: `${failure}; ${JSON.stringify(reply.applicationError)}` };
      }
      return { status: response.status, result: reply.result };
    } catch (error) {
      return { status: response.status, failure: `${failure}; ${error.message}` };
    }
  }

  return {
    inspectWithdrawal(withdrawal) {
      const answer = call('InspectWithdrawal', [{ id: 1, type: TYPE.STRUCT, value: withdrawalContextFields(withdrawal) }],
        'thrift_inspect_withdrawal');
      if (answer.failure) return answer;
      if (answer.result[1]) return { status: answer.status, invalidRequest: answer.result[1][1] || [] };
      if (answer.result[0] === undefined) return { status: answer.status, failure: 'no result' };
      return { status: answer.status, score: RISK_SCORES[answer.result[0]] || `unknown_${answer.result[0]}` };
    },

    isBlacklisted({ partyId, shopId, fieldName, value }) {
      const answer = call('IsBlacklisted', [{ id: 1, type: TYPE.STRUCT, value: [
        { id: 1, type: TYPE.STRING, value: partyId },
        { id: 2, type: TYPE.STRING, value: shopId },
        { id: 3, type: TYPE.STRING, value: fieldName },
        { id: 4, type: TYPE.STRING, value },
      ] }], 'thrift_is_blacklisted');
      if (answer.failure) return answer;
      return { status: answer.status, blacklisted: answer.result[0] };
    },
  };
}
