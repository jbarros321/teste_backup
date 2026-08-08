package com.credparapp.model.services;

public class CredParSPSession
   extends com.credparapp.model.services.CredParSPBean
   implements javax.ejb.SessionBean
{
   public void ejbActivate()
 {

   }

   public void ejbPassivate()
 {
   }

   public void setSessionContext(javax.ejb.SessionContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException
 {
      super.setSessionContext(ctx);
   }

   public void unsetSessionContext()
 {
   }

   public void ejbRemove()
 {
   }

   public void ejbCreate() throws javax.ejb.CreateException
 {
   }

}
