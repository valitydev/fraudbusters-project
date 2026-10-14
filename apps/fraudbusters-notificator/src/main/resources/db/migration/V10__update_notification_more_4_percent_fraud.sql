UPDATE
    fb_notificator.notification_template
set query_text =
        'select partyId,shopId,percent  from ( ' ||
        'SELECT partyId,shopId,resultStatus,count() as status_count, ' ||
        'sum(status_count) over(partition by partyId, shopId) as host_count, ' ||
        'round(100 * status_count / host_count, 2) as percent     ' ||
        'FROM fraud.events_unique ' ||
        'WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 day ' ||
        'GROUP BY partyId, shopId, resultStatus )' ||
        'where resultStatus = ''DECLINE'' and percent > 4 ' ||
        'order by percent ASC'
where name = '> 4% decline';