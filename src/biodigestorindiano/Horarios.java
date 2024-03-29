package biodigestorindiano;
/**
 *
 * @author WELL1NGTON
 */
public class Horarios extends javax.swing.JFrame {

    /**
     * Creates new form Horarios
     */
    public Horarios() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Horários de uso de energia");
        setAlwaysOnTop(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Digite as informações de uso de equipamentos em diferentes intervalos de tempo"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null}
            },
            new String [] {
                "Horario inicio (h)", "Horario fim (h)", "Motores em uso", "Chuveiros em uso", "Lampiões acesos", "Fogões em uso"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("Confirmar dados");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("-1 linha");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("+1 linha");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Exemplo");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private int[][] tabela = null;
    
    boolean atualizaTabela(){
        javax.swing.table.TableModel modelo = jTable1.getModel();
        int numLinha = modelo.getRowCount(), numColuna = modelo.getColumnCount();
        boolean test = true;
        tabela = new int[numLinha][numColuna];
        int qtdCozimento, qtdMotor, qtdIluminacao, qtdBanho;
        
        //Coloca os valores digitais na matriz "tabela"
        for(int i = 0; i < numLinha && test; i++)
            for(int j = 0; j < numColuna && test; j++){
                try
                {
                    if( modelo.getValueAt(i, j) == null )
                    {
                        tabela[i][j] = 0;
                        modelo.setValueAt(0, i, j);
                    }
                    else
                    {
                        tabela[i][j] = Integer.parseInt(modelo.getValueAt(i, j).toString());
                        if(tabela[i][j] < 0)
                        {
                            test = false;
                            jTable1.setRowSelectionInterval(i, i);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    test = false;
                }
            }
        
        //Verifica a coerência dos horários
        if( tabela[tabela.length - 1][1] != tabela[0][0])
        {
            test = false;
            jTable1.setRowSelectionInterval(0, 0);
            jTable1.addRowSelectionInterval(tabela.length - 1, tabela.length - 1);
        }
                    
        for(int i = 1; i < tabela.length && test; i++)
            if(tabela[i][0] != tabela[i - 1][1])
            {
                test = false;
                jTable1.setRowSelectionInterval(i - 1, i);
            }
        
        return test;
    }
    
    public Dados janelaDados;
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if( atualizaTabela() )
        {
            setVisible(false);
            janelaDados.tabelaHorarios = tabela;
            janelaDados.setVisible(true);
        }
        else
            javax.swing.JOptionPane.showMessageDialog(null, "Use apenas números inteiros não negativos para preencher os dados.\nO horário final de cada período deve coincidir com o horário de início do período seguinte.\nObs.: O período seguinte ao último período, ciclicamente, é o primeiro período","Erro de entrada de dados.",javax.swing.JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        modelo.addRow(new Object[] {null, null, null, null, null, null});

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        if(jTable1.getRowCount() > 1)
            modelo.removeRow(jTable1.getRowCount() - 1);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        
        while(jTable1.getRowCount() != 7){
            if(jTable1.getRowCount() < 7)
                modelo.addRow(new Object[] {null, null, null, null, null, null});
            else
                 modelo.removeRow(jTable1.getRowCount() - 1);
        }
        
        modelo.setValueAt("22",0,0);
        modelo.setValueAt("5",0,1);
        modelo.setValueAt("0",0,2);//motores
        modelo.setValueAt("0",0,3);//chuveiros
        modelo.setValueAt("0",0,4);//Lampiões
        modelo.setValueAt("0",0,5);//fogões
        
        modelo.setValueAt("5",1,0);
        modelo.setValueAt("6",1,1);
        modelo.setValueAt("1",1,2);
        modelo.setValueAt("0",1,3);
        modelo.setValueAt("2",1,4);
        modelo.setValueAt("1",1,5);
        
        modelo.setValueAt("6",2,0);
        modelo.setValueAt("9",2,1);
        modelo.setValueAt("0",2,2);
        modelo.setValueAt("0",2,3);
        modelo.setValueAt("0",2,4);
        modelo.setValueAt("0",2,5);
        
        modelo.setValueAt("9",3,0);
        modelo.setValueAt("12",3,1);
        modelo.setValueAt("0",3,2);
        modelo.setValueAt("0",3,3);
        modelo.setValueAt("0",3,4);
        modelo.setValueAt("1",3,5);
        
        modelo.setValueAt("12",4,0);
        modelo.setValueAt("17",4,1);
        modelo.setValueAt("0",4,2);
        modelo.setValueAt("0",4,3);
        modelo.setValueAt("0",4,4);
        modelo.setValueAt("0",4,5);
        
        modelo.setValueAt("17",5,0);
        modelo.setValueAt("19",5,1);
        modelo.setValueAt("0",5,2);
        modelo.setValueAt("5",5,3);
        modelo.setValueAt("6",5,4);
        modelo.setValueAt("1",5,5);
        
        modelo.setValueAt("19",6,0);
        modelo.setValueAt("22",6,1);
        modelo.setValueAt("0",6,2);
        modelo.setValueAt("0",6,3);
        modelo.setValueAt("8",6,4);
        modelo.setValueAt("0",6,5);
        
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
