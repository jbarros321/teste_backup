package br.com.cicrano.dash.servlet;

import br.com.cicrano.dash.dto.FluxoCaixaDTO;
import br.com.cicrano.dash.dto.ProvisaoDTO;
import br.com.cicrano.dash.repository.FinanceiroRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DashboardServlet extends HttpServlet {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat JSON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private final FinanceiroRepository repository = new FinanceiroRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("data".equals(action)) {
            processarDados(request, response);
        } else {
            String path = request.getRequestURI();
            if (path.endsWith("/") || path.endsWith("/dash")) {
                response.sendRedirect(request.getContextPath() + "/dash/dashboard.jsp");
            } else {
                processarDados(request, response);
            }
        }
    }

    private void processarDados(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Date dataIni = parseDate(request.getParameter("dataIni"));
            Date dataFim = parseDate(request.getParameter("dataFim"));
            
            if (dataIni == null || dataFim == null) {
                dataIni = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
                dataFim = new Date();
            }
            
            List<FluxoCaixaDTO> fluxoCaixa = repository.buscarFluxoCaixaReal(dataIni, dataFim);
            List<ProvisaoDTO> provisaoReceita = repository.buscarProvisaoReceita(dataIni, dataFim);
            List<ProvisaoDTO> provisaoDespesa = repository.buscarProvisaoDespesa(dataIni, dataFim);
            
            PrintWriter out = response.getWriter();
            out.print("{");
            out.print("\"fluxoCaixa\":");
            out.print(toJsonFluxoCaixa(fluxoCaixa));
            out.print(",\"provisaoReceita\":");
            out.print(toJsonProvisao(provisaoReceita));
            out.print(",\"provisaoDespesa\":");
            out.print(toJsonProvisao(provisaoDespesa));
            out.print("}");
            out.flush();
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"erro\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            out.flush();
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private String toJsonFluxoCaixa(List<FluxoCaixaDTO> lista) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("[");
        for (int i = 0; i < lista.size(); i++) {
            FluxoCaixaDTO dto = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"data\":\"").append(JSON_DATE_FORMAT.format(dto.getData())).append("\",");
            sb.append("\"receitas\":").append(dto.getReceitas()).append(",");
            sb.append("\"despesas\":").append(dto.getDespesas()).append(",");
            sb.append("\"saldo\":").append(dto.getSaldo());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonProvisao(List<ProvisaoDTO> lista) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("[");
        for (int i = 0; i < lista.size(); i++) {
            ProvisaoDTO dto = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"data\":\"").append(JSON_DATE_FORMAT.format(dto.getData())).append("\",");
            sb.append("\"valor\":").append(dto.getValor());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}

