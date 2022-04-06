** mavenで単体テスト結果をjacoco.execで取得する方法
mvn jacoco:prepare-agent test -Dtest=MannWhitneyUTestTest#testBigDataSet

** 起動方法
gradle run --args "/home/h-yosiok/Lab/d4j/lang_1_buggy"

## いちいちmavenの単体テストを実行するため，極めて非効率的であるが，動きます^^
