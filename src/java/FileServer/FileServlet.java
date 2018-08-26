/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author roshan
 */
public class FileServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject json = new JSONObject(new String(readAll(request.getInputStream())));
        System.out.println(json);
        JSONObject jsonResponse = new JSONObject();
        switch (json.optString("action")) {
            case "copy":
                ArrayList<String> copyErrors = copyFile(json.optString("src"), json.optString("dest"));
                jsonResponse.put("errors", copyErrors);
                jsonResponse.put("status", copyErrors.isEmpty() ? 0 : 1);
                break;
            case "rename":
                boolean renamed = new File(json.optString("src")).renameTo(new File(json.optString("dest")));
                jsonResponse.put("status", renamed ? 0 : 1);
                break;
            case "delete":
                ArrayList<String> deleteErrors = deleteFile(json.optString("path"));
                jsonResponse.put("errors", deleteErrors);
                jsonResponse.put("status", deleteErrors.isEmpty() ? 0 : 1);
                break;
            case "list":
                String path = json.optString("path");
                if (path.equals("root")) {
                    jsonResponse.put("parent", (Object) null);
                    jsonResponse.put("children", probeRoots());
                } else {
                    jsonResponse.put("parent", path);
                    jsonResponse.put("children", listFiles(path));
                }
                jsonResponse.put("status", 0);
                break;
            default:
                jsonResponse.put("status", 101);
        }
        response.getOutputStream().write(jsonResponse.toString().getBytes());
    }

    private ArrayList<String> probeRoots() {
        ArrayList<String> roots = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            File file = new File(c + ":");
            if (file.exists()) {
                roots.add(file.getAbsolutePath().replace("\\", "/"));
            }
        }
        return roots;
    }

    private byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pipeAllData(in, out);
        return out.toByteArray();
    }

    private void pipeAllData(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[8192];
        int r;
        while ((r = in.read(buff)) != -1) {
            out.write(buff, 0, r);
        }
    }

    private JSONArray listFiles(String parent) {
        File parentFile = new File(parent);
        JSONArray files = new JSONArray();
        for (File file : parentFile.listFiles()) {
            files.put(file.getAbsolutePath().replace("\\", "/"));
        }
        return files;
    }

    private void deleteFile(File file, ArrayList<String> errors) {
        if (file.isFile()) {
            if (!file.delete()) {
                System.out.println("2: " + file.getAbsolutePath().replace("\\", "/"));
                errors.add(file.getAbsolutePath().replace("\\", "/"));
            }
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFile(child.getAbsolutePath().replace("\\", "/"));
            }
        }
        if (!file.delete()) {
            System.out.println("3");
            errors.add(file.getAbsolutePath().replace("\\", "/"));
        }
    }

    private ArrayList<String> deleteFile(String path) {
        File file = new File(path);
        ArrayList<String> errors = new ArrayList<>();
        deleteFile(file, errors);
        return errors;
    }

    private boolean copyFileSingle(File src, File dest) {
        if (src.isDirectory() && dest.isDirectory()) {
            throw new IllegalArgumentException();
        }
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);
            pipeAllData(in, out);
            out.close();
            in.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void copy(File srcFile, File destFile, ArrayList<String> errors) {
        if (srcFile.isFile()) {
            if (destFile.isFile()) {
                if (!copyFileSingle(srcFile, new File(destFile, srcFile.getName()))) {
                    errors.add(srcFile.getAbsolutePath().replace("\\", "/"));
                }
                return;
            }
            destFile.mkdirs();
            if (!copyFileSingle(srcFile, new File(destFile, srcFile.getName()))) {
                errors.add(srcFile.getAbsolutePath().replace("\\", "/"));
            }
            return;
        }
        for (File child : srcFile.listFiles()) {
            copy(child, destFile, errors);
        }
    }

    private ArrayList<String> copyFile(String src, String dest) {
        System.out.println("copy: " + src + ": " + dest);
        ArrayList<String> errors = new ArrayList<>();
        copy(new File(src), new File(dest), errors);
        return errors;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
