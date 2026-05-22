package cl.municipalidad.reservas.filter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewayCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String gatewaySecret = req.getHeader("X-Gateway-Secret");

        if ("ClaveUltraSecretaEInviolableParaLaMunicipalidad2026!".equals(gatewaySecret)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("Acceso denegado: Por favor ingrese a traves del Gateway municipal.");
        }
    }
}