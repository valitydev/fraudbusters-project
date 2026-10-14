UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT t, shopId, currency, sm_ref AS refund, sm_all AS payment, sm_ref * 100 / sm_all AS metric ' ||
                 'FROM ( SELECT timestamp AS t, shopId, currency, sum(amount / 100) AS sm_ref ' ||
                 'FROM fraud.refund ' ||
                 'WHERE :currentDate <= timestamp AND status = ''succeeded'' AND shopId != ''TEST'' ' ||
                 'GROUP BY t, currency, shopId) as refunds_sum ' ||
                 'ANY LEFT JOIN ( SELECT timestamp AS t, shopId, currency, sum(amount / 100) AS sm_all ' ||
                 'FROM fraud.payment ' ||
                 'WHERE :currentDate <= timestamp AND status = ''captured'' AND shopId != ''TEST'' ' ||
                 'GROUP BY t, currency, shopId ' ||
                 'HAVING (sm_all > 100000 AND currency = ''RUB'') OR (sm_all > 1500 AND currency = ''USD'') ' ||
                 'OR (sm_all > 1500 AND currency = ''EUR'')) as payments_sum ' ||
                 'USING (t, shopId, currency) ' ||
                 'WHERE sm_all > 0 AND metric > 10 ' ||
                 'ORDER BY t DESC'
where name = 'refund-by-captured';

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT shopId,cnt,cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
                 'FROM ( SELECT shopId, count(amount/100) AS cnt ' ||
                 'FROM fraud.payment WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 week AND shopId != ''TEST'' AND status = ''captured'' AND currency = ''RUB'' ' ||
                 'GROUP BY shopId ) as captured_payment ' ||
                 'LEFT OUTER JOIN ' ||
                 '(SELECT shopId, count(amount) AS cnt_decline ' ||
                 'FROM fraud.payment ' ||
                 'WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 week AND status = ''failed'' AND shopId != ''TEST'' AND errorCode=''no_route_found:risk_score_is_too_high'' ' ||
                 'GROUP BY shopId ) as declined_payment ' ||
                 'USING shopId ' ||
                 'WHERE cnt_procent > 70'
where name = 'count decline/all > 70% by 1 week';

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT shopId, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
                 'FROM ' ||
                 '( SELECT shopId, count(id) AS cnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE toDateTime(eventTime) >= toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 5 MINUTE ' ||
                 'AND status = ''captured'' AND currency = ''RUB''' ||
                 'GROUP BY shopId ) as captured_payment ' ||
                 'LEFT OUTER JOIN ' ||
                 '( SELECT shopId, count(id) AS cnt_decline ' ||
                 'FROM fraud.payment ' ||
                 'WHERE toDateTime(eventTime) >= toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 5 MINUTE ' ||
                 'AND status = ''failed'' AND errorCode=''no_route_found:risk_score_is_too_high''' ||
                 'GROUP BY shopId ) as failed_payment' ||
                 'USING shopId ' ||
                 'WHERE cnt_procent > 70'
where name = 'demo >70% failed 5 min';

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT bin, bankCountry, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
                 'FROM ' ||
                 '( SELECT bin, bankCountry, count(id) AS cnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE timestamp >= toDate(:currentDate) - INTERVAL 2 day AND shopId != ''TEST'' ' ||
                 'AND status = ''captured'' AND currency = ''RUB'' ' ||
                 'GROUP BY bin, bankCountry ) as captured_payment' ||
                 'LEFT OUTER JOIN ' ||
                 '( SELECT bin, bankCountry, count(id) AS cnt_decline ' ||
                 'FROM fraud.payment ' ||
                 'WHERE timestamp >= toDate(:currentDate) - INTERVAL 2 day AND status = ''failed'' ' ||
                 'AND shopId != ''TEST'' AND ( errorCode=''authorization_failed:rejected_by_issuer'' ' ||
                 'OR errorCode=''authorization_failed:insufficient_funds'' ' ||
                 'OR errorCode=''preauthorization_failed:three_ds_not_finished'' ' ||
                 'OR errorCode=''no_route_found:risk_score_is_too_high'' ) ' ||
                 'GROUP BY bin, bankCountry ) as failed_payment ' ||
                 'USING bin, bankCountry ' ||
                 'WHERE cnt_procent > 70 AND cnt + cnt_decline > 150'
where name = 'bin > 70 AND > 20 cnt';

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT partyId, shopId, cntToday ' ||
                 'FROM ( SELECT partyId, shopId, count() AS cnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE shopId != ''TEST'' ' ||
                 'GROUP BY partyId, shopId ) as all_time ' ||
                 'ANY LEFT JOIN ' ||
                 '( SELECT partyId, shopId, count() AS cntToday ' ||
                 'FROM fraud.payment ' ||
                 'WHERE :currentDate <= timestamp ' ||
                 'AND shopId != ''TEST'' ' ||
                 'GROUP BY partyId, shopId ) as near_time ' ||
                 'USING  partyId, shopId ' ||
                 'WHERE cnt - cntToday == 0'
where name = 'New shops';

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT partyId, shopId, cntToday ' ||
                 'FROM ' ||
                 '( SELECT partyId, shopId, cntOlderThreeMonth AS cnt ' ||
                 'FROM ' ||
                 '( SELECT partyId, shopId, count() AS cntLastThreeMonth ' ||
                 'FROM fraud.payment ' ||
                 'WHERE subtractDays(toDate(:currentDate), 90) <= timestamp ' ||
                 'AND shopId != ''TEST'' ' ||
                 'GROUP BY partyId, shopId  ) as payment_last  ' ||
                 'ANY LEFT JOIN  ' ||
                 '( SELECT partyId, shopId, count() AS cntOlderThreeMonth ' ||
                 'FROM fraud.payment ' ||
                 'WHERE subtractDays(toDate(:currentDate), 90) > timestamp ' ||
                 'AND shopId != ''TEST'' ' ||
                 'GROUP BY partyId, shopId  ) as payment_older ' ||
                 'USING partyId, shopId  ' ||
                 'WHERE cntLastThreeMonth == 0 AND cntOlderThreeMonth > 0 ) as sum_older ' ||
                 'ANY LEFT JOIN ' ||
                 '( SELECT partyId, shopId, count() AS cntToday ' ||
                 'FROM fraud.payment ' ||
                 'WHERE :currentDate <= timestamp ' ||
                 'AND shopId != ''TEST'' ' ||
                 'GROUP BY partyId, shopId ) as cnt_older ' ||
                 'USING partyId, shopId ' ||
                 'WHERE cnt - cntToday == 0'
where name = 'Old shops that start pay';