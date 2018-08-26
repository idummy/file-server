/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileServer;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author roshan
 */


public class ServerServlet extends HttpServlet {

    String response;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        String action = req.getHeader("Request-Type");
        if (action != null && action.equalsIgnoreCase("upload")) {
            String uploadLocation = req.getHeader("Path");
            File file = new File(uploadLocation);
            InputStream in = req.getInputStream();
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int r;

            while ((r = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, r);
            }
            in.close();
            out.close();
            responseJson.put("status", "ok");

        } else {
            InputStream in = req.getInputStream();
            byte[] buf = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            while ((read = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, read);
            }
            String inString = out.toString();
            JSONObject json = new JSONObject(inString);
            String request = json.getString("request");
            String source = json.getString("source");

            switch (request) {
                case "listroot": {
                    ArrayList<File> files = listRoots();
                    JSONArray rootsJson = new JSONArray();
                    for (File file : files) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("name", file.getAbsolutePath());
                        jSONObject.put("type", file.getAbsoluteFile().isDirectory() ? "dir" : "file");
                        rootsJson.put(jSONObject);
                    }
                    JSONObject replyJson = new JSONObject();
                    replyJson.put("files",rootsJson);
                    replyJson.put("status", "OK");
                    resp.getOutputStream().write(replyJson.toString().getBytes());
                }
                break;
                case "list": {
                    JSONArray fJSONArray = list(source);
                    JSONObject replyJson = new JSONObject();
                    replyJson.put("files", fJSONArray);
                    replyJson.put("status", "OK");
                    resp.getOutputStream().write(replyJson.toString().getBytes());
                }
                break;
                case "rename": {
                    String target = json.getString("target");
                    File file = new File(source);
                    File nFile = new File(target);
                    responseJson = file.renameTo(nFile) ? status(true) : status(false);
                    resp.getOutputStream().write(responseJson.toString().getBytes());
                }
                break;
                case "delete": {
                    File target = new File(source);
                    responseJson = target.delete() ? status(true) : status(false);
                    resp.getOutputStream().write(responseJson.toString().getBytes());
                }
                break;
                case "copy": {
                    String target = json.getString("target");
                    boolean status;
                    try {
                        FileInputStream fileIn = new FileInputStream(source);
                        FileOutputStream fileOut = new FileOutputStream(target);
                        int r;
                        while ((r = fileIn.read(buf, 0, buf.length)) != -1) {
                            fileOut.write(buf, 0, r);
                        }
                        fileIn.close();
                        fileOut.close();
                        status = true;
                    } catch (Exception ex) {
                        status = false;
                    }
                    responseJson = status ? status(true) : status(false);
                    resp.getOutputStream().write(responseJson.toString().getBytes());
                }
                break;
                case "download": {
                    boolean status;
                    ServletOutputStream sOut = resp.getOutputStream();
                    try {
                        File file = new File(source);
                        FileInputStream fIn = new FileInputStream(file);

                        int r;
                        while ((r = fIn.read(buf, 0, buf.length)) != -1) {
                            sOut.write(buf, 0, r);
                        }
                        fIn.close();
                        status = true;
                    } catch (Exception ex) {
                        status = false;
                    }
//                    responseJson = status ? status(true) : status(false);
//                    sOut.write(responseJson.toString().getBytes());
                }
                break;
                default:
                    responseJson.put("status", "invalid action");
                    resp.getOutputStream().write(responseJson.toString().getBytes());
            }
        }
    }

    private JSONArray list(String source) {
        File file = new File(source);
        JSONArray filesJson = new JSONArray();
//      String[] files = file.list();
//       for (String f : files) {
//            File file1 = new File(f);
//            String filePath = file1.getAbsolutePath();
//            String type = file1.isDirectory() ? "dir" : "file";
//            JSONObject fileJson = new JSONObject();
//            fileJson.put("file", f);
//            fileJson.put("type", type);
//            filesJson.put(fileJson);
//        }
        for (File f : file.getAbsoluteFile().listFiles()) {
            JSONObject json = new JSONObject();
            json.put("file", f.getAbsolutePath());
            json.put("type", f.getAbsoluteFile().isDirectory() ? "dir" : "file");
            filesJson.put(json);
        }
        return filesJson;
    }

    private JSONObject status(boolean status) {
        JSONObject replyJson = new JSONObject();
        if (status) {
            replyJson.put("status", "Ok");
        } else {
            replyJson.put("status", "failed");
        }
        return replyJson;
    }

    private ArrayList<File> listRoots() {
        ArrayList<File> files = new ArrayList<>();
        for (char c = 'A'; c != 'Z'; c++) {
            File file = new File(c + ":\\");
            if (file.exists()) {
                files.add(file);
            }
        }
        return files;
    }
}
