/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bremer
 */
public class No {
    private Token pai;
    private List<No> filho;
    private String nome;
    public String code = "";
    public String initial = "";
    public String finalize = "";
    public boolean isFunction = false;
    public String alias = "";
    
    public No() {
        filho = new ArrayList();
    }
    
    public No(String nome) {
        this.filho = new ArrayList();
        this.nome = nome;
    }
    
    public List<No> getFilhos() {
        return filho;
    }

    public void addFilho(No filho) {
        this.filho.add(filho);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Token getPai() {
        return pai;
    }

    public void setPai(Token pai) {
        this.pai = pai;
    }
}
