# 🛠️ SankhyaUtil - Utilitários e Ferramentas Sankhya

## 🎯 Visão Geral

Este documento apresenta uma análise completa do módulo `SankhyaUtil`, extraído do código fonte SankhyaW 4.8. O SankhyaUtil é uma biblioteca fundamental que fornece utilitários essenciais para desenvolvimento e personalização no ecossistema Sankhya.

## 📁 **Estrutura do SankhyaUtil**

### **🔧 Utilitários Principais**

#### **1. StringUtils.java**
**Localização**: `/SankhyaUtil/src/com/sankhya/util/StringUtils.java`

```java
// Principais funcionalidades do StringUtils
public class StringUtils {
    
    // Validação e manipulação de strings
    public static String getEmptyAsNull(String str);
    public static String getNullAsEmpty(String str);
    public static boolean isEmpty(String str);
    public static boolean isNotEmpty(String str);
    
    // Formatação de strings
    public static String formatString(String template, Object... params);
    public static String padLeft(String str, int length, char padChar);
    public static String padRight(String str, int length, char padChar);
    
    // Conversões
    public static String toCamelCase(String str);
    public static String toSnakeCase(String str);
    public static String capitalize(String str);
    
    // Validações
    public static boolean isNumeric(String str);
    public static boolean isEmail(String email);
    public static boolean isValidCpfCnpj(String documento);
}
```

#### **2. XMLUtils.java**
**Localização**: `/SankhyaUtil/src/com/sankhya/util/XMLUtils.java`

```java
// Utilitários para manipulação de XML
public class XMLUtils {
    
    // Criação e manipulação de documentos XML
    public static Document buildDocumentFromString(String xmlString);
    public static Element getRequiredChild(Element parent, String childName);
    public static String getRequiredAttributeAsString(Element element, String attrName);
    public static BigDecimal getAttributeAsBigDecimal(Element element, String attrName);
    
    // Validação XML
    public static boolean isValidXML(String xmlString);
    public static void validateXMLSchema(String xmlString, String schemaPath);
    
    // Transformação XML
    public static String transformXML(String xmlString, String xslPath);
    public static Document mergeXMLDocuments(Document doc1, Document doc2);
}
```

#### **3. SQLUtils.java**
**Localização**: `/SankhyaUtil/src/com/sankhya/util/SQLUtils.java`

```java
// Utilitários para SQL
public class SQLUtils {
    
    // Construção de consultas
    public static String buildSelectQuery(String table, String[] columns, String whereClause);
    public static String buildInsertQuery(String table, Map<String, Object> values);
    public static String buildUpdateQuery(String table, Map<String, Object> values, String whereClause);
    
    // Validação SQL
    public static boolean isValidSQL(String sql);
    public static String sanitizeSQL(String sql);
    public static String escapeSQLString(String value);
    
    // Execução de consultas
    public static ResultSet executeQuery(Connection conn, String sql, Object... params);
    public static int executeUpdate(Connection conn, String sql, Object... params);
    public static boolean executeBatch(Connection conn, List<String> sqlStatements);
}
```

#### **4. BigDecimalUtil.java**
**Localização**: `/SankhyaUtil/src/com/sankhya/util/BigDecimalUtil.java`

```java
// Utilitários para BigDecimal
public class BigDecimalUtil {
    
    // Operações matemáticas seguras
    public static BigDecimal add(BigDecimal a, BigDecimal b);
    public static BigDecimal subtract(BigDecimal a, BigDecimal b);
    public static BigDecimal multiply(BigDecimal a, BigDecimal b);
    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale);
    
    // Comparações
    public static boolean isZero(BigDecimal value);
    public static boolean isPositive(BigDecimal value);
    public static boolean isNegative(BigDecimal value);
    public static boolean equals(BigDecimal a, BigDecimal b);
    
    // Formatação
    public static String formatCurrency(BigDecimal value);
    public static String formatPercentage(BigDecimal value);
    public static BigDecimal parseCurrency(String currencyString);
}
```

