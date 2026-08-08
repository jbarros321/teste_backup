package com.credparapp.model.services;

public interface CredParSP
   extends javax.ejb.EJBObject
{

   public void buscarServidores( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void buscaCidadesSegmentos( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void buscarAutorizacoesBordero( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void buscarExtratoVendas( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void buscarCidades( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void esqueciMinhaSenha( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

}
