import java.util.*;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;

public class MemorizeExecutionData {
    private Map<String,Set<Integer>> executionData;

    
    MemorizeExecutionData(){
        executionData = new LinkedHashMap<>();
    }

    public void addExecutionData(String className,TreeSet<Integer> route){
        executionData.put(className,route);
    }

    public void showAllExecutionData(){
        for(String key : executionData.keySet()){
            if(executionData.get(key).size()==0){
                continue;
            }
            System.out.print(key);
            System.out.print(" ");
            System.out.println(executionData.get(key));
        }
    }

    /**
     * @return Map<String,Set<Integer>> return the executionData
     */
    public Map<String,Set<Integer>> getExecutionData() {
        return executionData;
    }

    /**
     * @param executionData the executionData to set
     */
    public void setExecutionData(Map<String,Set<Integer>> executionData) {
        this.executionData = executionData;
    }

}
