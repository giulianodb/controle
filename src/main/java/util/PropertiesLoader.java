package util;
import java.io.IOException;  
import java.io.InputStream;  
import java.util.Properties;  
  
public class PropertiesLoader {  
  
    private Properties props;  
    private String nomeDoProperties = "config.properties";  
  
    public PropertiesLoader(){  
            props = new Properties();  
            InputStream in = this.getClass().getResourceAsStream(nomeDoProperties);  
            try{  
                    props.load(in);  
                    in.close();  
            }  
            catch(IOException e){e.printStackTrace();}  
    }  
  
    public String getValor(String chave){  
            return (String)props.getProperty(chave);  
    }  
}