
function doAction(context) {

    // Obter as linhas selecionadas na grade (grade)
    var linhas = context.getLinhas();

    for (var i = 0; i < linhas.length; i++) {
        var linha = linhas[i];

        var nuFech = linha.getCampo("NUFECH");
        var sequencia = linha.getCampo("SEQUENCIA");

        if (nuFech && sequencia) {

            var jdbc = context.getJdbcWrapper();
            var sql = context.getNativeSql();

            sql.appendSql("SELECT DESLIGADO, DIASTRAB, REMFIXA FROM DBFECHCOMFIN WHERE SEQUENCIA = :SEQ AND NUFECH = :NUFECH");
            sql.setNamedParameter("SEQ", sequencia);
            sql.setNamedParameter("NUFECH", nuFech);

            var rs = sql.executeQuery();

            if (rs.next()) {
                var desligado = rs.getString("DESLIGADO");
                var diasTrab = rs.getBigDecimal("DIASTRAB");
                var remFixa = rs.getBigDecimal("REMFIXA");

                // Lógica Final da Procedure: (P_REMFIXA / 30) * P_DIASTRAB
                if (desligado == "SIM" && remFixa > 0) {

                    var vlrDiario = remFixa / 30;
                    var remFixa2 = vlrDiario * diasTrab;

                    // Executa o Update no Banco (Conforme Procedure SQL)
                    sql.clean();
                    sql.appendSql("UPDATE AD_DBFECHCOMFIN SET REMFIXA = :VAL WHERE SEQUENCIA = :SEQ AND NUFECH = :NUFECH");
                    sql.setNamedParameter("VAL", remFixa2);
                    sql.setNamedParameter("SEQ", sequencia);
                    sql.setNamedParameter("NUFECH", nuFech);
                    sql.executeUpdate();

                    // Atualiza na tela do usuário também
                    linha.setCampo("REMFIXA", remFixa2);
                }
            }
            rs.close();
            sql.releaseResources();
        }
    }

    context.setMensagemRetorno("Processamento JavaScript finalizado.");
}