### **🔐 Utilitários de Segurança**

#### **1. Crypter.java**
```java
// Utilitários de criptografia
public class Crypter {
    
    // Criptografia simétrica
    public static String encrypt(String plainText, String key);
    public static String decrypt(String encryptedText, String key);
    
    // Hash de senhas
    public static String hashPassword(String password);
    public static boolean verifyPassword(String password, String hash);
    
    // Geração de chaves
    public static String generateRandomKey(int length);
    public static String generateHash(String input);
}
```

#### **2. AuthorizationUtils.java**
```java
// Utilitários de autorização
public class AuthorizationUtils {
    
    // Verificação de permissões
    public static boolean hasPermission(BigDecimal userId, String resource);
    public static boolean hasRole(BigDecimal userId, String role);
    public static boolean canAccess(BigDecimal userId, String action, String resource);
    
    // Geração de tokens
    public static String generateAccessToken(BigDecimal userId);
    public static String generateRefreshToken(BigDecimal userId);
    public static boolean validateToken(String token);
}
```

### **📊 Utilitários de Dados**

#### **1. CollectionUtils.java**
```java
// Utilitários para coleções
public class CollectionUtils {
    
    // Manipulação de listas
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate);
    public static <T> List<T> map(List<T> list, Function<T, T> mapper);
    public static <T> List<T> distinct(List<T> list);
    public static <T> List<T> sort(List<T> list, Comparator<T> comparator);
    
    // Conversões
    public static <T> Set<T> listToSet(List<T> list);
    public static <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> keyMapper);
    public static <T> T[] listToArray(List<T> list, Class<T> clazz);
    
    // Validações
    public static boolean isEmpty(Collection<?> collection);
    public static boolean isNotEmpty(Collection<?> collection);
    public static boolean containsAny(Collection<?> collection, Object... items);
}
```

#### **2. MapUtils.java**
```java
// Utilitários para Map
public class MapUtils {
    
    // Manipulação de maps
    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2);
    public static <K, V> Map<K, V> filter(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate);
    public static <K, V> Map<K, V> sortByKey(Map<K, V> map);
    public static <K, V> Map<K, V> sortByValue(Map<K, V> map);
    
    // Conversões
    public static <K, V> List<V> mapToList(Map<K, V> map);
    public static <K, V> Set<K> mapToKeySet(Map<K, V> map);
    public static <K, V> Collection<V> mapToValues(Map<K, V> map);
    
    // Validações
    public static boolean isEmpty(Map<?, ?> map);
    public static boolean isNotEmpty(Map<?, ?> map);
    public static boolean containsKey(Map<?, ?> map, Object key);
    public static boolean containsValue(Map<?, ?> map, Object value);
}
```

### **📁 Utilitários de Arquivo**

#### **1. FileAndStreamUtils.java**
```java
// Utilitários para arquivos e streams
public class FileAndStreamUtils {
    
    // Operações de arquivo
    public static byte[] readFileToByteArray(File file);
    public static String readFileToString(File file, String encoding);
    public static void writeStringToFile(File file, String content, String encoding);
    public static void writeByteArrayToFile(File file, byte[] data);
    
    // Operações de stream
    public static void copyStream(InputStream input, OutputStream output);
    public static byte[] streamToByteArray(InputStream input);
    public static String streamToString(InputStream input, String encoding);
    
    // Validações de arquivo
    public static boolean isValidFile(File file);
    public static boolean isValidImage(File file);
    public static boolean isValidPdf(File file);
    public static long getFileSize(File file);
}
```

#### **2. ZipUtils.java**
```java
// Utilitários para ZIP
public class ZipUtils {
    
    // Criação de arquivos ZIP
    public static void createZipFile(String zipPath, List<File> files);
    public static void createZipFile(String zipPath, Map<String, byte[]> fileContents);
    public static void addFileToZip(String zipPath, String fileName, byte[] content);
    
    // Extração de arquivos ZIP
    public static List<File> extractZipFile(String zipPath, String destPath);
    public static Map<String, byte[]> extractZipToMemory(String zipPath);
    public static byte[] extractFileFromZip(String zipPath, String fileName);
    
    // Validação ZIP
    public static boolean isValidZipFile(String zipPath);
    public static List<String> getZipFileList(String zipPath);
}
```

