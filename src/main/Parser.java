/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Queue;

/**
 *
 * @author Bremer
 */
public class Parser {
    private Queue<Token> Tokens;
    private Token token;
    public ArrayList<String> saida;
    public ArrayList<String> ordemSelect;
    public ArrayList<String> ordemGroupBy;
    public String queryMongoDB = "";
    private boolean AND;
    private boolean OR;
    private boolean temFuncao;
    private No arvore;
    private String tableName = "";
    private String tableAlias = "";
    private boolean usingGroupBy;
    private boolean usingOrderBy;
    private boolean needFinalize;
    private boolean temWhere;
    private String tableJoinName = "";
    private String tableJoinAlias = "";
    private String keyTable1 = "";
    private String keyTable2 = "";
    private boolean selecionarTudo;
    private boolean orderByDepois;
    
    public Parser(Queue<Token> tokens) {
        this.Tokens = tokens;
        this.saida = new ArrayList();
        this.ordemSelect = new ArrayList();
        this.ordemGroupBy = new ArrayList();
        Start();
        ImprimirArvore(this.arvore, 1);
    }
    
    public void ImprimirArvore(No no, int whiteSpace) {
        if (no != null && no.getNome() != null && !"".equals(no.getNome())) {
            String space = "";
            for (int i = 1; i <= whiteSpace; i++) {
                space += " ";
            }
            System.out.print("\n" + space + no.getNome());
            
            for (No n : no.getFilhos()) {
                ImprimirArvore(n, whiteSpace+3);
            }
        }
    }
    
    private Token getSymbolTableToken(String nome) {
        for (Token t : Lexan.symbolTable) {
            if (t.getNome().toUpperCase().equals(nome.toUpperCase())) {
                return t;
            }
        }
        return null;
    }
     
    public boolean isToken(String nome) {
        return nome.equals(this.token.getNome());
    }
    
