UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT cardToken, count(id) AS invCnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE toDate(:currentDate) - INTERVAL 3 day > timestamp ' ||
                 'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                 'AND cardToken GLOBAL IN ' ||
                 '( SELECT cardToken ' ||
                 'FROM fraud.payment AS uniqCnt ' ||
                 'WHERE toDateTime(:currentDateTime) - INTERVAL 3 hour <= eventTime ' ||
                 'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                 'AND shopId != ''TEST'' ' ||
                 'GROUP BY cardToken ' ||
                 'HAVING uniq(shopId) > 2) AND shopId != ''TEST'' ' ||
                 'GROUP BY cardToken ' ||
                 'HAVING invCnt = 0'
where id = 5;

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT shopId, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
                 'FROM ' ||
                 '( SELECT shopId, count(id) AS cnt ' ||
                 'FROM fraud.payment ' ||
                 'WHERE toDateTime(eventTime) >= toDateTime(:currentDateTime) - INTERVAL 5 MINUTE AND status = ''captured'' AND currency = ''RUB'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
                 'GROUP BY shopId ) ' ||
                 'LEFT OUTER JOIN ' ||
                 '( SELECT shopId, count(id) AS cnt_decline ' ||
                 'FROM fraud.payment ' ||
                 'WHERE toDateTime(eventTime) >= toDateTime(:currentDateTime) - INTERVAL 5 MINUTE AND status = ''failed'' AND errorCode=''no_route_found:risk_score_is_too_high'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
                 'GROUP BY shopId ) ' ||
                 'USING shopId ' ||
                 'WHERE cnt_procent > 70'
where id = 7;

UPDATE
    fb_notificator.notification_template
set query_text = 'SELECT partyId, count() AS cnt ' ||
                 'FROM fraud.chargeback ' ||
                 'WHERE toDateTime(eventTime) >= toDateTime(:currentDateTime) - INTERVAL 5 MINUTE AND status = ''accepted'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
                 'GROUP BY partyId ' ||
                 'HAVING cnt > 3'
where id = 8;