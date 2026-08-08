# ⚙️ Configuração de Ambiente Sankhya - Guia Completo

## 🎯 **Setup Completo do Ambiente de Desenvolvimento**

Este guia apresenta a configuração completa do ambiente de desenvolvimento Sankhya, incluindo todas as ferramentas e dependências necessárias.

## 🛠️ **Pré-requisitos**

### **Sistema Operacional**
- **Linux**: Ubuntu 20.04+ (recomendado)
- **Windows**: Windows 10/11
- **macOS**: macOS 10.15+

### **Requisitos Mínimos**
- **RAM**: 8GB (recomendado 16GB)
- **Disco**: 20GB livres
- **Processador**: Dual-core 2.0GHz+

## 📦 **Instalação das Ferramentas**

### **1. Java Development Kit (JDK)**

#### **Linux (Ubuntu/Debian)**
```bash
# Atualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar JDK 11
sudo apt install openjdk-11-jdk -y

# Verificar instalação
java -version
javac -version

# Configurar variáveis de ambiente
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verificar configuração
echo $JAVA_HOME
```

#### **Windows**
```powershell
# Download do JDK 11
# Acesse: https://adoptium.net/
# Baixe: OpenJDK 11 (LTS)

# Instalar e configurar variáveis de ambiente
# JAVA_HOME: C:\Program Files\Eclipse Adoptium\jdk-11.0.x-hotspot
# PATH: %JAVA_HOME%\bin

# Verificar instalação
java -version
javac -version
```

#### **macOS**
```bash
# Instalar via Homebrew
brew install openjdk@11

# Configurar variáveis de ambiente
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@11' >> ~/.zshrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.zshrc
source ~/.zshrc

# Verificar instalação
java -version
javac -version
```

### **2. Eclipse IDE**

#### **Download e Instalação**
```bash
# Criar diretório de desenvolvimento
mkdir -p ~/sankhya-development
cd ~/sankhya-development

# Download do Eclipse
wget https://download.eclipse.org/eclipse/downloads/drops4/R-4.25-202206291800/eclipse-SDK-4.25-linux-gtk-x86_64.tar.gz

# Extrair Eclipse
tar -xzf eclipse-SDK-4.25-linux-gtk-x86_64.tar.gz

# Criar workspace Sankhya
mkdir -p ~/sankhya-workspace

# Executar Eclipse
./eclipse/eclipse -data ~/sankhya-workspace
```

#### **Configuração do Eclipse**
```bash
# Configurar workspace
# File → Switch Workspace → Other
# Selecionar: ~/sankhya-workspace

# Instalar plugins necessários
# Help → Eclipse Marketplace
# Buscar e instalar:
# - Maven Integration for Eclipse
# - Spring Tools 4
# - Git Integration for Eclipse
```

### **3. Maven**

#### **Instalação**
```bash
# Download do Maven
wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz

# Extrair Maven
tar -xzf apache-maven-3.8.6-bin.tar.gz
sudo mv apache-maven-3.8.6 /opt/maven

# Configurar variáveis de ambiente
echo 'export MAVEN_HOME=/opt/maven' >> ~/.bashrc
echo 'export PATH=$PATH:$MAVEN_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verificar instalação
mvn -version
```

#### **Configuração do Maven**
```xml
<!-- ~/.m2/settings.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <localRepository>${user.home}/.m2/repository</localRepository>
    
    <servers>
        <server>
            <id>sankhya-repo</id>
            <username>seu-usuario</username>
            <password>sua-senha</password>
        </server>
    </servers>
    
    <repositories>
        <repository>
            <id>sankhya-repo</id>
            <url>https://developer.sankhya.com.br/maven/repository</url>
        </repository>
    </repositories>
    
    <pluginRepositories>
        <pluginRepository>
            <id>sankhya-repo</id>
            <url>https://developer.sankhya.com.br/maven/repository</url>
        </pluginRepository>
    </pluginRepositories>
</settings>
```

### **4. Git**

#### **Instalação e Configuração**
```bash
# Instalar Git
sudo apt install git -y

# Configurar Git
git config --global user.name "Seu Nome"
git config --global user.email "seu.email@exemplo.com"

# Verificar configuração
git config --list
```

## 🚀 **SDK Sankhya**

