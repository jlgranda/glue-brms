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
package org.eqaula.glue.faces;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.sound.midi.Soundbank;
import org.apache.commons.io.IOUtils;
import org.eqaula.glue.model.management.Method;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author jlgranda
 */
@Named
@RequestScoped
public class MethodPrinter implements Serializable {

    private static final long serialVersionUID = 3045720219325097581L;
    private StreamedContent graphicText = null;

    public StreamedContent print(Method method) {

        try {

            if (Method.Type.SEMAPHORE.equals(method.getMethodType())) {

                // So, browser is requesting the image. Get ID value from actual request param.
                BufferedImage bufferedImg = new BufferedImage(100, 25, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bufferedImg.createGraphics();
                g2.drawString(method.getTarget().getCurrentValue().toString(), 0, 10);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImg, "png", os);
                graphicText = new DefaultStreamedContent(new ByteArrayInputStream(os.toByteArray()), "image/png");
                System.out.println("---> image generated for method " + method);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return graphicText;
    }

    public Method printToDownload(Method method) {

        try {

            if (Method.Type.SEMAPHORE.equals(method.getMethodType())) {

                // So, browser is requesting the image. Get ID value from actual request param.
                BufferedImage bufferedImg = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bufferedImg.createGraphics();
                String color = "gray"; //Default color
                if (!method.getWrappers().isEmpty()) {
                    color = method.getWrappers().get(0).getValue();
                }
                Field field = Class.forName("java.awt.Color").getField(color);
                g2.setColor((Color) field.get(null));
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Ellipse2D.Float sign1 = new Ellipse2D.Float(0F, 0F, 32F, 32F);
                g2.fill(sign1);
                //g2.drawString(method.getTarget().getCurrentValue().toString(), 0, 10);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImg, "png", os);

                writeStreamedContentToFile(method, new ByteArrayInputStream(os.toByteArray()));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return method;
    }
    private static final String UPLOAD_DIRECTORY = "method";

    public String writeStreamedContentToFile(Method method, ByteArrayInputStream byteArrayInputStream) {
        String filePath = "glue";
        try {
            // constructs the directory path to store upload file
            // this path is relative to application's directory
            FacesContext context = FacesContext.getCurrentInstance();
            String uploadPath = context.getExternalContext().getRealPath("")
                    + File.separator + UPLOAD_DIRECTORY;

            // creates the directory if it does not exist
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String fileName = (new File(method.getId().toString())).getName() + ".png";

            filePath = uploadPath + File.separator + fileName;

            File storeFile = new File(filePath);
            // if file doesnt exists, then create it
            if (!storeFile.exists()) {
                storeFile.createNewFile();
            }

            IOUtils.copy(byteArrayInputStream, new FileOutputStream(storeFile));

        } catch (IOException ex) {
            Logger.getLogger(MethodPrinter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filePath;
    }
}
