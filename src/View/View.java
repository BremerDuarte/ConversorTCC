/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Lexan;
import main.Parser;
import main.Token;

/**
 *
 * @author Bremer
 */
public class View extends javax.swing.JFrame {

    /**
     * Creates new form View
     */
    public View() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        resultTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        sqlTextArea = new javax.swing.JTextArea();
        converterButton = new javax.swing.JButton();
        limparButton = new javax.swing.JButton();
        saidaLabel = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        resultTextArea.setColumns(20);
        resultTextArea.setRows(5);
        jScrollPane1.setViewportView(resultTextArea);

        sqlTextArea.setColumns(20);
        sqlTextArea.setRows(5);
        jScrollPane2.setViewportView(sqlTextArea);

        converterButton.setText("Converter");
        converterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                converterButtonActionPerformed(evt);
            }
        });

        limparButton.setText("Limpar");
        limparButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limparButtonActionPerformed(evt);
            }
        });

        saidaLabel.setText("Saída:");

        sqlLabel.setText("Consulta SQL:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saidaLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sqlLabel))
                .addContainerGap(44, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(limparButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(converterButton)
                .addGap(46, 46, 46))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(sqlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limparButton, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(converterButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saidaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void converterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_converterButtonActionPerformed
        String query = sqlTextArea.getText();
        Lexan lexan = new Lexan();
        String resultText = "Iniciando análise léxica...\n";
        resultTextArea.setText(resultText);
        boolean erro = false;
        String errorMessage = "";
        for (char c : query.toCharArray()) {
            errorMessage = lexan.ReadChar(c);
            if (errorMessage.length() > 0) {
                erro = true;
                break;
            }
        }
        if (!erro) {
            errorMessage = lexan.EnqueueLastToken();
            if (errorMessage.length() > 0) {
                erro = true;
            }
        }
        if (erro) {
            resultTextArea.setForeground(Color.red);
            resultText += "Análise léxica falhou...\n";
            resultText += errorMessage + "\n";
            resultTextArea.setText(resultText);
        } else {
            resultTextArea.setForeground(Color.black);
            resultText += "Análise léxica concluída com sucesso...\n\n";
            resultText += "Iniciando análise sintática...\n";
            Parser parser = new Parser(lexan.Tokens);
            if (parser.saida.size() > 0) {
                resultTextArea.setForeground(Color.red);
                resultText += "Análise sintática falhou...\n\n";
                resultText += parser.saida.get(0);
            } else {
                resultText += "Análise sintática concluída com sucesso...\n\n";
                resultText += "Consulta MongoDB: \n\n" + parser.queryMongoDB;
            }
//            resultText += "Tokens gerados: \n\n";
//            while (!lexan.Tokens.isEmpty()) {
//                try {
//                    Token t = lexan.Tokens.dequeue();
//                    resultText += t.getNome() + " ";
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            resultTextArea.setText(resultText);
        }
    }//GEN-LAST:event_converterButtonActionPerformed

    private void limparButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limparButtonActionPerformed
        resultTextArea.setText("");
        sqlTextArea.setText("");
    }//GEN-LAST:event_limparButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(View.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new View().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton converterButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton limparButton;
    private javax.swing.JTextArea resultTextArea;
    private javax.swing.JLabel saidaLabel;
    private javax.swing.JLabel sqlLabel;
    private javax.swing.JTextArea sqlTextArea;
    // End of variables declaration//GEN-END:variables
}
