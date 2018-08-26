/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author roshan
 */
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uploadLocation = req.getHeader("Path");
        File file = new File(uploadLocation+"/uploadedfile");
        InputStream in = req.getInputStream();
        FileOutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int r;
        
        while( (r = in.read(buf, 0, buf.length)) != -1){
            out.write(buf, 0, r);
        }
        in.close();
        out.close();
    }
}
