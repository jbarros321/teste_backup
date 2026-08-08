package br.com.credpar.ext;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.sankhya.dwf.controller.UserApplication;
import br.com.sankhya.modelcore.facades.MGEFrontFacade;
import br.com.sankhya.modulemgr.MGESession;

public class UserApplicationImpl extends UserApplication implements Serializable {
    private MGEFrontFacade mgeFrontFacade;
    private MGESession     mgeSession;
    private String         mgeSessionId;

    public void setMgeFrontFacade(MGEFrontFacade mgeFrontFacade) {
        this.mgeFrontFacade = mgeFrontFacade;
    }

    public MGEFrontFacade getMgeFrontFacade() {
        return mgeFrontFacade;
    }

    public void setMgeSession(MGESession mgeSession) {
        this.mgeSession = mgeSession;
    }

    public MGESession getMgeSession() {
        return mgeSession;
    }

    public void setMgeSessionId(String mgeSessionId) {
        this.mgeSessionId = mgeSessionId;
    }

    public String getMgeSessionId() {
        return mgeSessionId;
    }

    public void initUserSession(HttpServletRequest request, HttpServletResponse response, String mgeSessionId) throws Exception {
        mgeFrontFacade = mgeContext.getMgeFrontFacade();
    }

    public boolean validateSession(HttpServletRequest request, HttpServletResponse response, String mgeSessionId) throws Exception {
        try {
            if(mgeFrontFacade == null)
 {
                return false;
            }
            mgeFrontFacade.getAuthenticationInfo();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
