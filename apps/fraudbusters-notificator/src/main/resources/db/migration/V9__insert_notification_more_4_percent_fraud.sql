INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('> 4% decline', 'MAIL_FORM', '<>', 'partyId,shopId',
        'select partyId,shopId,percent  from ( ' ||
        'SELECT partyId,shopId,resultStatus,count() as status_count, ' ||
        'sum(status_count) over(partition by partyId, shopId) as host_count, ' ||
        'round(100 * status_count / host_count, 2) as percent     ' ||
        'FROM fraud.fruad ' ||
        'WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 day ' ||
        'GROUP BY partyId, shopId, resultStatus )' ||
        'where resultStatus = ''DECLINE'' and percent > 4 ' ||
        'order by percent ASC');
