package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.*;
import dev.vality.fraudbusters.notificator.dao.ChannelDao;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ChannelType;
import dev.vality.fraudbusters.notificator.resource.converter.ChannelConverter;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelHandler implements ChannelServiceSrv.Iface {

    private final ChannelDao channelDao;
    private final ChannelConverter channelConverter;
    private final FilterConverter filterConverter;

    @Override
    public Channel create(Channel channel) {
        var savedChannel = channelDao.insert(channelConverter.toTarget(channel));
        log.info("ChannelHandler create channel: {}", savedChannel);
        return channelConverter.toSource(savedChannel);
    }

    @Override
    public void remove(String name) {
        channelDao.remove(name);
        log.info("ChannelHandler delete channel with name: {}", name);
    }

    @Override
    public ChannelListResponse getAll(Page page, Filter filter) {
        FilterDto filterDto = filterConverter.convert(page, filter);
        var channels = channelDao.getAll(filterDto);
        log.info("ChannelHandler get all channels: {}", channels);
        List<Channel> result = channels.stream()
                .map(channelConverter::toSource)
                .collect(Collectors.toList());
        ChannelListResponse channelListResponse = new ChannelListResponse()
                .setChannels(result);
        if (channels.size() == filterDto.getSize()) {
            var lastChannel = channels.get(channels.size() - 1);
            channelListResponse.setContinuationId(lastChannel.getName());
        }
        return channelListResponse;

    }

    @Override
    public ChannelTypeListResponse getAllTypes() {
        List<ChannelType> types = channelDao.getAllTypes();
        List<String> result = types.stream()
                .map(ChannelType::getLiteral)
                .collect(Collectors.toList());
        log.info("ChannelHandler get all channel types: {}", result);
        return new ChannelTypeListResponse()
                .setChannelTypes(result);
    }

    @Override
    public Channel getById(String name) {
        var channel = channelDao.getByName(name);
        log.info("ChannelHandler get channel by id {} channel: {}", name, channel);
        return channelConverter.toSource(channel);
    }
}
