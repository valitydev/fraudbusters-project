export default {
  otherTemplate: 'rule: amount() > 0 -> accept;',
  globalTemplate: 'rule: in("party_id", "{{party-target}}") -> decline;',
  expected: { other: 'low', targetDeclined: 'fatal', afterRemoval: 'high' },
};
