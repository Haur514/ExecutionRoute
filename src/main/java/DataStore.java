import java.util.*;

public class DataStore {
    private Map<String,MemorizeExecutionData> dataStore = new LinkedHashMap<>();
    private Set<String> failedTestList = new HashSet();
    
    DataStore(){
    }

    public Set<String> getFailedTestList() {
        return failedTestList;
    }

    public void addFailedTestList(String failedTest) {
        this.failedTestList.add(failedTest);
    }

    public void setFailedTestList(String failedTest){

    }

    public void addExecutionData(String testName,MemorizeExecutionData memorizeExecutionData){
        dataStore.put(testName,memorizeExecutionData);
    }

    public void showAll(){
        //MemorizeExecutionData allExecutableRoute = dataStore.get("ALLEXECUTABLEROUTE");
        for(String key : dataStore.keySet()){
            if(key.equals("ALLEXECUTABLEROUTE")){
                continue;
            }
            System.out.println(key);
            dataStore.get(key).showAllExecutionData();
        }
    }


    /**
     * @return Map<String,MemorizeExecutionData> return the dataStore
     */
    public Map<String,MemorizeExecutionData> getDataStore() {
        return dataStore;
    }

    /**
     * @param dataStore the dataStore to set
     */
    public void setDataStore(Map<String,MemorizeExecutionData> dataStore) {
        this.dataStore = dataStore;
    }

}