### **🔍 Validadores**

#### **1. ValidadorCpfCnpj.java**
```java
// Validador de CPF/CNPJ
public class ValidadorCpfCnpj {
    
    // Validação de CPF
    public static boolean isValidCpf(String cpf);
    public static String formatCpf(String cpf);
    public static String removeCpfMask(String cpf);
    
    // Validação de CNPJ
    public static boolean isValidCnpj(String cnpj);
    public static String formatCnpj(String cnpj);
    public static String removeCnpjMask(String cnpj);
    
    // Validação genérica
    public static boolean isValidCpfCnpj(String documento);
    public static String formatCpfCnpj(String documento);
    public static String removeMask(String documento);
}
```

#### **2. ValidadorEmail.java**
```java
// Validador de email
public class ValidadorEmail {
    
    // Validação básica
    public static boolean isValidEmail(String email);
    public static boolean isValidEmailFormat(String email);
    public static boolean isValidEmailDomain(String email);
    
    // Validação avançada
    public static boolean isValidEmailWithMX(String email);
    public static List<String> extractEmailsFromText(String text);
    public static String normalizeEmail(String email);
}
```

### **🎨 Utilitários de Interface**

#### **1. BarcodeUtil.java**
```java
// Utilitários para códigos de barras
public class BarcodeUtil {
    
    // Geração de códigos de barras
    public static byte[] generateBarcode(String content, BarcodeType type);
    public static Image generateBarcodeImage(String content, BarcodeType type);
    public static String generateBarcodeBase64(String content, BarcodeType type);
    
    // Validação de códigos
    public static boolean isValidBarcode(String barcode, BarcodeType type);
    public static BarcodeType detectBarcodeType(String barcode);
    
    // Leitura de códigos
    public static String readBarcodeFromImage(Image image);
    public static String readBarcodeFromFile(File file);
}
```

#### **2. QRcodeUtil.java**
```java
// Utilitários para QR Code
public class QRcodeUtil {
    
    // Geração de QR Code
    public static byte[] generateQRCode(String content);
    public static Image generateQRCodeImage(String content);
    public static String generateQRCodeBase64(String content);
    
    // Configuração de QR Code
    public static byte[] generateQRCode(String content, int size, ErrorCorrectionLevel level);
    public static Image generateQRCodeImage(String content, int size, ErrorCorrectionLevel level);
    
    // Leitura de QR Code
    public static String readQRCodeFromImage(Image image);
    public static String readQRCodeFromFile(File file);
}
```

## 🚀 **Exemplos Práticos de Uso**

### **1. Manipulação de Strings**
```java
// Exemplo de uso do StringUtils
public class ExemploStringUtils {
    
    public void processarDados() {
        String nome = "  João Silva  ";
        
        // Limpar e formatar string
        String nomeLimpo = StringUtils.getEmptyAsNull(nome.trim());
        String nomeFormatado = StringUtils.capitalize(nomeLimpo);
        
        // Validar dados
        if (StringUtils.isNotEmpty(nomeFormatado)) {
            System.out.println("Nome processado: " + nomeFormatado);
        }
        
        // Formatação de template
        String template = "Bem-vindo, {0}! Sua conta foi criada em {1}";
        String mensagem = StringUtils.formatString(template, nomeFormatado, new Date());
    }
}
```

### **2. Manipulação de XML**
```java
// Exemplo de uso do XMLUtils
public class ExemploXMLUtils {
    
    public void processarXML() throws Exception {
        String xmlString = "<root><user id=\"1\">João</user></root>";
        
        // Parse do XML
        Document doc = XMLUtils.buildDocumentFromString(xmlString);
        Element root = doc.getRootElement();
        
        // Obter dados
        Element user = XMLUtils.getRequiredChild(root, "user");
        String id = XMLUtils.getRequiredAttributeAsString(user, "id");
        String nome = user.getText();
        
        // Validação
        if (XMLUtils.isValidXML(xmlString)) {
            System.out.println("XML válido - ID: " + id + ", Nome: " + nome);
        }
    }
}
```

