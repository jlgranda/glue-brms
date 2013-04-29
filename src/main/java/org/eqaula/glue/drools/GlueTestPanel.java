/*
 * Copyright 2013 jlgranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eqaula.glue.drools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author jlgranda
 */
public class GlueTestPanel extends JPanel {

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();

        f.setContentPane(new GlueTestPanel());
        f.setSize(700, 500);
        f.setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);
        //Draws the line
        String color = "gray"; //Default color
        Field field = null;
        try {
            field = Class.forName("java.awt.Color").getField(color);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GlueTestPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(GlueTestPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(GlueTestPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graphics2D g2 = (Graphics2D) g;
        try {
            g2.setColor((Color) field.get(null));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GlueTestPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GlueTestPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
        Ellipse2D.Float sign1 = new Ellipse2D.Float(0F, 0F, 32F, 32F);
        g2.fill(sign1);

        Font exFont = new Font("TimesRoman", Font.PLAIN, 10);
        g2.setFont(exFont);
        g2.setColor(Color.black);
        g2.drawString("JavaWorld", 0.0f, 0.0f);
    }
}
