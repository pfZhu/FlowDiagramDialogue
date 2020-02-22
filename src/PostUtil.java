import net.sf.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostUtil {
    public static String post(String path, JSONObject jsonObject)throws Exception{
        HttpURLConnection httpConn=null;
        BufferedReader in=null;
        //PrintWriter out=null;
        try {
            String params="";
            if(jsonObject.size()>0) {
                for (Object key : jsonObject.keySet()) {
                    params += key + "=" + jsonObject.getString((String) key);
                    params += "&";
                }
                params = params.substring(0, params.length() - 1);
            }
            URL url=new URL(path);
            httpConn=(HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            //utf-8
            PrintWriter ot = new PrintWriter(new OutputStreamWriter(httpConn.getOutputStream(),"utf-8"));
            ot.println(params);
            ot.flush();
            //out=new PrintWriter(httpConn.getOutputStream());
            //out.println(params);

            //out.flush();

            //读取响应
            if(httpConn.getResponseCode()==HttpURLConnection.HTTP_OK){
                StringBuffer content=new StringBuffer();
                String tempStr="";
                //utf-8
                in=new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"utf-8"));
                while((tempStr=in.readLine())!=null){
                    content.append(tempStr);
                }
                return content.toString();
            }else{
                throw new Exception("请求出现了问题!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(in!=null)
                in.close();
            // out.close();
            httpConn.disconnect();
        }
        return null;
    }
}