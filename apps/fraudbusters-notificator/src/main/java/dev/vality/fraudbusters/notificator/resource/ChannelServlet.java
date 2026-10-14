package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.ChannelServiceSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@WebServlet("/notification-channel/v1")
@RequiredArgsConstructor
public class ChannelServlet extends GenericServlet {

    private final ChannelServiceSrv.Iface channelHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(ChannelServiceSrv.Iface.class, channelHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
