# ⚡ Generator Sankhya - Gerador de Código

## 🎯 Visão Geral

O **Generator Sankhya** é uma ferramenta de geração de código que trabalha em conjunto com o componente dynaform para padronizar a criação de recursos visuais, facilitar a adição de interceptors e operações de CRUD. É uma ferramenta essencial para acelerar o desenvolvimento de personalizações Sankhya.

## 🏗️ Arquitetura do Generator

### **Componentes Principais**
- **Template Engine**: Motor de geração de código
- **Dynaform Integration**: Integração com dynaform
- **CRUD Generator**: Gerador de operações CRUD
- **Interceptor Generator**: Gerador de interceptors
- **Code Templates**: Templates de código reutilizáveis

### **Fluxo de Geração**
```
┌─────────────────────────────────────────────────────────────┐
│                    INPUT                                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Dynaform      │ │   Metadados     │ │   Configurações │ │
│  │   Definition    │ │   da Entidade   │ │   do Projeto    │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    GENERATOR SANKHYA                       │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Template      │ │   CRUD          │ │   Interceptor   │ │
│  │   Engine        │ │   Generator     │ │   Generator     │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    OUTPUT                                  │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   JSP Files     │ │   Java Classes  │ │   XML Configs   │ │
│  │   Components    │ │   Services      │ │   Mappings      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Funcionalidades Principais

### **1. Geração de CRUD**
- **Create**: Operações de inserção
- **Read**: Operações de consulta
- **Update**: Operações de atualização
- **Delete**: Operações de exclusão
- **Search**: Operações de busca

### **2. Geração de Interceptors**
- **Before Insert**: Interceptors de pré-inserção
- **After Insert**: Interceptors de pós-inserção
- **Before Update**: Interceptors de pré-atualização
- **After Update**: Interceptors de pós-atualização
- **Before Delete**: Interceptors de pré-exclusão
- **After Delete**: Interceptors de pós-exclusão

### **3. Geração de Componentes Visuais**
- **Formulários**: Formulários dinâmicos
- **Grids**: Tabelas de dados
- **Filtros**: Filtros de busca
- **Validações**: Validações de campos
- **Navegação**: Navegação entre telas

## 🔧 Configuração e Uso

### **1. Configuração Inicial**
```xml
<!-- Configuração do Generator no projeto -->
<generator-config>
    <project-name>MeuProjeto</project-name>
    <package-name>br.com.empresa.projeto</package-name>
    <entity-package>br.com.empresa.projeto.entity</entity-package>
    <service-package>br.com.empresa.projeto.service</service-package>
    <controller-package>br.com.empresa.projeto.controller</controller-package>
    <template-path>/templates</template-path>
    <output-path>/src/main/java</output-path>
</generator-config>
```

### **2. Definição de Entidade**
```xml
<!-- Definição da entidade para geração -->
<entity name="MinhaEntidade" table="MINHA_TABELA">
    <field name="id" type="Long" primary="true" auto-increment="true"/>
    <field name="nome" type="String" required="true" max-length="100"/>
    <field name="email" type="String" required="true" max-length="255"/>
    <field name="dataCriacao" type="Date" required="true"/>
    <field name="ativo" type="Boolean" default="true"/>
    
    <relationship name="relacionamentos" type="OneToMany" target="Relacionamento"/>
</entity>
```

### **3. Configuração de Dynaform**
```xml
<!-- Configuração do dynaform -->
<dynaform name="MinhaEntidadeForm">
    <field name="nome" label="Nome" type="text" required="true" max-length="100"/>
    <field name="email" label="E-mail" type="email" required="true" max-length="255"/>
    <field name="dataCriacao" label="Data de Criação" type="date" required="true"/>
    <field name="ativo" label="Ativo" type="checkbox" default="true"/>
    
    <action name="salvar" type="save" label="Salvar"/>
    <action name="cancelar" type="cancel" label="Cancelar"/>
    <action name="excluir" type="delete" label="Excluir"/>
</dynaform>
```

## 📊 Templates de Geração

### **1. Template de Entidade Java**
```java
// Template gerado para entidade
package ${package}.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "${tableName}")
public class ${entityName} {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ${idType} ${idField};
    