    public boolean casaToken(String nome, boolean gerarErro) {
        if (nome.equals(this.token.getNome())) {
            try {
                if (!this.Tokens.isEmpty()) {
                    this.token = this.Tokens.dequeue();
                }
                return true;
            } catch (InterruptedException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (gerarErro) {
            Token t = getSymbolTableToken(nome);
            if (t == null) {
                String valor = "";
                switch (nome) {
                    case "41":
                        valor = "ID";
                        break;
                    case "40":
                        valor = "Número";
                        break;
                    case "51":
                        valor = "String";
                        break;
                }
                this.saida.add("Erro sintático: Era esperado um " + valor + ".");
            } else {
                this.saida.add("Erro sintático: Esperado '" + t.getLexema() + "' e encontrado '" + this.token.getLexema() + "'.");
            }
        }
        return false;
    }
     
     public void Start() {
        try {
            token = Tokens.dequeue();
            this.arvore = Select();
            
            
            if (!this.saida.isEmpty()) return;
            
            if (this.tableAlias.isEmpty() && !this.tableJoinAlias.isEmpty()) {
                this.saida.add("Erro: Alias da tabela " + this.tableName + " não encontrado.");
                return;
            }
            
            for (No no : this.arvore.getFilhos()) {
                if (no.getNome().equals("SelectExpression")) {
                    for (No n : no.getFilhos()) {
                        if (n.isFunction) continue;
                        
                        if (n.alias.isEmpty() && (!this.tableAlias.isEmpty() || !this.tableJoinAlias.isEmpty()) && !n.getNome().equals("*")) {
                            this.saida.add("Erro: Alias do campo " + n.getNome() + " não encontrado.");
                            return;
                        } else if (!n.alias.toUpperCase().equals(this.tableAlias.toUpperCase()) && !n.alias.toUpperCase().equals(this.tableJoinAlias.toUpperCase()) && !n.getNome().equals("*")) {
                            this.saida.add("Erro: Alias " + n.alias + " do campo " + n.getNome() + " não declarado.");
                            return;
                        }
                    }
                }
            }
            
            if (!Tokens.isEmpty() && this.saida.isEmpty()) {
                this.saida.add("Erro Sintático: '" + token.getLexema() + "' inexperado.");
                return;
            } else if (this.AND && this.OR) {
                this.saida.add("Erro: Suportado apenas 1 operador lógico (AND ou OR).");
                return;
            } else if (temFuncao) {
                if (ordemGroupBy.size() < ordemSelect.size()) {
                    this.saida.add("Erro Sintático: valores no Group By não condizentes com valores no Select.");
                    return;
                } else {
                    for (int index = 0; index < ordemSelect.size(); index++) {
                        if (!ordemGroupBy.get(index).toUpperCase().equals(ordemSelect.get(index).toUpperCase())) {
                            this.saida.add("Erro Sintático: ordem dos valores no Group By não condizentes com a ordem dos valores no Select.");
                            return;
                        }
                    }
                }
            } 
            MontarQuery();
        } catch (InterruptedException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     
     public void MontarQuery() {
         String query = "";
         if (!this.temFuncao && !this.temWhere && this.tableJoinName.isEmpty() && !this.usingGroupBy && !this.usingOrderBy && this.selecionarTudo) {
             query += "db." + this.tableName + ".find();";
         } else {
            query += "db." + this.tableName + ".aggregate([\n";
            if (!this.tableJoinName.isEmpty()) {
                query += "    { $lookup: {\n        from: \"" + this.tableJoinName + "\",\n        localField: \"" + this.keyTable1 + "\",\n        foreignField: \"" + this.keyTable2 +
                        "\",\n        as: \"" + this.tableJoinAlias + "\"";
                query += "\n    }}";
            }
            if (this.temWhere) {
                query += this.tableJoinName.isEmpty() ? "    { $match: " :  ",\n    { $match: ";
                if (this.AND) {
                    query += "{ $and: [ ";
                } else if (this.OR) {
                    query += "{ $or: [ ";
                }
                for (No no : this.arvore.getFilhos()) {
                   if (no.getNome().toUpperCase().equals("WHERE")) {
                       boolean first = true;
                       for (No n : no.getFilhos()) {
                           if (!first) {
                               query += ", ";
                           }
                           first = false;
                           No filho1 = n.getFilhos().get(0);
                           No filho2 = n.getFilhos().get(1);
                           query += "{" + filho1.getNome();
                           query += String.format(n.code, filho2.getNome()) + "} ";
                       }
                   }
               }
               if (this.AND || this.OR) {
                   query += "] }";
               }
               query += " }";
           }

           if (this.usingOrderBy && !this.orderByDepois) {
               query += this.temWhere || !this.tableJoinName.isEmpty() ? ",\n    { $sort: {\n" : "    { $sort: {\n";
               for (No no : this.arvore.getFilhos()) {
                  if (no.getNome().toUpperCase().equals("ORDER BY")) {
                      boolean first = true;
                      for (No n : no.getFilhos()) {
                          if (!first) {
                              query += ",\n";
                          }
                          first = false;
                          query += "        " + n.code;
                      }
                  }
              }
               query += "\n    }}";
           } 

           if (this.temFuncao || this.usingGroupBy) {
               query += (this.usingOrderBy && !this.orderByDepois) || this.temWhere || !this.tableJoinName.isEmpty() ? ",\n    { $group: {\n" : "    { $group: {\n";
               query += ordemSelect.isEmpty() && ordemGroupBy.isEmpty() ? "        _id: 0" : "        _id: { ";
               boolean firstField = true;
               for (No no : this.arvore.getFilhos()) {
                   if (no.getNome().equals("SelectExpression")) {
                       for (No n : no.getFilhos()) {
                           if (n.isFunction) continue;
                           else if (!firstField) {
                               query += ", ";
                           }
                           firstField = false;
                           if (n.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                               query += "\"" + n.getNome() + "\": \"$" + n.getNome() + "\"";
                           } else {
                               query += "\"" + n.getNome() + "\": \"$" + this.tableJoinAlias + "." + n.getNome() + "\"";
                           }
                       }
                   }
               }

               for (int index = 0; index < ordemGroupBy.size(); index++) {
                   if (index >= ordemSelect.size() || !ordemGroupBy.get(index).toUpperCase().equals(ordemSelect.get(index).toUpperCase())) {
                       String field = ordemGroupBy.get(index);
                       if (!firstField) {
                               query += ", ";
                           }
                       firstField = false;
                       String alias = field.indexOf('.') > 0 ? field.substring(0, field.indexOf('.')) : "";
                       String fieldName = field.indexOf('.') > 0 ? field.substring(field.indexOf('.') + 1, field.length()): field;

                       if (alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                           query += "\"" + fieldName + "\": \"$" + fieldName + "\"";
                       } else {
                           query += "\"" + fieldName + "\": \"$" + this.tableJoinAlias + "." + fieldName + "\"";
                       }
                   }
               }

               query += ordemSelect.isEmpty() ? "" : "}";

               for (No no : this.arvore.getFilhos()) {
                    if (no.getNome().equals("SelectExpression")) {
                        for (No n : no.getFilhos()) {
                            if (!n.isFunction) continue;
                            query += ",\n";
                            query += "        " + n.code;
                        }
                    }
               }

               query += "\n    }}";

               if (this.usingOrderBy && this.orderByDepois) {
                   query += ",\n    { $sort: {\n";
                   for (No no : this.arvore.getFilhos()) {
                      if (no.getNome().toUpperCase().equals("ORDER BY")) {
                          boolean first = true;
                          for (No n : no.getFilhos()) {
                              if (!first) {
                                  query += ",\n";
                              }
                              first = false;
                              query += "        " + n.code;
                          }
                      }
                  }
                   query += "\n    }}";
              }
           } else if (!this.selecionarTudo) {
               query += this.usingOrderBy || this.temWhere || !this.tableJoinName.isEmpty() ? ",\n    { $project: {\n" : "    { $project: {\n";
               for (No no : this.arvore.getFilhos()) {
                   if (no.getNome().equals("SelectExpression")) {
                       boolean first = true;
                       for (No n : no.getFilhos()) {
                           if (n.isFunction) continue;
                           else if (!first) {
                               query += ", ";
                           }
                           first = false;
                           if (n.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                               query += "        " + n.getNome() + ": \"$" + n.getNome() + "\"";
                           } else {
                               query += "        " + n.getNome() + ": \"$" + this.tableJoinAlias + "." + n.getNome() + "\"";
                           }
                       }
                   }
               }
               query += " }\n    }";
           }

           query += "\n]);";
        }
        this.queryMongoDB = query; 
     }
     
     public No Select() {
         No no = null;
         String lexema = this.token.getLexema();
         if (casaToken("1", true)) {
             no = new No(lexema);
             if (isToken("2") || isToken("3")) {
                 Type(no);
             }
             No noSelectExpression = SelectExpression();
             no.addFilho(noSelectExpression);
             
             lexema = this.token.getLexema();
             if (casaToken("4", true)) {
                 No noFrom = new No(lexema);
                 lexema = this.tableName = this.token.getLexema();
                 if (casaToken("41", true)) {
                     if (casaToken("9", false)) {
                        String lexema2 = this.token.getLexema();
                        if (casaToken("41", true)) {
                            this.tableAlias = lexema2;
                        }
                     }
                 }
                 noFrom.addFilho(new No(lexema));
                 no.addFilho(noFrom);
             }
             
             if (isToken("47")) {
                 this.saida.add("Erro: Não é suportado mais de uma tabela no FROM.");
                 return no;
             }
             
             if (casaToken("14", false)) { //JOIN
                 lexema = this.token.getLexema();
                 if (casaToken("41", true)) {
                     this.tableJoinName = lexema;
                     if (casaToken("9", true)) {
                        lexema = this.token.getLexema();
                        if (casaToken("41", true)) {
                            this.tableJoinAlias = lexema;
                            if (this.tableAlias.toUpperCase().equals(lexema.toUpperCase())) {
                                this.saida.add("Erro: Duplicidade de alias. Já existe uma tabela utilizando o alias " + lexema + ".");
                            }
                            if (casaToken("15", true)) {
                                String lexema2 = this.token.getLexema();
                                if (casaToken("41", true)) {
                                    if (casaToken("55", true)) {
                                        lexema = this.token.getLexema();
                                        if (casaToken("41", true)) {
                                            if (lexema2.toUpperCase().equals(this.tableAlias.toUpperCase())) this.keyTable1 = lexema;
                                            else if (lexema2.toUpperCase().equals(this.tableJoinAlias.toUpperCase())) {
                                                this.tableJoinAlias = lexema2;
                                                this.keyTable2 = lexema;
                                            } else {
                                                this.saida.add("Erro: Alias " + lexema2 + " não declarado. Encontrado em " + lexema2 + "." + lexema+".");
                                            }
                                            if (casaToken("30", true)) {
                                                lexema2 = this.token.getLexema();
                                                if (casaToken("41", true)) {
                                                    if (casaToken("55", true)) {
                                                        lexema = this.token.getLexema();
                                                        if (casaToken("41", true)) {
                                                           if (lexema2.toUpperCase().equals(this.tableAlias.toUpperCase())) this.keyTable1 = lexema;
                                                            else if (lexema2.toUpperCase().equals(this.tableJoinAlias.toUpperCase())) {
                                                                this.tableJoinAlias = lexema2;
                                                                this.keyTable2 = lexema;
                                                            } else {
                                                                this.saida.add("Erro: Alias " + lexema2 + " não declarado. Encontrado em " + lexema2 + "." + lexema+".");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                     }
                 }
             }
             
             if (isToken("5")) {
                 No noWhere = WhereClausure();
                 no.addFilho(noWhere);
             }
             
             if (isToken("6")) {
                 No noGroupBy = GroupByClausure();
                 no.addFilho(noGroupBy);
             } else if (temFuncao && !this.ordemSelect.isEmpty()) {
                 this.saida.add("Erro sintático: Esperado Group By.");
                 return no;
             }
             
             if (isToken("52")) {
                 No noOrderBy = OrderByClausure();
                 no.addFilho(noOrderBy);
             }             
         }
         return no;
     }
     
     public void Type(No no) {
         String lexema = this.token.getLexema();
         if (casaToken("2", false)) {
            No noType = new No(lexema);
            no.addFilho(noType);
         } else if (casaToken("3", false)) {
            No noType = new No(lexema);
            no.addFilho(noType);
         }
     }
     
     public No SelectExpression() {
        No no = new No("SelectExpression");
        if (isToken("41") || isToken("42") || isToken("43") || isToken("44") || isToken("45") || isToken("46")) {
            ProjectItem(no);
            ProjectItemAsterisco(no);
        } else {
            No noStar = new No(this.token.getLexema());
            no.addFilho(noStar);
            casaToken("37", true);
            this.selecionarTudo = true;
        }
        return no;
     }
     
     public void ProjectItem(No no) {
         No noProjectItem = new No();
         String lexema = this.token.getLexema();
         String code = "";
         if (casaToken("42", false)) {
             No noFunction = function(false);
             noProjectItem.setNome(lexema);
             if (noFunction.alias.equals("") || noFunction.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                noProjectItem.code = "max" + noFunction.getNome() + noFunction.alias + ": {$max: \"$" + noFunction.getNome() + "\"}";
             } else {
                 noProjectItem.code = "max" + noFunction.getNome() + noFunction.alias + ": {$max: \"$" + noFunction.alias + "." + noFunction.getNome() + "\"}";
             }
             noProjectItem.isFunction = true;
             no.addFilho(noProjectItem);
             temFuncao = true;
         } else if (casaToken("43", false)) {
             No noFunction = function(false);
             noProjectItem.setNome(lexema);
             if (noFunction.alias.equals("") || noFunction.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                noProjectItem.code = "min" + noFunction.getNome() + noFunction.alias + ": {$min: \"$" + noFunction.getNome() + "\"}";
             } else {
                 noProjectItem.code = "min" + noFunction.getNome() + noFunction.alias + ": {$min: \"$" + noFunction.alias + "." + noFunction.getNome() + "\"}";
             }
             noProjectItem.addFilho(noFunction);
             noProjectItem.isFunction = true;
             no.addFilho(noProjectItem);
             temFuncao = true;
         } else if (casaToken("44", false)) {
             No noFunction = function(false);
             noProjectItem.setNome(lexema);
             if (noFunction.alias.equals("") || noFunction.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                noProjectItem.code = "avg" + noFunction.getNome() + noFunction.alias + ": {$avg: \"$" + noFunction.getNome() + "\"}";
             } else {
                 noProjectItem.code = "avg" + noFunction.getNome() + noFunction.alias + ": {$avg: \"$" + noFunction.alias + "." + noFunction.getNome() + "\"}";
             }
             noProjectItem.addFilho(noFunction);
             noProjectItem.isFunction = true;
             no.addFilho(noProjectItem);
             temFuncao = true;
             this.needFinalize = true;
         } else if (casaToken("45", false)) {
             No noFunction = function(false);
             noProjectItem.setNome(lexema);
             if (noFunction.alias.equals("") || noFunction.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                noProjectItem.code = "sum" + noFunction.getNome() + noFunction.alias + ": {$sum: \"$" + noFunction.getNome() + "\"}";
             } else {
                 noProjectItem.code = "sum" + noFunction.getNome() + noFunction.alias + ": {$sum: \"$" + noFunction.alias + "." + noFunction.getNome() + "\"}";
             }
             noProjectItem.addFilho(noFunction);
             noProjectItem.isFunction = true;
             no.addFilho(noProjectItem);
             temFuncao = true;
         } else if (casaToken("46", false)) {
             No noFunction = function(true);
             noProjectItem.setNome(lexema);
             if (noFunction.alias.equals("") || noFunction.alias.toUpperCase().equals(this.tableAlias.toUpperCase())) {
                noProjectItem.code = "count" + noFunction.getNome() + noFunction.alias + ": {$sum: 1}";
             } else {
                 noProjectItem.code = "count" + noFunction.getNome() + noFunction.alias + ": {$sum: 1}";
             }
             noProjectItem.addFilho(noFunction);
             noProjectItem.isFunction = true;
             no.addFilho(noProjectItem);
             temFuncao = true;
         } else {
             casaToken("41", true);
             if (casaToken("55", false)) {
                 String lexema2 = this.token.getLexema();
                 if (casaToken("41", true)) {
                     noProjectItem.setNome(lexema2);
                     noProjectItem.alias = lexema;
                     ordemSelect.add(lexema + "." + lexema2);
                 }
             } else {
                 noProjectItem.setNome(lexema);
                 ordemSelect.add(lexema);
             }
             
             no.addFilho(noProjectItem);
             
         }
     }
     
     public No function(boolean isCount) {
         No no = null;
         if (casaToken("28", true)) {
            String lexema = this.token.getLexema();
            if (casaToken("37", false)) {
                no = new No("all");
                if (!isCount) {
                    this.saida.add("Erro: Não é permitido usar o símbolo * em uma função que não seja Count.");
                }
            } else if (casaToken("41", true)) {
                if (casaToken("55", false)) {
                    String lexema2 = this.token.getLexema();
                    if (casaToken("41", true)) {
                        no = new No(lexema2);
                        no.alias = lexema;
                    }
                } else {
                    no = new No(lexema);
                }
            }
            casaToken("29", true);
         } 
         return no;
     }
     
     public void ProjectItemAsterisco(No no) {
         if (casaToken("47", false)) {
             ProjectItem(no);
             ProjectItemAsterisco(no);
         }
     }
     
     public No WhereClausure() {
         No no = null;
         String lexema = this.token.getLexema();
         if (casaToken("5", false)) {
             this.temWhere = true;
             no = new No(lexema);
             No noBoolean = BooleanExpression();
             no.addFilho(noBoolean);
             Other(no);
         }
         return no;
     }
     
     public No BooleanExpression() {
         No no = null;
         No noID = null;
         String lexema = this.token.getLexema();
         if (casaToken("41", true)) {
             if (casaToken("55", false)) {
                 String lexema2 = this.token.getLexema();
                 if (casaToken("41", true)) {
                     if (this.tableAlias.toUpperCase().equals(lexema.toUpperCase())) {
                        noID = new No(lexema2);
                        noID.alias = lexema;
                     } else if (this.tableJoinAlias.toUpperCase().equals(lexema.toUpperCase())) {
                        noID = new No("\"" + lexema + "." + lexema2 + "\"");
                        noID.alias = lexema;
                     } else {
                         this.saida.add("Erro: Alias " + lexema + " não declarado. Encontrado em " + lexema + "." + lexema2+".");
                     }
                 }
             } else {
                 noID = new No(lexema);
             }
             
             No noOperador = Operador();
             No noComparador = Comparador();
             
             if (noOperador != null) {
                 noOperador.addFilho(noID);
                 noOperador.addFilho(noComparador);
                 no = noOperador;
             }
         }
         return no;
     }
     
     public No Operador() {
         No no = null;
         String lexema = this.token.getLexema();
         if (casaToken("30", false)) {
            no = new No(lexema);
            no.code = ": %s";
         } else if (casaToken("31", false)) {
            no = new No(lexema);
            no.code = ": { $lt: %s }";
         } else if (casaToken("32", false)) {
            no = new No(lexema);
            no.code = ": { $lte: %s }";
         } else if (casaToken("33", false)) {
            no = new No(lexema);
            no.code = ": { $gt: %s }";
         } else if (casaToken("34", false)) {
            no = new No(lexema);
            no.code = ": { $gte: %s }";
         } else if (casaToken("35", false)) {
            no = new No(lexema);
            no.code = ": { $ne: %s }";
         } else if (casaToken("50", false)) {
            no = new No(lexema);
            no.code = ": %s";
         } else {
             this.saida.add("Erro sintático: Operador '" + this.token.getLexema() + "' não reconhecido.");
         }
         return no;
     }
     
     public No Comparador() {
         No no = null;
         String lexema = this.token.getLexema();
         if (casaToken("41", false)) {
             if (casaToken("55", false)) {
                 String lexema2 = this.token.getLexema();
                 if (casaToken("41", true)) {
                     if (this.tableAlias.toUpperCase().equals(lexema.toUpperCase())) {
                        no = new No(lexema2);
                        no.alias = lexema;
                     } else if (this.tableJoinAlias.toUpperCase().equals(lexema.toUpperCase())) {
                        no = new No("\"" +lexema + "." + lexema2 + "\"");
                        no.alias = lexema;
                     } else {
                         this.saida.add("Erro: Alias " + lexema + " não declarado. Encontrado em " + lexema + "." + lexema2+".");
                     }
                 }
             } else {
                 no = new No(lexema);
             }
         } else if (casaToken("51", false)) {
             no = new No(lexema);
         } else if (casaToken("40", false)) {
             no = new No(lexema);
         } else {
             this.saida.add("Erro sintático: Comparação com '" + this.token.getLexema() + "' é inválida.");
         }
         return no;
     }
     
     public void Other(No no) {
         if (isToken("48")) {
             casaToken("48", false);
             No noBoolean = BooleanExpression();
             no.addFilho(noBoolean);
             Other(no);
             this.AND = true;
         } else if (isToken("49")) {
             casaToken("49", false);
             No noBoolean = BooleanExpression();
             no.addFilho(noBoolean);
             Other(no);
             this.OR = true;
         }
     }
     
     public No GroupByClausure() {
         No no = null;
         if (casaToken("6", false)) {
             if (casaToken("7", true)) {
                 this.usingGroupBy = true;
                 no = new No("group by");
                 GroupByExpression(no);
                 if (this.selecionarTudo) {
                     this.saida.add("Erro: A ferramenta não suporta selecionar * com group by na mesma consulta. Favor selecionar os campos explícitos ou remover o group by.");
                 }
             }
         }
         return no;
     }
     
     public void GroupByExpression(No no) {
         No noGroupByExpression = null;
         String lexema = this.token.getLexema();
         if (casaToken("41", true)) {
             if (casaToken("55", false)) {
                 String lexema2 = this.token.getLexema();
                 if (casaToken("41", true)) {
                     noGroupByExpression = new No(lexema2);
                     no.alias = lexema;
                     ordemGroupBy.add(lexema + "." + lexema2);
                 }
             } else {
                 noGroupByExpression = new No(lexema);
                 ordemGroupBy.add(lexema);
             }
             
             no.addFilho(noGroupByExpression);
             GroupItemAsterisco(no);
         }
     }
     
     public void GroupItemAsterisco(No no) {
         No noGroupItemAsterisco = null;
         if (casaToken("47", false)) {
             GroupByExpression(no);
//             String lexema = this.token.getLexema();
//             if (casaToken("41", true)) {
//                ordemGroupBy.add(lexema);
//                noGroupItemAsterisco = new No(lexema);
//                no.addFilho(noGroupItemAsterisco);
//                GroupItemAsterisco(no);
//             }
         }
     }
     
     public No OrderByClausure() {
         No no = null;
         if (casaToken("52", false)) {
             if (casaToken("7", true)) {
                 this.usingOrderBy = true;
                 no = new No("order by");
                 OrderByExpression(no);
             }
         }
         return no;
     }
     
     public void OrderByExpression(No no) {
         No noOrderByExpression = null;
         String lexema = this.token.getLexema();
         if (casaToken("41", true)) {
            if (casaToken("55", false)) {
                String lexema2 = this.token.getLexema();
                if (casaToken("41", true)) {
                    noOrderByExpression = new No(lexema2);
                    noOrderByExpression.alias = lexema;
                    lexema = lexema.toUpperCase().equals(this.tableAlias.toUpperCase()) ? lexema2 : lexema + "." + lexema2;
                    if (usingGroupBy) {
//                        String fieldName = lexema + "." + lexema2;
                        if (ordemGroupBy.contains(lexema) || ordemSelect.contains(lexema)) {
                            this.orderByDepois = true;
                            lexema = "_id." + lexema;
                        }
                    }
                }
            } else {
                noOrderByExpression = new No(lexema);
                if (usingGroupBy) {
                    if (ordemGroupBy.contains(lexema) || ordemSelect.contains(lexema)) {
                        this.orderByDepois = true;
                        lexema = "_id." + lexema;
                    }
                }
            }
            boolean order = Order();
            noOrderByExpression.code = order ? String.format("\"%s\": 1", lexema) : String.format("\"%s\": -1", lexema);
            no.addFilho(noOrderByExpression);
            OrderItemAsterisco(no);
         }
     }
     
     public boolean Order() {
         if (casaToken("53", false)) {
            return true; 
         } else if (casaToken("54", false)) {
            return false;
         }
         return true;
     }
     
     public void OrderItemAsterisco(No no) {
         if (casaToken("47", false)) {
             OrderByExpression(no);
         }
     }
}
