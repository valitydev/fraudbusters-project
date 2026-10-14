INSERT INTO fb_notificator.notification_template (name, type, skeleton, basic_params, query_text)
VALUES ('> 5k EUR by cardToken or email on shop', 'MAIL_FORM', '<>', 'partyId,shopId,cardToken,email,currency',
        'SELECT partyId, shopId, cardToken, email, currency, sum(amount/100) AS sm ' ||
        'FROM fraud.payment ' ||
        'WHERE shopId != ''TEST'' AND status = ''captured'' AND currency = ''EUR'' ' ||
        'GROUP BY partyId, shopId, cardToken, email, currency ' ||
        'HAVING sm > 5000');
