/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.osgi.internal.loader.buddy.IBuddyPolicy;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.junit.runner.Description;




/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 *
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class Main {

	private final String title;

	private File executionDataFile;
	private File classesDirectory;
	private File sourceDirectory;
	private File reportDirectory;

	private final IRuntime jacocoRuntime;
	private final Instrumenter jacocoInstrumenter;
	private final RuntimeData jacocoRuntimeData;

	private ExecFileLoader execFileLoader;

	private static DataStore dataStore = new DataStore();

	public static String fileName;
	public static String homeDir;
	private static String testName;
	private static List<String> testNameList;

	/**
	 * Starts the report generation process
	 *
	 * @param args
	 *            Arguments to the application. This will be the location of the
	 *            eclipse projects that will be used to generate reports for
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		homeDir = args[0];

		final Main initial = new Main(new File(homeDir),readTestNameFile().get(0));
		initial.setAllExecutableRoute();
		testNameList = readTestNameFile();
		for(int i = 0; i < testNameList.size();i++){
			testName = testNameList.get(i);
			if(!checkFileExistence(testName)){
				System.out.println("NoFILEFound:"+testName);
				continue;
			}
			final Main generator = new Main(new File(homeDir),testName);
			generator.create();
		}
		String tmp[] = homeDir.split("/");
		new WriteFile("./"+tmp[tmp.length-1],dataStore);
	}

	//実行可能な経路をALLEXECUTABLEROUTEのkeyでdataStoreに格納
	private void setAllExecutableRoute() throws IOException{
		loadExecutionData();
		final IBundleCoverage bundleCoverage = forInitialAnalyzeStructure();
		createReport(bundleCoverage);
	}


	public static List<String> readTestNameFile(){
		Path file = Paths.get(homeDir+"/testNames");

		try {
			return Files.readAllLines(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static boolean checkFileExistence(String fileName){
		File file = new File(homeDir+"/target/jacocoexec/"+fileName+".exec");
		return file.exists();
	}

	/**
	 * Create a new generator based for the given project.
	 *
	 * @param projectDirectory
	 */
	public Main(final File projectDirectory,final String test) {
		this.jacocoRuntime = new LoggerRuntime();
		this.jacocoInstrumenter = new Instrumenter(jacocoRuntime);
		this.jacocoRuntimeData = new RuntimeData();
		this.title = projectDirectory.getName();
		setFileData(projectDirectory, test);
	}

	public void setFileData(final File projectDirectory,final String test){
		this.executionDataFile = new File(projectDirectory, "target/jacocoexec/"+test+".exec");
		this.classesDirectory = new File(projectDirectory, "target/classes");
		this.sourceDirectory = new File(projectDirectory, "src");
		this.reportDirectory = new File(projectDirectory, "target/report");
	}

	/**
	 * Create the report.
	 *
	 * @throws IOException
	 */
	public void create() throws IOException {

		// Read the jacoco.exec file. Multiple data files could be merged
		// at this point
		loadExecutionData();

		// Run the structure analyzer on a single class folder to build up
		// the coverage model. The process would be similar if your classes
		// were in a jar file. Typically you would create a bundle for each
		// class folder and each jar you want in your report. If you have
		// more than one bundle you will need to add a grouping node to your
		// report
		final IBundleCoverage bundleCoverage = analyzeStructure();

		createReport(bundleCoverage);

	}

	private void createReport(final IBundleCoverage bundleCoverage)
			throws IOException {

		// Create a concrete report visitor based on some supplied
		// configuration. In this case we use the defaults
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		final IReportVisitor visitor = htmlFormatter
				.createVisitor(new FileMultiReportOutput(reportDirectory));

		// Initialize the report with all of the execution and session
		// information. At this point the report doesn't know about the
		// structure of the report being created
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(bundleCoverage,
				new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();

	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);
	}

	private IBundleCoverage forInitialAnalyzeStructure() throws IOException{
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);
		// coverageBuilder.getExecutedTargetFQN();
		analyzer.analyzeAll(classesDirectory);

		MemorizeExecutionData executionDataMemory = new MemorizeExecutionData();

		for(IClassCoverage classCoverage:coverageBuilder.getClasses()){
			TreeSet<Integer> tmpList = new TreeSet<>();
			String tmpFileName = new String();

			for(int i = classCoverage.getFirstLine(); i <= classCoverage.getLastLine();i++){
				final int s = classCoverage.getLine(i).getStatus();
				
				if(s == ICounter.EMPTY){
					if(tmpList.contains(i)){
						tmpList.remove(i);
					}
				}else if(s == ICounter.FULLY_COVERED || s == ICounter.PARTLY_COVERED){
					tmpList.add(i);
				}else if(s == ICounter.NOT_COVERED){
					tmpList.add(i);
				}else{
					if(tmpList.contains(i)){
						tmpList.remove(i);
					}
				}
			}

			executionDataMemory.addExecutionData(classCoverage.getName().replace("/","."),tmpList);
		}

		dataStore.addExecutionData("ALLEXECUTABLEROUTE",executionDataMemory);
		
		for (final ExecutionData data : execFileLoader.getExecutionDataStore().getContents()) {
		if (!data.hasHits()) {
			continue;
		}

		final String strFqn = data.getName()
			.replace("/", ".");
	}
		return coverageBuilder.getBundle(title);
	}

	private IBundleCoverage analyzeStructure() throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);
		// coverageBuilder.getExecutedTargetFQN();
		analyzer.analyzeAll(classesDirectory);

		MemorizeExecutionData executionDataMemory = new MemorizeExecutionData();

		for(IClassCoverage classCoverage:coverageBuilder.getClasses()){
			TreeSet<Integer> tmpList = new TreeSet<>();
			String tmpFileName = new String();

			for(int i = classCoverage.getFirstLine(); i <= classCoverage.getLastLine();i++){
				final int s = classCoverage.getLine(i).getStatus();
				
				if(s == ICounter.EMPTY){
					if(tmpList.contains(i)){
						tmpList.remove(i);
					}
				}else if(s == ICounter.FULLY_COVERED || s == ICounter.PARTLY_COVERED){
					tmpList.add(i);
				}else if(s == ICounter.NOT_COVERED){
					if(tmpList.contains(i)){
						tmpList.remove(i);
					}
				}else{
					if(tmpList.contains(i)){
						tmpList.remove(i);
					}
				}
			}

			executionDataMemory.addExecutionData(classCoverage.getName().replace("/","."),tmpList);
		}

		dataStore.addExecutionData(testName,executionDataMemory);
		
		// 一度でもカバレッジ計測されたクラスのみに対してカバレッジ情報を探索
		for (final ExecutionData data : execFileLoader.getExecutionDataStore().getContents()) {
		//System.out.println("analyzeJacocoRuntimeData().roop()");
 		// System.out.println(data.toString());
		// 当該テスト実行でprobeが反応しない＝実行されていない場合はskip
		if (!data.hasHits()) {
			continue;
		}

		final String strFqn = data.getName()
			.replace("/", ".");
		// System.out.println(strFqn);
		// analyzer.analyzeClass(bytecode, "");
	}

		return coverageBuilder.getBundle(title);
	}


	/**
     * jacocoにより計測した行ごとのCoverageを回収する．
     *
     * @param coverageBuilder 計測したCoverageを格納する保存先
     * @throws IOException
     */
    private void analyzeJacocoRuntimeData(final CoverageBuilder coverageBuilder)
        throws IOException {
      final ExecutionDataStore executionData = new ExecutionDataStore();
      final SessionInfoStore sessionInfo = new SessionInfoStore();
      jacocoRuntimeData.collect(executionData, sessionInfo, false);
      // jacocoRuntime.shutdown(); // Don't shutdown (This statement is a cause for bug #290)

      final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

	  System.out.println("analyzeJacocoRuntimeData()");

      // 一度でもカバレッジ計測されたクラスのみに対してカバレッジ情報を探索
      for (final ExecutionData data : executionData.getContents()) {
		//   System.out.println(data.toString)

        // 当該テスト実行でprobeが反応しない＝実行されていない場合はskip
        if (!data.hasHits()) {
          continue;
        }

        final String strFqn = data.getName()
            .replace("/", ".");
        // System.out.println(strFqn);
        // analyzer.analyzeClass(bytecode, "");
      }
    }
	    /**
     * 回収したCoverageを型変換しTestResultsに格納する．
     *
     * @param coverageBuilder Coverageが格納されたビルダー
     * @param description テストの実行情報
     */
    private void addJacocoCoverageToTestResults(final CoverageBuilder coverageBuilder,
        final Description description) {
    //final FullyQualifiedName testMethodFQN = getTestMethodName(description);

    //   final Map<FullyQualifiedName, Coverage> coverages = coverageBuilder.getClasses()
    //       .stream()
    //       .map(RawCoverage::new)
    //       .collect(Collectors.toMap(Coverage::getExecutedTargetFQN, Functions.identity()));
    }
}