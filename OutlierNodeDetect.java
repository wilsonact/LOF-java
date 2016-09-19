import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;  
  
/** 
 * 离群点分析 
 *  
 * @author Wilson 
 * 算法：基于密度的局部离群点检测（lof算法）  
 * 输入：样本集合D，正整数K（用于计算第K距离） 
 * 输出：各样本点的局部离群点因子  
 * 过程： 
 *  1）计算每个对象与其他对象的欧几里得距离  
 *  2）对欧几里得距离进行排序，计算第k距离以及第K领域 
 *  3）计算每个对象的可达密度  
 *  4）计算每个对象的局部离群点因子 
 *  5）对每个点的局部离群点因子进行排序，输出。 
 **/  
public class OutlierNodeDetect {  
	
    private int INT_K = 4;//正整数K  
    public void setK(int int_k) {  
    	this.INT_K = int_k;  
    }  
    // 1.找到给定点与其他点的欧几里得距离  
    // 2.对欧几里得距离进行排序，找到前5位的点，并同时记下k距离  
    // 3.计算每个点的可达密度  
    // 4.计算每个点的局部离群点因子  
    // 5.对每个点的局部离群点因子进行排序，输出。  
    public List<DataNode> getOutlierNode(List<DataNode> allNodes) {  
  
        List<DataNode> kdAndKnList = getKDAndKN(allNodes);  
        calReachDis(kdAndKnList);  
        calReachDensity(kdAndKnList);  
        calLof(kdAndKnList);  
        //降序排序  
        Collections.sort(kdAndKnList, new LofComparator());  
  
        return kdAndKnList;  
    }  
  
    /** 
     * 计算每个点的局部离群点因子 
     * @param kdAndKnList 
     */  
    private void calLof(List<DataNode> kdAndKnList) {  
        for (DataNode node : kdAndKnList) {  
            List<DataNode> tempNodes = node.getkNeighbor();  
            double sum = 0.0;  
            for (DataNode tempNode : tempNodes) {  
                double rd = getRD(tempNode.getNodeName(), kdAndKnList);  
                sum = rd / node.getReachDensity() + sum;  
            }  
            sum = sum / (double) INT_K;  
            node.setLof(sum);
        }  
    }  
  
    /** 
     * 计算每个点的可达距离 
     * @param kdAndKnList 
     */  
    private void calReachDensity(List<DataNode> kdAndKnList) {  
        for (DataNode node : kdAndKnList) {  
            List<DataNode> tempNodes = node.getkNeighbor();  
            double sum = 0.0;  
            double rd = 0.0;  
            for (DataNode tempNode : tempNodes) {  
                sum = tempNode.getReachDis() + sum;  
            }
            rd = (double) INT_K / sum;
            node.setReachDensity(rd); 
        }
    }
      
    /** 
     * 计算每个点的可达密度,reachdis(p,o)=max{ k-distance(o),d(p,o)} 
     * @param kdAndKnList 
     */  
    private void calReachDis(List<DataNode> kdAndKnList) {  
        for (DataNode node : kdAndKnList) {  
            List<DataNode> tempNodes = node.getkNeighbor();  
            for (DataNode tempNode : tempNodes) {  
                //获取tempNode点的k-距离  
                double kDis = getKDis(tempNode.getNodeName(), kdAndKnList);  
                //reachdis(p,o)=max{ k-distance(o),d(p,o)}  
                if (kDis < tempNode.getDistance()) {  
                    tempNode.setReachDis(tempNode.getDistance());  
                } else {  
                    tempNode.setReachDis(kDis);  
                }  
            }  
        }  
    }  
  
    /** 
     * 获取某个点的k-距离（kDistance） 
     * @param nodeName 
     * @param nodeList 
     * @return 
     */  
    private double getKDis(String nodeName, List<DataNode> nodeList) {  
        double kDis = 0;  
        for (DataNode node : nodeList) {  
            if (nodeName.trim().equals(node.getNodeName().trim())) {  
                kDis = node.getkDistance();  
                break;  
            }  
        }  
        return kDis;  
  
    }  
  
    /** 
     * 获取某个点的可达距离 
     * @param nodeName 
     * @param nodeList 
     * @return 
     */  
    private double getRD(String nodeName, List<DataNode> nodeList) {  
        double kDis = 0;  
        for (DataNode node : nodeList) {  
            if (nodeName.trim().equals(node.getNodeName().trim())) {  
                kDis = node.getReachDensity();  
                break;  
            }  
        }  
        return kDis;  
  
    }  
      
    /** 
     * 计算给定点NodeA与其他点NodeB的欧几里得距离（distance）,并找到NodeA点的前5位NodeB，然后记录到NodeA的k-领域（kNeighbor）变量。 
     * 同时找到NodeA的k距离，然后记录到NodeA的k-距离（kDistance）变量中。 
     * 处理步骤如下： 
     * 1,计算给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。 
     * 2,对所有NodeB点中的distance进行升序排序。 
     * 3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。 
     * 4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。 
     * @param allNodes 
     * @return List<Node> 
     */  
    private List<DataNode> getKDAndKN(List<DataNode> allNodes) {  
        List<DataNode> kdAndKnList = new ArrayList<DataNode>();  
        for (int i = 0; i < allNodes.size(); i++) {  
            List<DataNode> tempNodeList = new ArrayList<DataNode>();  
            DataNode nodeA = new DataNode(allNodes.get(i).getNodeName(), allNodes  
                    .get(i).getDimensioin());  
            //1,找到给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。  
            for (int j = 0; j < allNodes.size(); j++) {  
                DataNode nodeB = new DataNode(allNodes.get(j).getNodeName(), allNodes  
                        .get(j).getDimensioin());  
                //计算NodeA与NodeB的欧几里得距离(distance)  
                double tempDis = getDis(nodeA, nodeB);  
                nodeB.setDistance(tempDis);  
                tempNodeList.add(nodeB);  
            }  
  
            //2,对所有NodeB点中的欧几里得距离（distance）进行升序排序。  
            Collections.sort(tempNodeList, new DistComparator());  
            for (int k = 1; k < INT_K; k++) {  
                //3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。  
                nodeA.getkNeighbor().add(tempNodeList.get(k));  
                if (k == INT_K - 1) {  
                    //4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。  
                    nodeA.setkDistance(tempNodeList.get(k).getDistance());  
                }  
            }  
            kdAndKnList.add(nodeA);  
        }  
  
        return kdAndKnList;  
    }  
      
