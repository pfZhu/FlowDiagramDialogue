import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        Graph g= Graph.generateGraph("/Users/zhupf/Documents/GitHub/FlowDiagramDialogue/config/config_messages.txt","/Users/zhupf/Documents/GitHub/FlowDiagramDialogue/config/config_procedure.txt");
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String info="";
        String prefix="";
        String message="";
        while (!info.equals("quit")){
            message=g.getCurrentMessage();
            if(!prefix.equals("")) {
                message = prefix + message;
                prefix="";
            }
            System.out.println(message);
            int currentNodeId=g.getCurrentNodeId();
            if(currentNodeId==16 || currentNodeId==17|| currentNodeId==18 || currentNodeId==19 )
                prefix="好的我这里记下了。";
            try{
                info = br.readLine();
                g.updateState(info);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}


