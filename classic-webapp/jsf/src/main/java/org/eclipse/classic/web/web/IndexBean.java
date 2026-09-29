package org.eclipse.classic.web.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class IndexBean {

    public String goToProduct() {
        return "/products?faces-redirect=true";
    }
}