    /** 
     * 计算给定点A与其他点B之间的欧几里得距离。 
     * 欧氏距离的公式： 
     * d=sqrt( ∑(xi1-xi2)^2 ) 这里i=1,2..n 
     * xi1表示第一个点的第i维坐标,xi2表示第二个点的第i维坐标 
     * n维欧氏空间是一个点集,它的每个点可以表示为(x(1),x(2),...x(n)), 
     * 其中x(i)(i=1,2...n)是实数,称为x的第i个坐标,两个点x和y=(y(1),y(2)...y(n))之间的距离d(x,y)定义为上面的公式. 
     * @param A 
     * @param B 
     * @return 
     */  
    private double getDis(DataNode A, DataNode B) {  
        double dis = 0.0;  
        double[] dimA = A.getDimensioin();  
        double[] dimB = B.getDimensioin();  
        if (dimA.length == dimB.length) {  
            for (int i = 0; i < dimA.length; i++) {  
                double temp = Math.pow(dimA[i] - dimB[i], 2);  
                dis = dis + temp;  
            }  
            dis = Math.pow(dis, 0.5);  
        }  
        return dis;  
    }  
  
    /** 
     * 升序排序  
     * 
     */  
    class DistComparator implements Comparator<DataNode> {  
        public int compare(DataNode A, DataNode B) {  
            //return A.getDistance() - B.getDistance() < 0 ? -1 : 1;  
            if((A.getDistance()-B.getDistance())<0)     
                return -1;    
            else if((A.getDistance()-B.getDistance())>0)    
                return 1;    
            else return 0;    
        }  
    }  
  
    /** 
     * 降序排序 
     * 
     */  
    class LofComparator implements Comparator<DataNode> {  
        public int compare(DataNode A, DataNode B) {  
            //return A.getLof() - B.getLof() < 0 ? 1 : -1;  
            if((A.getLof()-B.getLof())<0)     
                return 1;    
            else if((A.getLof()-B.getLof())>0)    
                return -1;    
            else return 0;    
        }  
    }  
 
     
    public static void main(String[] args) throws IOException {  
          
        java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.####");    
        
         
        
        // 读取目录下所有文件  bag
        File root = new File("C:/Users/uatrm990204/Desktop/LOF/javatest");
        File[] files = root.listFiles();
        
        // 读取一个文件
        for (int i = 0; i < files.length; i++){
        	// print the name of each file, for the purpose that we can recognize the sample size
//        	System.out.println(files[i]);
        	
        	BufferedReader br = new BufferedReader(new FileReader(files[i]));
        	String str = br.readLine();
        	// build a DataNode set
        	ArrayList<DataNode> dpoints = new ArrayList<DataNode>(); 
        	//walk through each line
        	int r = 1;
        	while ((str = br.readLine()) != null){
//        		System.out.println(str);
    		    String[] strs = str.split(",");
    		    double[] eachRow= new double[strs.length];
    		    for (int j =0; j<strs.length;j++){
    		    
    		    eachRow[j] =  Double.parseDouble(strs[j]);
    		    }
    		    
    		    dpoints.add(new DataNode(String.valueOf(r), eachRow));  
//    		    System.out.println(eachRow[j]);
    		    r++;
    		    
        	}
//        	System.out.println(dpoints.size());
        	
        
        	 Date datestart = new Date();
                         
        	 OutlierNodeDetect lof = new OutlierNodeDetect();
        	 // bagging for k
        	 int[] k = {5,6,7,9,12,15,20,25};
        	 for (int l = 0; l < k.length; l++){
        		 Date datestart1 = new Date();
        	     lof.setK(k[l]);
        	     List<DataNode> nodeList = lof.getOutlierNode(dpoints); 
        	     Date dateend1 = new Date();
        	     long gap1 = dateend1.getTime() - datestart1.getTime();
        	     System.out.println(gap1);
//        	     System.out.println("int_k" + k[l] +":" + gap1);
//             System.out.println("The K of this loop:" +k[l] +"\n");
             
//                 for (DataNode node : nodeList) {  
//            	     System.out.println(node.getNodeName() + "  " + df.format(node.getLof()));
//        	     }
        	 }
             //计时结束
             Date dateend = new Date();
             long gap = dateend.getTime() - datestart.getTime();
//             System.out.println(datestart.getTime());
//             System.out.println(dateend.getTime());
             System.out.println("total:"+gap);
            // print results
             for (DataNode node : nodeList) {  
                 System.out.println(node.getNodeName() + "  " + df.format(node.getLof()));  
             }  
        }
          
    }  
 