### **Download e Instalação**
```bash
# Criar diretório SDK
mkdir -p ~/sankhya-sdk
cd ~/sankhya-sdk

# Download do SDK Sankhya
wget https://developer.sankhya.com.br/downloads/sdk-sankhya-latest.zip

# Extrair SDK
unzip sdk-sankhya-latest.zip

# Configurar variáveis de ambiente
echo 'export SANKHYA_HOME=~/sankhya-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$SANKHYA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verificar instalação
sankhya-sdk --version
```

### **Configuração do SDK**
```bash
# Inicializar configuração
sankhya-sdk init

# Configurar servidor
sankhya-sdk config server https://api.sankhya.com.br

# Configurar credenciais
sankhya-sdk config auth --username seu-usuario --password sua-senha

# Verificar configuração
sankhya-sdk config list
```

## 📁 **Estrutura de Projeto**

### **Criar Primeiro Projeto**
```bash
# Criar projeto Sankhya
sankhya-sdk create-project --name="meu-primeiro-projeto" --type="customization"

# Navegar para o projeto
cd meu-primeiro-projeto

# Estrutura criada
tree -L 3
```

### **Estrutura de Diretórios**
```
meu-primeiro-projeto/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/
│   │   │       └── com/
│   │   │           └── empresa/
│   │   │               └── sankhya/
│   │   │                   ├── eventos/
│   │   │                   ├── validacoes/
│   │   │                   ├── integracoes/
│   │   │                   └── utils/
│   │   ├── resources/
│   │   │   ├── sql/
│   │   │   ├── xml/
│   │   │   ├── properties/
│   │   │   └── logs/
│   │   └── webapp/
│   │       ├── js/
│   │       ├── css/
│   │       ├── images/
│   │       └── jsp/
│   └── test/
│       ├── java/
│       └── resources/
├── lib/
├── scripts/
├── config/
├── logs/
├── README.md
├── pom.xml
└── .gitignore
```

## ⚙️ **Configuração do Projeto**

### **pom.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>br.com.empresa</groupId>
    <artifactId>meu-primeiro-projeto</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <name>Meu Primeiro Projeto Sankhya</name>
    <description>Projeto de personalização Sankhya</description>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <sankhya.version>1.0.0</sankhya.version>
    </properties>
    
    <dependencies>
        <!-- Sankhya Core -->
        <dependency>
            <groupId>br.com.sankhya</groupId>
            <artifactId>sankhya-core</artifactId>
            <version>${sankhya.version}</version>
        </dependency>
        
        <!-- Sankhya JAPE -->
        <dependency>
            <groupId>br.com.sankhya</groupId>
            <artifactId>sankhya-jape</artifactId>
            <version>${sankhya.version}</version>
        </dependency>
        
        <!-- Sankhya Util -->
        <dependency>
            <groupId>br.com.sankhya</groupId>
            <artifactId>sankhya-util</artifactId>
            <version>${sankhya.version}</version>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
            </plugin>
        </plugins>
    </build>
    
    <repositories>
        <repository>
            <id>sankhya-repo</id>
            <url>https://developer.sankhya.com.br/maven/repository</url>
        </repository>
    </repositories>
</project>
```

### **Configuração de Logs**
```properties
# src/main/resources/log4j.properties
log4j.rootLogger=INFO, console, file

# Console Appender
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c{1} - %m%n

# File Appender
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=logs/sankhya.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=5
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c{1} - %m%n

# Package specific logging
log4j.logger.br.com.empresa.sankhya=DEBUG
```

## 🔧 **Configuração do Banco de Dados**

### **Oracle Database**
```bash
# Instalar Oracle Instant Client
wget https://download.oracle.com/otn_software/linux/instantclient/2110000/oracle-instantclient-basic-21.10.0.0.0-1.x86_64.rpm
wget https://download.oracle.com/otn_software/linux/instantclient/2110000/oracle-instantclient-devel-21.10.0.0.0-1.x86_64.rpm

# Instalar pacotes
sudo rpm -ivh oracle-instantclient-basic-21.10.0.0.0-1.x86_64.rpm
sudo rpm -ivh oracle-instantclient-devel-21.10.0.0.0-1.x86_64.rpm