### **3. Operações com BigDecimal**
```java
// Exemplo de uso do BigDecimalUtil
public class ExemploBigDecimalUtil {
    
    public void calcularValores() {
        BigDecimal preco = new BigDecimal("100.50");
        BigDecimal desconto = new BigDecimal("10.00");
        BigDecimal taxa = new BigDecimal("0.10");
        
        // Cálculos seguros
        BigDecimal precoComDesconto = BigDecimalUtil.subtract(preco, desconto);
        BigDecimal valorComTaxa = BigDecimalUtil.multiply(precoComDesconto, BigDecimalUtil.add(BigDecimal.ONE, taxa));
        
        // Formatação
        String valorFormatado = BigDecimalUtil.formatCurrency(valorComTaxa);
        System.out.println("Valor final: " + valorFormatado);
        
        // Validações
        if (BigDecimalUtil.isPositive(valorComTaxa)) {
            System.out.println("Valor é positivo");
        }
    }
}
```

### **4. Validação de Dados**
```java
// Exemplo de uso dos validadores
public class ExemploValidadores {
    
    public void validarDados() {
        String cpf = "123.456.789-00";
        String cnpj = "12.345.678/0001-90";
        String email = "usuario@exemplo.com";
        
        // Validar CPF
        if (ValidadorCpfCnpj.isValidCpf(cpf)) {
            System.out.println("CPF válido: " + ValidadorCpfCnpj.formatCpf(cpf));
        }
        
        // Validar CNPJ
        if (ValidadorCpfCnpj.isValidCnpj(cnpj)) {
            System.out.println("CNPJ válido: " + ValidadorCpfCnpj.formatCnpj(cnpj));
        }
        
        // Validar email
        if (ValidadorEmail.isValidEmail(email)) {
            System.out.println("Email válido: " + email);
        }
    }
}
```

## 🎯 **Boas Práticas**

### **1. Uso de Utilitários**
- **Sempre use os utilitários**: Em vez de reimplementar funcionalidades
- **Tratamento de exceções**: Sempre trate exceções ao usar utilitários
- **Validação de entrada**: Valide dados antes de usar utilitários
- **Performance**: Use utilitários otimizados para operações frequentes

### **2. Manipulação de Dados**
- **BigDecimal para valores monetários**: Use BigDecimalUtil para cálculos
- **Validação de strings**: Use StringUtils para validações
- **Formatação consistente**: Use utilitários de formatação
- **Sanitização**: Sempre sanitize dados de entrada

### **3. Segurança**
- **Criptografia**: Use Crypter para dados sensíveis
- **Autorização**: Use AuthorizationUtils para controle de acesso
- **Validação de entrada**: Valide todos os dados de entrada
- **Sanitização SQL**: Use SQLUtils para prevenir SQL injection

## 🎊 **Conclusão**

O SankhyaUtil é uma biblioteca fundamental que fornece:

- **✅ Utilitários Essenciais**: String, XML, SQL, BigDecimal
- **✅ Segurança**: Criptografia, autorização, validação
- **✅ Manipulação de Dados**: Collections, Maps, Files
- **✅ Validadores**: CPF/CNPJ, Email, Códigos de barras
- **✅ Interface**: QR Code, Barcode, Formatação

### **Benefícios:**
- **Produtividade**: Reduz tempo de desenvolvimento
- **Confiabilidade**: Utilitários testados e otimizados
- **Padronização**: Uso consistente em todo o sistema
- **Manutenibilidade**: Código mais limpo e organizado

---

*Este documento foi criado com base na análise completa do código fonte SankhyaUtil do SankhyaW 4.8.*
