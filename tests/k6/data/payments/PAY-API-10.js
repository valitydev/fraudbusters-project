export default {
  black: { header: 'party_id,shop_id,list_name,value', listName: 'CARD_TOKEN', rows: 5 },
  grey: {
    header: 'party_id,shop_id,list_name,value,count_info_count,count_info_start_time,count_info_end_time',
    listName: 'CARD_TOKEN',
    value: '{{csv-grey}}',
    count: 3,
    durationMs: 3600 * 1000,
  },
};
