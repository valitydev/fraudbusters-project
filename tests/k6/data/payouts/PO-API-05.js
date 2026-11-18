export default {
  template: 'rule: in("party_id", "{{party-00}}") -> decline;',
  partiesWithWallet: 5,
  pageSize: 4,
  expected: { walletWithoutParty: 400, removeMissing: 404 },
};
