package dev.vality.fraudbusters.notificator.dao;


import dev.vality.fraudbusters.notificator.dao.domain.enums.ChannelType;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;

import java.util.List;

public interface ChannelDao {

    Channel insert(Channel channel);

    void remove(String name);

    Channel getByName(String name);

    List<Channel> getAll(FilterDto filter);

    List<ChannelType> getAllTypes();

}
