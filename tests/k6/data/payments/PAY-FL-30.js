const withAccept = (rule) => `
  rule: ${rule} -> decline;
  rule: other: amount() > 0 -> accept;
`;

export default {
  loads: ['captured', 'captured', 'captured'],
  variants: [
    { role: 'minutes', template: withAccept('count("card_token", 10, minutes) > 2'), expected: 'fatal' },
    { role: 'calendar-days', template: withAccept('count("card_token", 1, calendar_days) > 2'), expected: 'fatal' },
    { role: 'calendar-months', template: withAccept('count("card_token", 1, calendar_months) > 2'), expected: 'fatal' },
    { role: 'offset-hours', template: withAccept('count("card_token", 24, 1, hours) > 1'), expected: 'low' },
    { role: 'offset-days', template: withAccept('count("card_token", 30, 1, days) > 1'), expected: 'low' },
    { role: 'no-unit', template: withAccept('count("card_token", 1) > 2') },
    { role: 'offset-no-unit', template: withAccept('count("card_token", 2, 1) > 1') },
  ],
};
