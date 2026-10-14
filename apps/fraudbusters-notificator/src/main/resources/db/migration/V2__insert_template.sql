---notification_template
INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('> 4kk RUB by cardToken 90 days', 'MAIL_FORM', '<>', 'cardToken', 'SELECT cardToken, sum(amount/100) AS sm ' ||
                                                                          'FROM fraud.payment ' ||
                                                                          'WHERE toDate(:currentDate) - INTERVAL 90 day <= timestamp ' ||
                                                                          'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                          'AND shopId != ''TEST'' ' ||
                                                                          'AND status = ''captured'' ' ||
                                                                          'AND currency = ''RUB'' ' ||
                                                                          'GROUP BY cardToken HAVING sm > 4000000');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('refund-by-captured', 'MAIL_FORM', '<>', 't,shopId,currency',
        'SELECT t, shopId, currency, sm_ref AS refund, sm_all AS payment, sm_ref * 100 / sm_all AS metric ' ||
        'FROM ( SELECT timestamp AS t, shopId, currency, sum(amount / 100) AS sm_ref ' ||
        'FROM fraud.refund ' ||
        'WHERE :currentDate <= timestamp AND status = ''succeeded'' AND shopId != ''TEST'' ' ||
        'GROUP BY t, currency, shopId) ' ||
        'ANY LEFT JOIN ( SELECT timestamp AS t, shopId, currency, sum(amount / 100) AS sm_all ' ||
        'FROM fraud.payment ' ||
        'WHERE :currentDate <= timestamp AND status = ''captured'' AND shopId != ''TEST'' ' ||
        'GROUP BY t, currency, shopId ' ||
        'HAVING (sm_all > 100000 AND currency = ''RUB'') OR (sm_all > 1500 AND currency = ''USD'') OR (sm_all > 1500 AND currency = ''EUR'')) ' ||
        'USING t, shopId, currency ' ||
        'WHERE sm_all > 0 AND metric > 10 ' ||
        'ORDER BY t DESC');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('count decline/all > 70% by 1 week', 'MAIL_FORM', '<>', 'shopId',
        'SELECT shopId,cnt,cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
        'FROM ( SELECT shopId, count(amount/100) AS cnt ' ||
        'FROM fraud.payment WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 week AND shopId != ''TEST'' AND status = ''captured'' AND currency = ''RUB'' ' ||
        'GROUP BY shopId ) ' ||
        'LEFT OUTER JOIN ' ||
        '(SELECT shopId, count(amount) AS cnt_decline ' ||
        'FROM fraud.payment ' ||
        'WHERE timestamp >= toDate(:currentDate) - INTERVAL 1 week AND status = ''failed'' AND shopId != ''TEST'' AND errorCode=''no_route_found:risk_score_is_too_high'' ' ||
        'GROUP BY shopId ) ' ||
        'USING shopId ' ||
        'WHERE cnt_procent > 70');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('Count fingerprint by all', 'MAIL_FORM', '<>', 't,fingerprint',
        'SELECT timestamp AS t, fingerprint, count(fingerprint) AS cnt ' ||
        'FROM fraud.payment ' ||
        'WHERE :currentDate <= timestamp ' ||
        'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
        'AND shopId != ''TEST'' AND fingerprint != '''' AND fingerprint != ''Other device'' AND notLike(fingerprint, ''Mozilla%'') AND notLike(fingerprint, ''Apache-HttpClient%'') ' ||
        'GROUP BY t, fingerprint ' ||
        'HAVING cnt > 70');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('>2 uniq shops by card', 'MAIL_FORM', '<>', 'cardToken', 'SELECT cardToken, count(id) AS invCnt ' ||
                                                                 'FROM fraud.payment ' ||
                                                                 'WHERE toDate(:currentDate) - INTERVAL 3 day > timestamp ' ||
                                                                 'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                 'AND cardToken GLOBAL IN ' ||
                                                                 '( SELECT cardToken ' ||
                                                                 'FROM fraud.payment AS uniqCnt ' ||
                                                                 'WHERE toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 3 hour <= eventTime ' ||
                                                                 'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                 'AND shopId != ''TEST'' ' ||
                                                                 'GROUP BY cardToken ' ||
                                                                 'HAVING uniq(shopId) > 2) AND shopId != ''TEST'' ' ||
                                                                 'GROUP BY cardToken ' ||
                                                                 'HAVING invCnt = 0');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('demo >40k', 'MAIL_FORM', '<>', 't,cardToken,currency',
        'SELECT timestamp AS t, cardToken, currency, max(amount / 100) AS maxAmount ' ||
        'FROM fraud.payment ' ||
        'WHERE :currentDate <= timestamp AND status = ''captured'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
        'GROUP BY t, cardToken, currency ' ||
        'HAVING maxAmount >= 40000');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('demo >70% failed 5 min', 'MAIL_FORM', '<>', 'shopId',
        'SELECT shopId, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
        'FROM ' ||
        '( SELECT shopId, count(id) AS cnt ' ||
        'FROM fraud.payment ' ||
        'WHERE toDateTime(eventTime) >= toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 5 MINUTE AND status = ''captured'' AND currency = ''RUB'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
        'GROUP BY shopId ) ' ||
        'LEFT OUTER JOIN ' ||
        '( SELECT shopId, count(id) AS cnt_decline ' ||
        'FROM fraud.payment ' ||
        'WHERE toDateTime(eventTime) >= toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 5 MINUTE AND status = ''failed'' AND errorCode=''no_route_found:risk_score_is_too_high'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
        'GROUP BY shopId ) ' ||
        'USING shopId ' ||
        'WHERE cnt_procent > 70');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('demo chargeback > 3 last 5 min', 'MAIL_FORM', '<>', 'partyId', 'SELECT partyId, count() AS cnt ' ||
                                                                        'FROM fraud.chargeback ' ||
                                                                        'WHERE toDateTime(eventTime) >= toDateTime(substring(:currentDateTime, 1, length(:currentDateTime) - 7)) - INTERVAL 5 MINUTE AND status = ''accepted'' AND partyId==''f42723d0-2022-4b66-9f92-4549769f1a92'' ' ||
                                                                        'GROUP BY partyId ' ||
                                                                        'HAVING cnt > 3');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('bin > 70 AND > 20 cnt', 'MAIL_FORM', '<>', 'bin,bankCountry',
        'SELECT bin, bankCountry, cnt, cnt_decline, cnt_decline * 100/cnt AS cnt_procent ' ||
        'FROM ' ||
        '( SELECT bin, bankCountry, count(id) AS cnt ' ||
        'FROM fraud.payment ' ||
        'WHERE timestamp >= toDate(:currentDate) - INTERVAL 2 day AND shopId != ''TEST'' AND status = ''captured'' AND currency = ''RUB'' ' ||
        'GROUP BY bin, bankCountry ) ' ||
        'LEFT OUTER JOIN ' ||
        '( SELECT bin, bankCountry, count(id) AS cnt_decline ' ||
        'FROM fraud.payment ' ||
        'WHERE timestamp >= toDate(:currentDate) - INTERVAL 2 day AND status = ''failed'' ' ||
        'AND shopId != ''TEST'' AND ( errorCode=''authorization_failed:rejected_by_issuer'' OR errorCode=''authorization_failed:insufficient_funds'' OR errorCode=''preauthorization_failed:three_ds_not_finished'' OR errorCode=''no_route_found:risk_score_is_too_high'' ) ' ||
        'GROUP BY bin, bankCountry ) ' ||
        'USING bin, bankCountry ' ||
        'WHERE cnt_procent > 70 AND cnt + cnt_decline > 150');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('Count fingerprint by shop', 'MAIL_FORM', '<>', 't,shopId,fingerprint',
        'SELECT timestamp AS t, shopId, fingerprint, count(fingerprint) AS cnt ' ||
        'FROM fraud.payment ' ||
        'WHERE :currentDate <= timestamp ' ||
        'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
        'AND shopId != ''TEST'' AND fingerprint != '''' AND fingerprint != ''Other device'' AND notLike(fingerprint, ''Mozilla%'') AND notLike(fingerprint, ''Apache-HttpClient%'') ' ||
        'GROUP BY t, shopId, fingerprint ' ||
        'HAVING cnt > 70');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('New shops', 'MAIL_FORM', '<>', 'partyId, shopId', 'SELECT partyId, shopId, cntToday ' ||
                                                           'FROM ( SELECT partyId, shopId, count() AS cnt ' ||
                                                           'FROM fraud.payment ' ||
                                                           'WHERE cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                           'AND shopId != ''TEST'' ' ||
                                                           'GROUP BY partyId, shopId ) ' ||
                                                           'ANY LEFT JOIN ' ||
                                                           '( SELECT partyId, shopId, count() AS cntToday ' ||
                                                           'FROM fraud.payment ' ||
                                                           'WHERE :currentDate <= timestamp ' ||
                                                           'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                           'AND shopId != ''TEST'' ' ||
                                                           'GROUP BY partyId, shopId ) ' ||
                                                           'USING  partyId, shopId ' ||
                                                           'WHERE cnt - cntToday == 0');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('> 50k EUR-USD by cardToken 90 days', 'MAIL_FORM', '<>', 'cardToken, currency',
        'SELECT cardToken, currency, sum(amount/100) AS sm ' ||
        'FROM fraud.payment ' ||
        'WHERE toDate(:currentDate) - INTERVAL 90 day <= timestamp ' ||
        'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
        'AND shopId != ''TEST'' AND status = ''captured'' AND (currency = ''EUR'' OR currency = ''USD'') ' ||
        'GROUP BY cardToken, currency ' ||
        'HAVING sm > 50000');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('Old shops that start pay', 'MAIL_FORM', '<>', 'partyId, shopId', 'SELECT partyId, shopId, cntToday ' ||
                                                                          'FROM ' ||
                                                                          '( SELECT partyId, shopId, cntOlderThreeMonth AS cnt ' ||
                                                                          'FROM ' ||
                                                                          '( SELECT partyId, shopId, count() AS cntLastThreeMonth ' ||
                                                                          'FROM fraud.payment ' ||
                                                                          'WHERE subtractDays(toDate(:currentDate), 90) <= timestamp ' ||
                                                                          'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                          'AND shopId != ''TEST'' ' ||
                                                                          'GROUP BY partyId, shopId  )  ' ||
                                                                          'ANY LEFT JOIN  ' ||
                                                                          '( SELECT partyId, shopId, count() AS cntOlderThreeMonth ' ||
                                                                          'FROM fraud.payment ' ||
                                                                          'WHERE subtractDays(toDate(:currentDate), 90) > timestamp ' ||
                                                                          'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                          'AND shopId != ''TEST'' ' ||
                                                                          'GROUP BY partyId, shopId  ) ' ||
                                                                          'USING partyId, shopId  ' ||
                                                                          'WHERE cntLastThreeMonth == 0 AND cntOlderThreeMonth > 0 ) ' ||
                                                                          'ANY LEFT JOIN ' ||
                                                                          '( SELECT partyId, shopId, count() AS cntToday ' ||
                                                                          'FROM fraud.payment ' ||
                                                                          'WHERE :currentDate <= timestamp ' ||
                                                                          'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
                                                                          'AND shopId != ''TEST'' ' ||
                                                                          'GROUP BY partyId, shopId ) ' ||
                                                                          'USING partyId, shopId ' ||
                                                                          'WHERE cnt - cntToday == 0');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('limits', 'MAIL_FORM', '<>', 't,cardToken,currency',
        'SELECT timestamp AS t, cardToken, currency, max(amount / 100) AS maxAmount, count() AS cnt, sum(amount / 100) AS sm_all ' ||
        'FROM fraud.payment ' ||
        'WHERE :currentDate <= timestamp AND status = ''captured'' AND shopId != ''TEST'' ' ||
        'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
        'GROUP BY t, cardToken, currency ' ||
        'HAVING maxAmount >= 400000 OR (cnt >= 10 AND sm_all >= 400000)');

INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('> 600k EUR-USD by cardToken 90 days', 'MAIL_FORM', '<>', 'cardToken',
        'SELECT cardToken, sum(amount/100) AS sm ' ||
        'FROM fraud.payment ' ||
        'WHERE toDate(:currentDate) - INTERVAL 90 day <= timestamp ' ||
        'AND cardToken NOT IN (''WLCeYVOp4bW1hJ3oCv8KG'',''4xINpeuFJKxLPFGrdcrnUK'',''vUp3YTW8wcLTS0VttdebO'',''3zRjUhax48E1IY7wp7XZrS'',''2ttEW2bq4CpuBRD0VOWW9g'',''MfYHwQObqcIYlUTZD0v1y'',''4Y3xD7ftAkQrAwpEds5lMk'',''3q70aYBG6EH14ATRxxaom8'',''quvSmP2lQwjouDkbF84Op'',''4rqCK4KF5VwQL6o0TBzAHT'',''6I2L72hZrsc2SHUbDIVoZF'',''2X4iZv4CZ7XIG01DSgj4bv'',''70DVkKLfywEJGaj7oqUY1Q'',''7fAtmPASn44rNYieJfiv13'',''2CL3xaXNWq76hEGcVMj1rF'',''F3hq028Qsuo4KBNbRu8ud'',''4XZts3K0zWi7JAfU9kK8Ku'',''3csJuJyUbM7JmDCFiJoTfj'',''8yCcCMHbhUWbH9Kh6ZtLW'',''5iR9GSS7mzRFaYH1yROOiC'',''7MZOauIs8yUtoKuCamADWv'',''56kxngx6KgFFHrUbynNxi7'') ' ||
        'AND shopId != ''TEST'' AND status = ''captured'' AND (currency = ''EUR'' OR currency = ''USD'') ' ||
        'GROUP BY cardToken ' ||
        'HAVING sm > 600000');