    #foreach($field in $fields)
    @Column(name = "${field.columnName}"#if($field.required), nullable = false#end#if($field.maxLength), length = ${field.maxLength}#end)
    private ${field.type} ${field.name};
    #end
    
    #foreach($relationship in $relationships)
    @${relationship.annotation}
    #if($relationship.type == "OneToMany")
    @JoinColumn(name = "${relationship.joinColumn}")
    #end
    private ${relationship.type} ${relationship.name};
    #end
    
    // Construtores
    public ${entityName}() {}
    
    public ${entityName}(${constructorParams}) {
        #foreach($field in $fields)
        this.${field.name} = ${field.name};
        #end
    }
    
    // Getters e Setters
    #foreach($field in $fields)
    public ${field.type} get${field.capitalizedName}() {
        return ${field.name};
    }
    
    public void set${field.capitalizedName}(${field.type} ${field.name}) {
        this.${field.name} = ${field.name};
    }
    #end
}
```

### **2. Template de Service Java**
```java
// Template gerado para service
package ${package}.service;

import ${package}.entity.${entityName};
import ${package}.repository.${entityName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ${entityName}Service {
    
    @Autowired
    private ${entityName}Repository repository;
    
    public List<${entityName}> findAll() {
        return repository.findAll();
    }
    
    public Optional<${entityName}> findById(${idType} id) {
        return repository.findById(id);
    }
    
    public ${entityName} save(${entityName} entity) {
        return repository.save(entity);
    }
    
    public void deleteById(${idType} id) {
        repository.deleteById(id);
    }
    
    public List<${entityName}> findByNomeContaining(String nome) {
        return repository.findByNomeContaining(nome);
    }
}
```

### **3. Template de Controller Java**
```java
// Template gerado para controller
package ${package}.controller;

import ${package}.entity.${entityName};
import ${package}.service.${entityName}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/${entityNameLower}")
public class ${entityName}Controller {
    
    @Autowired
    private ${entityName}Service service;
    
    @GetMapping
    public List<${entityName}> findAll() {
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<${entityName}> findById(@PathVariable ${idType} id) {
        Optional<${entityName}> entity = service.findById(id);
        return entity.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ${entityName} create(@RequestBody ${entityName} entity) {
        return service.save(entity);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<${entityName}> update(@PathVariable ${idType} id, 
                                               @RequestBody ${entityName} entity) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        entity.set${idFieldCapitalized}(id);
        return ResponseEntity.ok(service.save(entity));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ${idType} id) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

### **4. Template de JSP**
```jsp
<!-- Template gerado para JSP -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <title>${entityName} - Formulário</title>
    <link rel="stylesheet" type="text/css" href="${BASE_FOLDER}css/mainCSS.css">
    <style>
        .form-container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        
        .form-group input, .form-group select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        
        .form-actions {
            text-align: center;
            margin-top: 20px;
        }
        
        .btn {
            padding: 10px 20px;
            margin: 0 5px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .btn-primary {
            background: #007bff;
            color: white;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
    </style>
</head>
<body>
    <div class="form-container">
        <h2>${entityName} - Formulário</h2>
        
        <form id="${entityNameLower}Form" method="post">
            <input type="hidden" name="id" value="${entity.id}"/>
            
            #foreach($field in $fields)
            <div class="form-group">
                <label for="${field.name}">${field.label}:</label>
                #if($field.type == "Boolean")
                <input type="checkbox" name="${field.name}" id="${field.name}" 
                       value="true" ${entity.${field.name} ? 'checked' : ''}/>
                #elseif($field.type == "Date")
                <input type="date" name="${field.name}" id="${field.name}" 
                       value="<fmt:formatDate value='${entity.${field.name}}' pattern='yyyy-MM-dd'/>" 
                       ${field.required ? 'required' : ''}/>
                #elseif($field.type == "String" && $field.maxLength > 100)
                <textarea name="${field.name}" id="${field.name}" 
                          rows="4" ${field.required ? 'required' : ''}>${entity.${field.name}}</textarea>
                #else
                <input type="text" name="${field.name}" id="${field.name}" 
                       value="${entity.${field.name}}" 
                       ${field.required ? 'required' : ''}
                       #if($field.maxLength)maxlength="${field.maxLength}"#end/>
                #end
            </div>
            #end
            
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Salvar</button>
                <button type="button" class="btn btn-secondary" onclick="cancelar()">Cancelar</button>
            </div>
        </form>
    </div>
    
    <script>
        function cancelar() {
            window.location.href = '${entityNameLower}List.jsp';
        }
        
        document.getElementById('${entityNameLower}Form').addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Validações JavaScript
            #foreach($field in $fields)
            #if($field.required)
            if (!document.getElementById('${field.name}').value.trim()) {
                alert('O campo ${field.label} é obrigatório!');
                return;
            }
            #end
            #end
            
            // Submeter formulário
            this.submit();
        });
    </script>
</body>
</html>
```

## 🔄 Interceptors Gerados

### **1. Interceptor de Validação**
```java
// Interceptor gerado para validação
package ${package}.interceptor;

import ${package}.entity.${entityName};
import org.springframework.stereotype.Component;

@Component
public class ${entityName}ValidationInterceptor {
    
    public void beforeInsert(${entityName} entity) {
        // Validações antes da inserção
        validateRequiredFields(entity);
        validateBusinessRules(entity);
    }
    
    public void beforeUpdate(${entityName} entity) {
        // Validações antes da atualização
        validateRequiredFields(entity);
        validateBusinessRules(entity);
        validateUniqueness(entity);
    }
    
    public void beforeDelete(${entityName} entity) {
        // Validações antes da exclusão
        validateDeleteRules(entity);
    }
    
    private void validateRequiredFields(${entityName} entity) {
        #foreach($field in $fields)
        #if($field.required)
        if (entity.get${field.capitalizedName}() == null) {
            throw new ValidationException("O campo ${field.label} é obrigatório!");
        }
        #end
        #end
    }
    
    private void validateBusinessRules(${entityName} entity) {
        // Regras de negócio específicas
        // Implementar conforme necessário
    }
    
    private void validateUniqueness(${entityName} entity) {
        // Validações de unicidade
        // Implementar conforme necessário
    }
    
    private void validateDeleteRules(${entityName} entity) {
        // Regras para exclusão
        // Implementar conforme necessário
    }
}
```

### **2. Interceptor de Auditoria**
```java
// Interceptor gerado para auditoria
package ${package}.interceptor;

import ${package}.entity.${entityName};
import org.springframework.stereotype.Component;

@Component
public class ${entityName}AuditInterceptor {
    
    public void afterInsert(${entityName} entity) {
        // Log de auditoria após inserção
        logAudit("INSERT", entity.getId(), "Entidade inserida");
    }
    
    public void afterUpdate(${entityName} entity) {
        // Log de auditoria após atualização
        logAudit("UPDATE", entity.getId(), "Entidade atualizada");
    }
    
    public void afterDelete(${entityName} entity) {
        // Log de auditoria após exclusão
        logAudit("DELETE", entity.getId(), "Entidade excluída");
    }
    
    private void logAudit(String operation, ${idType} id, String description) {
        // Implementar log de auditoria
        System.out.println(String.format("AUDIT: %s - ID: %s - %s", 
                                        operation, id, description));
    }
}
```

## 📋 Configurações Avançadas

### **1. Configuração de Templates Personalizados**
```xml
<!-- Configuração de templates customizados -->
<template-config>
    <template name="entity" path="/templates/custom/entity.java.vm"/>
    <template name="service" path="/templates/custom/service.java.vm"/>
    <template name="controller" path="/templates/custom/controller.java.vm"/>
    <template name="jsp" path="/templates/custom/form.jsp.vm"/>
    <template name="interceptor" path="/templates/custom/interceptor.java.vm"/>
</template-config>
```

### **2. Configuração de Validações**
```xml
<!-- Configuração de validações -->
<validation-config>
    <field name="email" type="email" message="E-mail inválido"/>
    <field name="cpf" type="cpf" message="CPF inválido"/>
    <field name="cnpj" type="cnpj" message="CNPJ inválido"/>
    <field name="phone" type="phone" message="Telefone inválido"/>
    <field name="date" type="date" format="dd/MM/yyyy" message="Data inválida"/>
</validation-config>
```

### **3. Configuração de Relacionamentos**
```xml
<!-- Configuração de relacionamentos -->
<relationship-config>
    <relationship name="relacionamentos" type="OneToMany" target="Relacionamento">
        <cascade>ALL</cascade>
        <fetch>LAZY</fetch>
    </relationship>
    <relationship name="categoria" type="ManyToOne" target="Categoria">
        <cascade>PERSIST</cascade>
        <fetch>EAGER</fetch>
    </relationship>
</relationship-config>
```

## 🎯 Casos de Uso

### **1. Geração de CRUD Completo**
```bash
# Comando para gerar CRUD completo
generator-sankhya generate-crud \
    --entity MinhaEntidade \
    --package br.com.empresa.projeto \
    --output /src/main/java \
    --include-jsp \
    --include-interceptors
```

### **2. Geração de Componentes Específicos**
```bash
# Gerar apenas entidade
generator-sankhya generate-entity \
    --entity MinhaEntidade \
    --package br.com.empresa.projeto

# Gerar apenas service
generator-sankhya generate-service \
    --entity MinhaEntidade \
    --package br.com.empresa.projeto

# Gerar apenas controller
generator-sankhya generate-controller \
    --entity MinhaEntidade \
    --package br.com.empresa.projeto
```

### **3. Geração com Configurações Personalizadas**
```bash
# Gerar com templates customizados
generator-sankhya generate-crud \
    --entity MinhaEntidade \
    --package br.com.empresa.projeto \
    --template-path /custom/templates \
    --config-file /custom/config.xml
```

## 🛠️ Boas Práticas

### **1. Estrutura de Projeto**
- Organize templates em diretórios lógicos
- Use nomenclatura consistente
- Mantenha configurações centralizadas
- Documente templates customizados

### **2. Templates**
- Crie templates reutilizáveis
- Use variáveis de configuração
- Implemente validações nos templates
- Mantenha templates atualizados

### **3. Configuração**
- Use arquivos de configuração externos
- Valide configurações antes da geração
- Mantenha backup das configurações
- Documente configurações complexas

### **4. Geração**
- Teste código gerado antes de usar
- Revise templates regularmente
- Mantenha controle de versão
- Documente mudanças

## 🔍 Troubleshooting

### **Problemas Comuns**
- Templates não encontrados
- Configurações inválidas
- Código gerado com erros
- Dependências faltando

### **Soluções**
- Verificar caminhos de templates
- Validar arquivos de configuração
- Revisar templates
- Instalar dependências necessárias

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- Novos tipos de templates
- Melhor integração com IDEs
- Suporte a mais frameworks
- Ferramentas de debug

### **Tendências Futuras**
- Geração baseada em IA
- Templates dinâmicos
- Integração com cloud
- Automação avançada

---

*Este documento foi criado com base na documentação oficial do Generator Sankhya e melhores práticas de desenvolvimento.*
