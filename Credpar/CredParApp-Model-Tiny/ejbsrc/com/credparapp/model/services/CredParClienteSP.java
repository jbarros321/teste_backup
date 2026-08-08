package com.credparapp.model.services;

public interface CredParClienteSP
   extends javax.ejb.EJBObject
{

   public void loginCliente( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void getInfoSaldo( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void getInfoExtrato( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void getInfoContato( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void enviarEmail( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void alterarSenha( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

   public void esqueciMinhaSenhaCliente( br.com.sankhya.ws.ServiceContext ctx )
      throws br.com.sankhya.modelcore.MGEModelException, java.rmi.RemoteException;

}
