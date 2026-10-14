package dev.vality.fraudbusters.management.service.iface;

import dev.vality.damsel.fraudbusters_notificator.*;

public interface ChannelService {

    Channel create(Channel channel);

    void delete(String name);

    ChannelListResponse getAll(Page page, Filter filter);

    ChannelTypeListResponse getAllTypes();

    Channel getById(String name);

}
