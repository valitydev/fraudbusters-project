package dev.vality.fraudbusters.management.resource.notificator.converter;

import dev.vality.damsel.fraudbusters_notificator.ChannelType;
import dev.vality.swag.fraudbusters.management.model.Channel;
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
            result.setType(Channel.TypeEnum.fromValue(channel.getType().name()));
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
            result.setType(ChannelType.valueOf(channel.getType().getValue()));
        }
        if (Objects.nonNull(channel.getCreatedAt())) {
            result.setCreatedAt(channel.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return result;
    }
}
