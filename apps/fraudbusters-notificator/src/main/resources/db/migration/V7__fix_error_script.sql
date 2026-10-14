UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT bin, bankCountry, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
                 'FROM ' ||
                 '( SELECT bin, bankCountry, count(id) AS cnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE timestamp >= toDate(:currentDate) - INTERVAL 2 day AND shopId != ''TEST'' ' ||
                 'AND status = ''captured'' AND currency = ''RUB'' ' ||
                 'GROUP BY bin, bankCountry ) as captured_payment ' ||
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
