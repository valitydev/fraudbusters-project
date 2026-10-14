package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.NotificationServiceSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@WebServlet("/notification/v1")
@RequiredArgsConstructor
public class NotificationServlet extends GenericServlet {

    private final NotificationServiceSrv.Iface notificationHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(NotificationServiceSrv.Iface.class, notificationHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
