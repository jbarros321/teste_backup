# 📦 Instalação - Dashboard Financeiro CICRANO

## 🚀 Passo a Passo para Instalação no Sankhya

### 1. Preparar o Projeto

#### 1.1. Adicionar JARs do Sankhya
```bash
cd Dash/lib
# Copiar os seguintes JARs do SDK Sankhya:
# - SankhyaW-extensions.jar
# - jape.jar
# - mge-modelcore.jar
# - sanutil.jar
# - sanws.jar
```

#### 1.2. Compilar o Projeto
```bash
cd Dash
./build.sh
# ou
mvn clean package install
```

### 2. Instalar no Sankhya

#### 2.1. Copiar JAR
Copie o JAR gerado para a pasta de extensões do Sankhya:
```
target/personalizacao-dash-1.0.0.jar → [Sankhya]/extensions/
```

#### 2.2. Configurar Servlet no web.xml

Localize o arquivo `web.xml` do Sankhya (geralmente em `[Sankhya]/web/WEB-INF/web.xml`) e adicione:

```xml
<servlet>
    <servlet-name>DashboardServlet</servlet-name>
    <servlet-class>br.com.cicrano.dash.servlet.DashboardServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>DashboardServlet</servlet-name>
    <url-pattern>/dash/*</url-pattern>
</servlet-mapping>
```

**Importante**: Adicione ANTES do fechamento da tag `</web-app>`.

#### 2.3. Copiar JSP

Copie o arquivo JSP para a pasta web do Sankhya:
```
Dash/web/dashboard.jsp → [Sankhya]/web/dash/dashboard.jsp
```

**Nota**: Crie a pasta `dash` dentro de `web` se não existir.

### 3. Reiniciar o Sankhya

Após as alterações, reinicie o servidor Sankhya para carregar as novas configurações.

### 4. Acessar o Dashboard

Abra o navegador e acesse:
```
http://seu-servidor-sankhya:porta/dash/dashboard.jsp
```

**Exemplo**:
- `http://localhost:8080/dash/dashboard.jsp`
- `http://192.168.1.100:8080/dash/dashboard.jsp`

## ✅ Verificação

Após a instalação, o dashboard deve:
1. ✅ Carregar automaticamente ao abrir a página
2. ✅ Exibir dados do último mês por padrão
3. ✅ Mostrar gráficos e tabelas com dados reais
4. ✅ Permitir filtrar por período

## 🔧 Troubleshooting

### Erro 404 - Página não encontrada
- Verifique se o JSP está em `[Sankhya]/web/dash/dashboard.jsp`
- Verifique se o servlet está configurado corretamente no `web.xml`
- Verifique se o servidor foi reiniciado

### Erro 500 - Erro interno do servidor
- Verifique se o JAR está na pasta `extensions/`
- Verifique os logs do Sankhya para detalhes do erro
- Verifique se os JARs do Sankhya estão corretos

### Dashboard não carrega dados
- Verifique se há dados na tabela TGFFIN no período selecionado
- Verifique se o usuário tem permissão para acessar os dados
- Abra o console do navegador (F12) para ver erros JavaScript

### Gráficos não aparecem
- Verifique a conexão com a internet (Chart.js é carregado via CDN)
- Verifique se há dados para exibir no período selecionado

## 📝 Notas Importantes

1. **JSP Standalone**: O JSP é totalmente standalone e carrega os dados automaticamente ao abrir
2. **Servlet para Dados**: O servlet serve apenas os dados JSON, não o HTML
3. **URL Relativa**: O JSP usa URLs relativas, funcionando em qualquer contexto do Sankhya
4. **Carregamento Automático**: Os dados são carregados automaticamente ao abrir a página

## 🎯 Estrutura Final no Sankhya

```
[Sankhya]/
├── extensions/
│   └── personalizacao-dash-1.0.0.jar
├── web/
│   ├── dash/
│   │   └── dashboard.jsp
│   └── WEB-INF/
│       └── web.xml (modificado)
```

## 📞 Suporte

Em caso de problemas, verifique:
1. Logs do Sankhya
2. Console do navegador (F12)
3. Documentação do README.md











