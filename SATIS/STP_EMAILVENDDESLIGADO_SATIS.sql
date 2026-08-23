CREATE OR REPLACE PROCEDURE "STP_EMAILVENDDESLIGADO_SATIS" (
       P_CODUSU NUMBER,        -- Código do usuário logado
       P_IDSESSAO VARCHAR2,    -- Identificador da execução. Serve para buscar informações dos parâmetros/campos da execução.
       P_QTDLINHAS NUMBER,     -- Informa a quantidade de registros selecionados no momento da execução.
       P_MENSAGEM OUT VARCHAR2 -- Caso seja passada uma mensagem aqui, ela será exibida como uma informação ao usuário.
) AS
       FIELD_NUFECH         NUMBER;
       FIELD_SEQUENCIA      NUMBER;
       P_EMAIL              VARCHAR2(100);
       P_VALOR              NUMBER;
       P_REMFIXA            NUMBER;
       P_REEMBOLSO          NUMBER;
       P_ADIANTCOMISSAO     NUMBER;
       P_EXTRAS             NUMBER;
       P_DESLIGADO          VARCHAR2(100);
       P_CODFILA            NUMBER;

BEGIN


       FOR I IN 1..P_QTDLINHAS -- Este loop permite obter o valor de campos dos registros envolvidos na execução.
       LOOP                    -- A variável "I" representa o registro corrente.
           -- Para obter o valor dos campos utilize uma das seguintes funções:


           FIELD_NUFECH := ACT_INT_FIELD(P_IDSESSAO, I, 'NUFECH');
           FIELD_SEQUENCIA := ACT_INT_FIELD(P_IDSESSAO, I, 'SEQUENCIA');


    SELECT 
        VEN.AD_EMAILPART,
        ROUND(NVL(FIN.VALOR, 0) - NVL(FIN.VLRADTCOMP, 0),2) AS VALOR,
        NVL(FIN.REMFIXA, 0) AS REMFIXA,
        INITCAP(VEN.APELIDO) AS APELIDO,
        VEN.CODVEND,
        NVL(FIN.REEMBOLSO, 0) AS REEMBOLSO,
        NVL(FIN.ADIANTAMENTO, 0)+(NVL(FIN.ADIANTFIXO,0)) AS ADIANTCOMISSAO,
        NVL(FIN.EXTRA, 0) AS EXTRAS,
        DESLIGADO

    INTO P_EMAIL, P_VALOR, P_REMFIXA, P_APELIDO, P_CODVEND, P_REEMBOLSO, P_ADIANTCOMISSAO, P_EXTRAS,P_DESLIGADO

    FROM 
        AD_DBFECHCOMFIN FIN
    INNER JOIN 
        TGFVEN VEN ON FIN.CODVEND = VEN.CODVEND
    WHERE 
        FIN.NUFECH = FIELD_NUFECH AND
        FIN.SEQUENCIA = FIELD_SEQUENCIA;


   IF P_DESLIGADO = 'NÃO' THEN 

         RAISE_APPLICATION_ERROR(-20101, FC_FORMATAHTML('Email não enviado para Vendedor: '||P_APELIDO,
                                               'Vendedor Não esta Desligado para que se gere o E-mail de Desligamento',
                                               'Selecione um vendedor com o Desligado marcado como SIM "'));       


   END IF;

   
     IF P_DESLIGADO = 'SIM' THEN 


            P_ASSUNTO := 'Faturamento '||P_APELIDO||',';

            P_CORPO := 
                'Prezado(a) '||P_APELIDO||',<br /><br />' ||
                'Esperamos que esta mensagem lhe encontre bem!<br /><br />' ||
                'Conforme o encerramento do contrato de prestação de serviços firmado com a Satis, encaminhamos, com a devida consideração, o detalhamento dos valores referentes ao seu acerto.<br /><br />' ||
                'Detalhamento dos valores:<br /><br />' ||
                '&bull; Ajuda de custo: R$ ' || TO_CHAR(P_REMFIXA, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS='',.''') || '<br />' ||
                '&bull; Comissão do mês: R$ ' ||TO_CHAR(P_VALOR, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS ='',.''') || '<br />' ||
                '&bull; Comissão futura: R$ [valor]<br />' ||
                '&bull; Outros valores: R$ ' ||TO_CHAR(NVL(P_EXTRAS, 0) + NVL(P_REEMBOLSO, 0), 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS ='',.''') || '<br />' ||
                '&bull; Valor a compensar: R$ ' ||TO_CHAR(P_ADIANTCOMISSAO, 'FM999G999G990D00', 'NLS_NUMERIC_CHARACTERS ='',.''') || '<br /><br />' ||
                'Valor líquido a receber: R$ [valor]<br /><br />' ||
                'Informamos que o pagamento será realizado no prazo de 10 (dez) dias, contados a partir do recebimento da respectiva Nota Fiscal.<br /><br />' ||
                'Caso haja qualquer dúvida ou necessidade de esclarecimentos adicionais, permanecemos inteiramente à disposição para auxiliá-lo(a).<br /><br />' ||
                'Aproveitamos a oportunidade para agradecer, de forma sincera, pela dedicação e contribuição durante o período em que esteve conosco. Desejamos êxito e realizações em seus próximos desafios profissionais.'; 

                SELECT NVL(MAX(CODFILA),0)INTO P_CODFILA FROM TMDFMG;
    
                INSERT INTO TMDFMG (CODFILA, ASSUNTO, CODMSG, DTENTRADA, STATUS, CODCON, TENTENVIO, MENSAGEM, TIPOENVIO, MAXTENTENVIO, EMAIL, NUANEXO, MIMETYPE, CODSMTP) 
                VALUES( P_CODFILA+1, P_ASSUNTO, NULL, SYSDATE, 'Pendente', 0, 0, P_CORPO, 'E', 3, P_EMAIL, NULL, NULL, 33 );
                COMMIT;

                UPDATE 
                    AD_DBFECHCOMFIN 
                SET 
                    EMAILVEND = 'S' 
                WHERE 
                NUFECH = FIELD_NUFECH AND
                SEQUENCIA = FIELD_SEQUENCIA;

                UPDATE 
                    AD_DBFECHCOMFIN 
                SET 
                    CODFILA = P_CODFILA 
                WHERE 
                NUFECH = FIELD_NUFECH AND
                SEQUENCIA = FIELD_SEQUENCIA;  


                -- Mensagem de Sucesso para dizer que o Vendedor Recebeu o E-mail    
                P_MENSAGEM := 'Email enviado para vendedor com Sucesso!';
    
   END IF; 

    END LOOP;


END;

/
