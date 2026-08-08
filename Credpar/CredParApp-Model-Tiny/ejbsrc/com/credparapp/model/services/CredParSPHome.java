package com.credparapp.model.services;

public interface CredParSPHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/CredParSP";
   public static final String JNDI_NAME="com/credparapp/model/services/CredParSP";

   public com.credparapp.model.services.CredParSP create()
      throws javax.ejb.CreateException,java.rmi.RemoteException;

}
