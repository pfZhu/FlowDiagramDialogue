import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

public class Graph{
    private HashMap<Integer,Node> nodes; //记录所有的节点
    private HashMap<Integer,Integer> classifier; //记录vi的分类器
    private HashMap<Collection<Integer>,Integer> class2classifier; //分类结果对应的分类器编号，key为ei的编号集合
    private int currentNode;

    private Graph(){
        nodes=new HashMap<>();
        classifier=new HashMap<>();
        class2classifier=new HashMap<>();
        currentNode=-1;
    }
    private static void addClassifier(Graph g, int vertexId, Collection<Integer> edges){
        if(g.class2classifier.containsKey(edges)){
            g.classifier.put(vertexId, g.class2classifier.get(edges));
        }
        else{
            int num=g.class2classifier.size();
            g.class2classifier.put(edges,num);
            g.classifier.put(vertexId,num);
        }
    }

    public static Graph generateGraph(String fileMessage,String fileProcedure){
        Graph g=new Graph();
        try {
            HashMap<Integer,Node> nodes=new HashMap<>();
            FileReader reader = new FileReader(fileMessage);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr=line.split("\t");
                if(arr.length==2){
                    if(Pattern.matches("^v\\d+$",arr[0])){
                        String vertex=arr[0];
                        int id=Integer.valueOf(vertex.substring(1));
                        Node node=new Node();
                        node.setId(id);
                        node.setMessage(arr[1]);
                        nodes.put(id,node);
                    }else
                        throw new Exception("Format error in message config file");
                }
            }
            reader=new FileReader(fileProcedure);
            br=new BufferedReader(reader);
            int currentId=-1;
            int lastId=-1;
            Collection<Integer> edges=new HashSet<>();
            while ((line = br.readLine()) != null) {
                String[] arr;
                if(Pattern.matches("^v\\d+$",line)) {
                    lastId=currentId;
                    currentId = Integer.valueOf(line.substring(1));
                    if(edges.size()>0){
                        addClassifier(g,lastId,edges);
                    }
                    edges=new HashSet<>();
                }
                else if((arr=line.split("\t")).length==2){
                    if(Pattern.matches("^e\\d+$",arr[0]) && Pattern.matches("^v\\d+$",arr[1])){
                        int edgeNum=Integer.valueOf(arr[0].substring(1));
                        int vertexId=Integer.valueOf(arr[1].substring(1));
                        nodes.get(currentId).addEdgeVertex(edgeNum,vertexId);
                        edges.add(edgeNum);
                    }else{
                        throw new Exception("Format Error in procedure config file");
                    }
                }
            }
            if(edges.size()>0){
                addClassifier(g,currentId,edges);
            }
            g.setNodes(nodes);
            g.setCurrentNode(32);
            JSONObject classifier2class=new JSONObject();
            for(Collection<Integer> cls: g.class2classifier.keySet()){
                if(cls.size()>1)
                    classifier2class.put(g.class2classifier.get(cls),cls);
            }
            JSONObject param=new JSONObject();
            param.put("classifier2class",classifier2class.toString());
            String rtn=PostUtil.post("http://127.0.0.1:5000/init",param);
            JSONObject object=JSONObject.fromObject(rtn);
            if(object.getInt("code")!=200)
                throw new Exception("initializing classifier failed!");
        } catch (Exception e) {
            e.printStackTrace();
        };
        return g;
    }

    public int getCurrentNodeCounter(){
        return this.nodes.get(this.currentNode).getCounter();
    }

    public String getCurrentMessage(){
        return this.nodes.get(this.currentNode).getMessage();
    }


    public int getCurrentNodeId(){
        return currentNode;
    }

    public void updateState(String info){
        Node currentNode=this.nodes.get(this.currentNode);
        if(currentNode.getNextNodes().size()==1){
            this.currentNode=currentNode.getNextNodes().get(0);
        }
        else if(currentNode.getNextNodes().size()>0) {
            int classifierId = this.classifier.get(this.currentNode);
            int edgeNum = classifyInfo(info, classifierId);
            if(edgeNum==42){
                int nextNode=doEvaluation();
                setCurrentNode(nextNode);
            }
            else {
                List<Integer> edges = currentNode.getEdges();
                int i = 0;
                for (i = 0; i < edges.size(); i++) {
                    if (edges.get(i) == edgeNum)
                        break;
                }
                setCurrentNode(currentNode.getNextNodes().get(i));
            }
        }

    }

    private int doEvaluation() {
        Random rand=new Random();
        return 33+rand.nextInt(2);
    }

    private int classifyInfo(String info, int classifierId) {
        JSONObject param=new JSONObject();
        param.put("classifier_num",classifierId);
        param.put("message",info);
        JSONObject object=new JSONObject();
        try {
            String rtn=PostUtil.post("http://127.0.0.1:5000/predict", param);
            object=JSONObject.fromObject(rtn);
        }catch (Exception e){
            e.printStackTrace();
        }
        return object.getInt("label");
    }

    private void setNodes(HashMap<Integer, Node> nodes) {
        this.nodes = nodes;
    }


    private void setCurrentNode(int currentNode) {
        this.currentNode = currentNode;
        this.nodes.get(currentNode).addCount();
    }


}

class Node{
    private int id;
    private String message;
    private List<Integer> nextNodes; //与edges一一对应
    private List<Integer> edges;
    private int counter=0;

    public Node(){
        nextNodes=new ArrayList<>();
        edges=new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNextNodes(List<Integer> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public void setEdges(List<Integer> edges) {
        this.edges = edges;
    }
    public void addEdgeVertex(int edgeNum,int vertexId){
        edges.add(edgeNum);
        nextNodes.add(vertexId);
    }

    public void addCount(){
        this.counter++;
    }

    public int getId() {
        return id;
    }

    public int getCounter() { return counter;}

    public String getMessage() {
        return message;
    }

    public List<Integer> getNextNodes() {
        return nextNodes;
    }

    public List<Integer> getEdges() {
        return edges;
    }
}