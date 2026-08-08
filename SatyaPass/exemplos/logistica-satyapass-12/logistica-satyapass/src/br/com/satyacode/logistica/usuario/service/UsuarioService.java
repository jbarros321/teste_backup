package br.com.satyacode.logistica.usuario.service;

import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.StringUtils;

public class UsuarioService {

    public String getUsuarioLogado(){
        String usuarioRetorno = " ";
        if (StringUtils.isNotEmpty(AuthenticationInfo.getCurrent().getUserID())){
            usuarioRetorno = AuthenticationInfo.getCurrent().getUserID() + " - " + AuthenticationInfo.getCurrent().getUsuVO().getNOMEUSU();
        }
        return usuarioRetorno;
    }
}