# Configurar variáveis de ambiente
echo 'export ORACLE_HOME=/usr/lib/oracle/21/client64' >> ~/.bashrc
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$ORACLE_HOME/lib' >> ~/.bashrc
echo 'export PATH=$PATH:$ORACLE_HOME/bin' >> ~/.bashrc
source ~/.bashrc
```

### **Configuração de Conexão**
```properties
# src/main/resources/database.properties
# Configuração do banco de dados
db.driver=oracle.jdbc.driver.OracleDriver
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.username=seu_usuario
db.password=sua_senha
db.pool.initialSize=5
db.pool.maxActive=20
db.pool.maxIdle=10
db.pool.minIdle=5
```

## 🧪 **Teste da Configuração**

### **Script de Teste**
```bash
#!/bin/bash
# test-environment.sh

echo "🧪 Testando Configuração do Ambiente Sankhya"
echo "============================================="

# Teste 1: Java
echo "☕ Testando Java..."
if java -version > /dev/null 2>&1; then
    echo "   ✅ Java instalado"
    java -version | head -1
else
    echo "   ❌ Java não encontrado"
    exit 1
fi

# Teste 2: Maven
echo "📦 Testando Maven..."
if mvn -version > /dev/null 2>&1; then
    echo "   ✅ Maven instalado"
    mvn -version | head -1
else
    echo "   ❌ Maven não encontrado"
    exit 1
fi

# Teste 3: Git
echo "🔧 Testando Git..."
if git --version > /dev/null 2>&1; then
    echo "   ✅ Git instalado"
    git --version
else
    echo "   ❌ Git não encontrado"
    exit 1
fi

# Teste 4: SDK Sankhya
echo "🚀 Testando SDK Sankhya..."
if sankhya-sdk --version > /dev/null 2>&1; then
    echo "   ✅ SDK Sankhya instalado"
    sankhya-sdk --version
else
    echo "   ❌ SDK Sankhya não encontrado"
    exit 1
fi

# Teste 5: Eclipse
echo "🛠️ Testando Eclipse..."
if [ -f "~/sankhya-development/eclipse/eclipse" ]; then
    echo "   ✅ Eclipse instalado"
else
    echo "   ❌ Eclipse não encontrado"
    exit 1
fi

echo "🎉 Todos os testes passaram! Ambiente configurado com sucesso!"
```

### **Executar Teste**
```bash
# Tornar script executável
chmod +x test-environment.sh

# Executar teste
./test-environment.sh
```

## 🎯 **Próximos Passos**

### **1. Criar Primeiro Projeto**
```bash
# Criar projeto
sankhya-sdk create-project --name="hello-sankhya" --type="customization"

# Navegar para o projeto
cd hello-sankhya

# Compilar projeto
mvn clean compile

# Executar testes
mvn test
```

### **2. Configurar IDE**
```bash
# Abrir Eclipse
~/sankhya-development/eclipse/eclipse -data ~/sankhya-workspace

# Importar projeto
# File → Import → Existing Maven Projects
# Selecionar: ~/sankhya-development/hello-sankhya
```

### **3. Primeira Personalização**
```java
// src/main/java/br/com/empresa/sankhya/HelloSankhya.java
package br.com.empresa.sankhya;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;

public class HelloSankhya {
    
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        System.out.println("Hello Sankhya! Registro inserido: " + vo.getProperty("ID"));
    }
}
```

## 📊 **Resumo da Configuração**

### **Ferramentas Instaladas**
- ✅ **Java JDK 11**: Ambiente de execução
- ✅ **Eclipse IDE**: Ambiente de desenvolvimento
- ✅ **Maven**: Gerenciamento de dependências
- ✅ **Git**: Controle de versão
- ✅ **SDK Sankhya**: Ferramentas específicas
- ✅ **Oracle Client**: Conectividade com banco

### **Configurações Realizadas**
- ✅ **Variáveis de Ambiente**: PATH e JAVA_HOME
- ✅ **Workspace**: Diretório de trabalho
- ✅ **Projeto Maven**: Estrutura padrão
- ✅ **Logs**: Configuração de logging
- ✅ **Banco de Dados**: Conexão Oracle

### **Próximos Passos**
1. **Criar Primeiro Projeto**: Hello Sankhya
2. **Configurar IDE**: Importar no Eclipse
3. **Primeira Personalização**: Evento básico
4. **Testar Funcionamento**: Verificar execução

---

*Com esta configuração completa, você está pronto para começar a desenvolver personalizações Sankhya de forma profissional e eficiente.*
