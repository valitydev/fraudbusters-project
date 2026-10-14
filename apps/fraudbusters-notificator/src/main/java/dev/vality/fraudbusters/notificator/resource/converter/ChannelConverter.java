package dev.vality.fraudbusters.notificator.resource.converter;

import dev.vality.fraudbusters.notificator.dao.domain.enums.ChannelType;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class ChannelConverter
        implements BiConverter<dev.vality.damsel.fraudbusters_notificator.Channel, Channel> {

    @Override
    public Channel toTarget(dev.vality.damsel.fraudbusters_notificator.Channel channel) {
        if (Objects.isNull(channel)) {
            return null;
        }
        Channel result = new Channel();
        result.setName(channel.getName());
        result.setDestination(channel.getDestination());
        if (channel.isSetType()) {
            result.setType(ChannelType.valueOf(channel.getType().name()));
        }
        if (channel.isSetCreatedAt()) {
            result.setCreatedAt(LocalDateTime.parse(channel.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME));
        }
        return result;
    }

    @Override
    public dev.vality.damsel.fraudbusters_notificator.Channel toSource(Channel channel) {
        if (Objects.isNull(channel)) {
            return null;
        }
        var result = new dev.vality.damsel.fraudbusters_notificator.Channel();
        result.setName(channel.getName());
        result.setDestination(channel.getDestination());
        if (Objects.nonNull(channel.getType())) {
            result.setType(
                    dev.vality.damsel.fraudbusters_notificator.ChannelType.valueOf(channel.getType().getLiteral()));
        }
        if (Objects.nonNull(channel.getCreatedAt())) {
            result.setCreatedAt(channel.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return result;
    }
}
