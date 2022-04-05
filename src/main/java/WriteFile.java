import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteFile {
    private final String fileName;

    WriteFile(String fileName) {
        this.fileName = fileName;
    }

    WriteFile(String fileName, DataStore dataStore) {
        this.fileName = fileName;
        try {
            writeExecutionRouteToFile(dataStore);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean allTestNotExecuteThatLine(DataStore dataStore, String sourceFile, int lineNum) {
        for (String testName : dataStore.getDataStore().keySet()) {
            if(testName.equals("ALLEXECUTABLEROUTE")){
                continue;
            }

            if(dataStore.getDataStore().get(testName).getExecutionData().get(sourceFile).contains(lineNum)){
                return false;
            }
        }
        return true;
    }

    private boolean allTestNotExecuteThatFile(DataStore dataStore, String sourceFile) {
        for (String testName : dataStore.getDataStore().keySet()) {
            if(testName.equals("ALLEXECUTABLEROUTE")){
                continue;
            }
            if (dataStore.getDataStore().get(testName).getExecutionData().keySet().contains(sourceFile)) {
                if(dataStore.getDataStore().get(testName).getExecutionData().get(sourceFile).size()!=0){
                    return false;
                }
            }
        }
        return true;
    }

    public void writeExecutionRouteToFile(DataStore dataStore) throws IOException {
        File file = new File(fileName);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        MemorizeExecutionData allExecutableRoute = dataStore.getDataStore().get("ALLEXECUTABLEROUTE");

        pw.println(dataStore.getDataStore().keySet());

        for (String sourceFile : allExecutableRoute.getExecutionData().keySet()) {
            if (allTestNotExecuteThatFile(dataStore, sourceFile)) {
                continue;
            }
            pw.println(sourceFile);
            for (int lineNum : allExecutableRoute.getExecutionData().get(sourceFile)) {
                if (allTestNotExecuteThatLine(dataStore, sourceFile, lineNum)) {
                    continue;
                }
                pw.print(String.format("%05d",lineNum));
                for (String testName : dataStore.getDataStore().keySet()) {
                    if (testName.equals("ALLEXECUTABLEROUTE")) {
                        continue;
                    }
                    pw.print(",");
                    if (dataStore.getDataStore().get(testName).getExecutionData().get(sourceFile).contains(lineNum)) {
                        pw.print("o");
                    } else {
                        pw.print("-");
                    }
                }
                pw.println();
            }
        }
        pw.close();
    }
}
