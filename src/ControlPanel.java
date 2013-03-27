
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JSlider;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mahefa
 */
public class ControlPanel extends javax.swing.JFrame {

    private JSlider[] sliders;
    private JLabel[] mins, maxs, values;
    private Agent agent;
    private DecimalFormat df = new DecimalFormat("0.00000");
    
    public ControlPanel(Agent agent){
        this();
        this.agent = agent;
        for(int i=0; i<5; i++) updateSlider(i);
    }
    
    public ControlPanel() {
        initComponents();
        sliders = new JSlider[]{this.slideReward, this.slideAlpha,
            this.slideGamma, this.slideLambda, this.slideUpsilon};
        mins = new JLabel[]{this.minReward, this.minAlpha,
            this.minGamma, this.minLambda, this.minEpsilon};
        maxs = new JLabel[]{this.maxReward, this.maxAlpha,
            this.maxGamma, this.maxLambda, this.maxEpsilon};
        values = new JLabel[]{this.testReward, this.testAlpha,
            this.textGamma, this.textLambda, this.textEpsilon};
    }

    private void updateSlider(int index){
        double min = Double.parseDouble(mins[index].getText());
        double max = Double.parseDouble(maxs[index].getText());
        int slideVal = sliders[index].getValue();
        int slideMin = sliders[index].getMinimum();
        int slideMax = sliders[index].getMaximum();
        double value = (max-min)/(double)(slideMax-slideMin)*(double)(slideVal)+min;
        values[index].setText(df.format(value));
        switch(index){
            case 0 : agent.setReward(value); break;
            case 1 : agent.setAlpha(value); break;
            case 2 : agent.setGamma(value); break;
            case 3 : agent.setLambda(value); break;
            case 4 : agent.setEpsilon(value); break;
            default : break;
        }
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        slideAlpha = new javax.swing.JSlider();
        minAlpha = new javax.swing.JLabel();
        maxAlpha = new javax.swing.JLabel();
        testAlpha = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        slideGamma = new javax.swing.JSlider();
        minGamma = new javax.swing.JLabel();
        maxGamma = new javax.swing.JLabel();
        textGamma = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        slideLambda = new javax.swing.JSlider();
        minLambda = new javax.swing.JLabel();
        maxLambda = new javax.swing.JLabel();
        textLambda = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        slideUpsilon = new javax.swing.JSlider();
        minEpsilon = new javax.swing.JLabel();
        maxEpsilon = new javax.swing.JLabel();
        textEpsilon = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        minReward = new javax.swing.JLabel();
        slideReward = new javax.swing.JSlider();
        maxReward = new javax.swing.JLabel();
        testReward = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Alpha");

        slideAlpha.setMaximum(10000);
        slideAlpha.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideAlphaStateChanged(evt);
            }
        });

        minAlpha.setText("0.0");

        maxAlpha.setText("1.0");

        testAlpha.setBackground(new java.awt.Color(255, 255, 255));
        testAlpha.setText("0.00000");

        jLabel5.setText("Gamma");

        slideGamma.setMaximum(10000);
        slideGamma.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideGammaStateChanged(evt);
            }
        });

        minGamma.setText("0.0");

        maxGamma.setText("1.0");

        textGamma.setBackground(new java.awt.Color(255, 255, 255));
        textGamma.setText("0.00000");

        jLabel9.setText("Lambda");

        slideLambda.setMaximum(10000);
        slideLambda.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideLambdaStateChanged(evt);
            }
        });

        minLambda.setText("0.0");

        maxLambda.setText("1.0");

        textLambda.setBackground(new java.awt.Color(255, 255, 255));
        textLambda.setText("0.00000");

        jLabel13.setText("Epsilon");

        slideUpsilon.setMaximum(10000);
        slideUpsilon.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideUpsilonStateChanged(evt);
            }
        });

        minEpsilon.setText("0.0");

        maxEpsilon.setText("1.0");

        textEpsilon.setBackground(new java.awt.Color(255, 255, 255));
        textEpsilon.setText("0.00000");

        jLabel17.setText("Reward");

        minReward.setText("-10.0");

        slideReward.setMaximum(10000);
        slideReward.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slideRewardStateChanged(evt);
            }
        });

        maxReward.setText("0.0");

        testReward.setBackground(new java.awt.Color(255, 255, 255));
        testReward.setText("0.00000");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(minAlpha)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(maxAlpha))
                                    .addComponent(slideAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(testAlpha))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(minGamma)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(maxGamma))
                                    .addComponent(slideGamma, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textGamma)))
                        .addGap(7, 7, 7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(minLambda)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(maxLambda))
                            .addComponent(slideLambda, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textLambda)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(minEpsilon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(maxEpsilon))
                            .addComponent(slideUpsilon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textEpsilon)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minReward)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(maxReward))
                            .addComponent(slideReward, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(testReward)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testReward)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(slideReward, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minReward)
                            .addComponent(maxReward))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testAlpha)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(slideAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minAlpha)
                            .addComponent(maxAlpha))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textGamma)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(slideGamma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minGamma, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(maxGamma))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textLambda)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(slideLambda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minLambda, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(maxLambda))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textEpsilon)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(slideUpsilon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minEpsilon, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(maxEpsilon)))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void slideRewardStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideRewardStateChanged
        JSlider source = (JSlider)evt.getSource();
        if(!source.getValueIsAdjusting()){
            updateSlider(0);
        }
    }//GEN-LAST:event_slideRewardStateChanged

    private void slideAlphaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideAlphaStateChanged
        JSlider source = (JSlider)evt.getSource();
        if(!source.getValueIsAdjusting()){
            updateSlider(1);
        }
    }//GEN-LAST:event_slideAlphaStateChanged

    private void slideGammaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideGammaStateChanged
        JSlider source = (JSlider)evt.getSource();
        if(!source.getValueIsAdjusting()){
            updateSlider(2);
        }
    }//GEN-LAST:event_slideGammaStateChanged

    private void slideLambdaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideLambdaStateChanged
        JSlider source = (JSlider)evt.getSource();
        if(!source.getValueIsAdjusting()){
            updateSlider(3);
        }
    }//GEN-LAST:event_slideLambdaStateChanged

    private void slideUpsilonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideUpsilonStateChanged
        JSlider source = (JSlider)evt.getSource();
        if(!source.getValueIsAdjusting()){
            updateSlider(4);
        }
    }//GEN-LAST:event_slideUpsilonStateChanged

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ControlPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ControlPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ControlPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ControlPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ControlPanel().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel maxAlpha;
    private javax.swing.JLabel maxEpsilon;
    private javax.swing.JLabel maxGamma;
    private javax.swing.JLabel maxLambda;
    private javax.swing.JLabel maxReward;
    private javax.swing.JLabel minAlpha;
    private javax.swing.JLabel minEpsilon;
    private javax.swing.JLabel minGamma;
    private javax.swing.JLabel minLambda;
    private javax.swing.JLabel minReward;
    private javax.swing.JSlider slideAlpha;
    private javax.swing.JSlider slideGamma;
    private javax.swing.JSlider slideLambda;
    private javax.swing.JSlider slideReward;
    private javax.swing.JSlider slideUpsilon;
    private javax.swing.JLabel testAlpha;
    private javax.swing.JLabel testReward;
    private javax.swing.JLabel textEpsilon;
    private javax.swing.JLabel textGamma;
    private javax.swing.JLabel textLambda;
    // End of variables declaration//GEN-END:variables
}
