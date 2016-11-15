/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.List;
import sun.misc.Queue;

/**
 *
 * @author Bremer
 */
public class Lexan {
    public static List<Token> symbolTable;
    private short estado;
    private StringBuilder lexema;
    public Queue<Token> Tokens;
    
    public Lexan() {
        this.estado = 1;
        this.lexema = new StringBuilder("");
        this.Tokens = new Queue();
        this.symbolTable = new ArrayList<>();
        CarregarSymbolTable();
    }
    
    public String ReadChar(char c) {
        String errorMessage = "";
        switch (this.estado) {
            case 1:
                if (IsLetter(c)) {
                    this.estado = 2;
                    this.lexema.append(c);
                } else if (IsDigit(c)) {
                    this.estado = 3;
                    this.lexema.append(c);
                } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == ',' || c == '(' || c == ')' || c == '.') {
                    HandleNewToken(getSymbolTableToken(Character.toString(c)));
                } else if (c == '<') {
                    this.estado = 5;
                    this.lexema.append(c);
                } else if (c == '>') {
                    this.estado = 6;
                    this.lexema.append(c);
                } else if (c == '\'') {
                    this.estado = 7;
                    this.lexema.append(c);
                } else if (c == '\"') {
                    this.estado = 8;
                    this.lexema.append(c);
                } else if (c == ' ' || c == '\n' || c == '\r') {
                    break;
                } else {
                    errorMessage = "Erro próximo a: caracter " + c + " não permitido";
                    break;
                }
                break;
            case 2:
                if (IsLetter(c) || IsDigit(c) || c == '_') {
                    this.lexema.append(c);
                    break;
                }
                Token token = getSymbolTableToken(this.lexema.toString());
                if (token == null) {
                    token = new Token("41", this.lexema.toString());
                }
                HandleNewToken(token);
                errorMessage = ReadChar(c);
                break;
            case 3:
                if (IsDigit(c)) {
                    this.lexema.append(c);
                    break;
                } else if (c == '.') {
                    this.estado = 4;
                    this.lexema.append(c);
                    break;
                }
                HandleNewToken(new Token("40", this.lexema.toString()));
                errorMessage = ReadChar(c);
                break;
            case 4:
                if (IsDigit(c)) {
                    this.lexema.append(c);
                    break;
                } else {
                    int lastChar = this.lexema.length() - 1;
                    if (this.lexema.charAt(lastChar) == '.') { 
                        errorMessage = "Erro próximo a: " +  this.lexema.toString();
                    } else {
                        HandleNewToken(new Token("40", this.lexema.toString()));
                    }
                }
                break;
            case 5:
                if (c == '>' || c == '=') {
                    this.lexema.append(c);
                    HandleNewToken(getSymbolTableToken(this.lexema.toString()));
                } else {
                    HandleNewToken(getSymbolTableToken(this.lexema.toString()));
                    errorMessage = ReadChar(c);
                }                
                break;
            case 6:
                if (c == '=') {
                    this.lexema.append(c);
                    HandleNewToken(getSymbolTableToken(this.lexema.toString()));
                } else {
                    HandleNewToken(getSymbolTableToken(this.lexema.toString()));
                    errorMessage = ReadChar(c);
                }
                break;
            case 7: 
                if (c == '\'') {
                    this.lexema.append(c);
                    HandleNewToken(new Token("51", this.lexema.toString()));
                } else {
                    this.lexema.append(c);
                }
                break;
            case 8: 
                if (c == '\"') {
                    this.lexema.append(c);
                    HandleNewToken(new Token("51", this.lexema.toString()));
                } else {
                    this.lexema.append(c);
                }
                break;    
        }
        return errorMessage;
    }
    
    public String EnqueueLastToken() {
        String errorMessage = "";
        if (this.estado == 7 || this.estado == 8) {
             errorMessage = "Erro: String " + this.lexema.toString() + " não foi fechada.";
        }
        if (this.estado != 1) {
            int lastChar = this.lexema.length() - 1;
            if (this.lexema.charAt(lastChar) == '.') { 
                errorMessage = "Erro próximo a: " +  this.lexema.toString();
            } else {
                Token token = getSymbolTableToken(this.lexema.toString());
                if (token == null) {
                    if (IsLetter(this.lexema.charAt(lastChar)) || this.estado == 2) {
                        token = new Token("41", this.lexema.toString());
                    } else {
                        token = new Token("40", this.lexema.toString());
                    }
                }
                HandleNewToken(token);
            }
        }
        return errorMessage;
    }
    
    private void HandleNewToken(Token token) {
        this.Tokens.enqueue(token);
        this.lexema.setLength(0);
        this.estado = 1;
    }
    
    private void CarregarSymbolTable() {
        symbolTable.add(new Token("1", "select"));
        symbolTable.add(new Token("2", "all"));
        symbolTable.add(new Token("3", "distinct"));
        symbolTable.add(new Token("4", "from"));
        symbolTable.add(new Token("5", "where"));
        symbolTable.add(new Token("6", "group"));
        symbolTable.add(new Token("7", "by"));
        symbolTable.add(new Token("8", "having"));
        symbolTable.add(new Token("9", "as"));
        symbolTable.add(new Token("10", "left"));
        symbolTable.add(new Token("11", "right"));
        symbolTable.add(new Token("12", "full"));
        symbolTable.add(new Token("13", "inner"));
        symbolTable.add(new Token("14", "join"));
        symbolTable.add(new Token("15", "on"));
        symbolTable.add(new Token("16", "using"));
        symbolTable.add(new Token("17", "table"));
        symbolTable.add(new Token("18", "lateral"));
        symbolTable.add(new Token("19", "unnest"));
        symbolTable.add(new Token("20", "with"));
        symbolTable.add(new Token("21", "ordinality"));
        symbolTable.add(new Token("22", "specific"));
        symbolTable.add(new Token("23", "values"));
        symbolTable.add(new Token("24", "cube"));
        symbolTable.add(new Token("25", "rollup"));
        symbolTable.add(new Token("26", "grouping"));
        symbolTable.add(new Token("27", "sets"));
        symbolTable.add(new Token("28", "("));
        symbolTable.add(new Token("29", ")"));
        symbolTable.add(new Token("30", "="));
        symbolTable.add(new Token("31", "<"));
        symbolTable.add(new Token("32", "<="));
        symbolTable.add(new Token("33", ">"));
        symbolTable.add(new Token("34", ">="));
        symbolTable.add(new Token("35", "<>"));
        symbolTable.add(new Token("36", "/"));
        symbolTable.add(new Token("37", "*"));
        symbolTable.add(new Token("38", "+"));
        symbolTable.add(new Token("39", "-"));
        symbolTable.add(new Token("42", "max"));
        symbolTable.add(new Token("43", "min"));
        symbolTable.add(new Token("44", "avg"));
        symbolTable.add(new Token("45", "sum"));
        symbolTable.add(new Token("46", "count"));
        symbolTable.add(new Token("47", ","));
        symbolTable.add(new Token("48", "and"));
        symbolTable.add(new Token("49", "or"));
        symbolTable.add(new Token("50", "like"));
        symbolTable.add(new Token("52", "order"));
        symbolTable.add(new Token("53", "asc"));
        symbolTable.add(new Token("54", "desc"));
        symbolTable.add(new Token("55", "."));
    }
    
    private boolean IsLetter(char c) {
        //"^[a-zA-Z]$"
        return Character.toString(c).matches("[a-zA-Z]");
    }
    
    private boolean IsDigit(char c) {
        //"^[0-9]$"
        return Character.toString(c).matches("[0-9]");
    }
    
    private Token getSymbolTableToken(String lexema) {
        for (Token t : symbolTable) {
            if (t.getLexema().toUpperCase().equals(lexema.toUpperCase())) {
                return t;
            }
        }
        return null;
    }
}
