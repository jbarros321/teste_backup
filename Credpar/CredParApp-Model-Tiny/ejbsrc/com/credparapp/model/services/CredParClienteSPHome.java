package com.credparapp.model.services;

public interface CredParClienteSPHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/CredParClienteSP";
   public static final String JNDI_NAME="com/credparapp/model/services/CredParClienteSP";

   public com.credparapp.model.services.CredParClienteSP create()
      throws javax.ejb.CreateException,java.rmi.RemoteException;

}
