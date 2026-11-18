export default {
  template: `
    rule: probe: amount() = 7999 -> decline;
    rule: bank_rus: countryBy("country_bank") = "RUS" and amount() = 7001 -> decline;
    rule: bank_ru: countryBy("country_bank") = "RU" and amount() = 7001 -> decline;
    rule: bank_unknown: countryBy("country_bank") = "UNKNOWN" and amount() = 7002 -> decline;
    rule: ip_in_list: in(countryBy("ip"), "KOR", "KR", "RUS", "RU", "USA", "US") -> highRisk;
    rule: ip_unknown: countryBy("ip") = "UNKNOWN" -> accept;
  `,
  ready: { amount: 7999, ip: '1.208.61.253', decision: 'fatal' },
  scenarios: [
    { name: 'private ip has no country', payment: { ip: '10.0.0.1' }, expected: 'accept/ip_unknown' },
    { name: 'bank country', payment: { amount: 7001 } },
    { name: 'public ip', payment: { ip: '1.208.61.253' } },
  ],
};
