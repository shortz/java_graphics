import java.io.*;
import java.util.*;

public class Pipe implements Runnable{
  InputStream in;
  OutputStream out;
  boolean check_http;

  public Pipe(InputStream in,OutputStream out, int port) {
    check_http = (port == 80);
    this.in = in;
    this.out = out;
  }

  public void run(){
    byte[] buf = new byte[8024];
    int len = 0;
    while(len >= 0){
       try{
          if(len!=0){
            out.write(buf,0,len);
            out.flush();
          }
          len = in.read(buf);
          if (check_http && len > 0){
            check_http_auth(buf);
          }
       }catch(IOException iioe){
          return;
       }
    }
  }

  private void check_http_auth(byte[] msg){
    BufferedReader msg_reader = conv_bytes_to_reader(msg);
    if (msg_reader == null){
      return;
    }

    try {
      String line = msg_reader.readLine();
      if (http_is_get(line)){
        print_basic_creds(msg_reader, get_path(line));
      }
    } catch (Exception e) {
      return;
    }
  }

  private void print_basic_creds(BufferedReader msg_reader, String path){
    String line;
    String host;
    String creds;
    try{
      line = msg_reader.readLine();
      host = get_host(line);
      line = msg_reader.readLine();
      creds = get_creds(line);
    } catch (Exception e) {
      return;
    }
    System.out.println("Password Found! http://" + creds + "/" + host + path);
  }

  private boolean http_is_get(String line){
    String[] s = line.split(" ");
    if (s[0].equals("GET")){
      return true;
    }
    return false;
  }

  private String get_path(String line){
    String[] s = line.split(" ");
    return s[1];
  }

  private String get_host(String line){
    String[] s = line.split(" ");
    return s[1];
  }

  private String get_creds(String line){
    String[] s = line.split(" ");
    byte[] decodedBytes = Base64.getDecoder().decode(s[2].getBytes());
    String result = new String(decodedBytes);
    return result;
  }

  private BufferedReader conv_bytes_to_reader(byte[] byte_arr){
    BufferedReader bfReader = null;
    InputStream is = null;
    try {
      is = new ByteArrayInputStream(byte_arr);
      bfReader = new BufferedReader(new InputStreamReader(is));
    } catch (Exception e) {
      return null;
    }
    return bfReader;
  }
}