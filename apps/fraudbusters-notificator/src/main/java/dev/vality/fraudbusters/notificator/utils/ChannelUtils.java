package dev.vality.fraudbusters.notificator.utils;

import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.exception.UnknownRecipientException;

public class ChannelUtils {

    public static String[] initRecipient(Channel channel) {
        String[] split = channel.getDestination().trim().split("\\s*,\\s*");
        if (split.length == 0) {
            throw new UnknownRecipientException("Unknown recipient or can't parse: " + channel.getDestination());
        }
        return split;
    }

}
