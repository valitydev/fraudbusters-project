package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.NotificationTemplateServiceSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@WebServlet("/notification-template/v1")
@RequiredArgsConstructor
public class NotificationTemplateServlet extends GenericServlet {

    private final NotificationTemplateServiceSrv.Iface notificationTemplateHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(NotificationTemplateServiceSrv.Iface.class, notificationTemplateHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
