import sys
import os
import glob
import re
import shutil



def getTestName(line):
    words = line.split()
    if len(words) < 3:
        return None
    if(words[0]=='public' and words[1]=='void'):
        return removeBrackets(words[2])
    else:
        return None
    
def removeBrackets(test_name):
    i = 0
    for i in range(len(test_name)):
        if test_name[i] == '(':
            return test_name[0:i]
    
def setTestList(currentdir,testCode):
    for index in range(len(testCode)):
        # if '@Test' in testCode[index]:
            if not (getTestName(testCode[index]) is None):
                executionTestFQNs.add(currentdir+"#"+getTestName(testCode[index]))

def failedTestClass(homeDir):
    d4jpropfile = open(homeDir+"defects4j.build.properties")
    failedTestLine = d4jpropfile.readlines()
    for index in range(len(failedTestLine)):
        if "d4j.tests.trigger" in failedTestLine[index]:
            splitFailedTestLine = re.split('=|::|,',failedTestLine[index])
            ret = set()
            for i in range(len(splitFailedTestLine)):
                if(i%2==1 and i >= 1):
                    ret.add(splitFailedTestLine[i])
            return ret


currentDir = os.getcwd()

homeDir = '/home/h-yosiok/Lab/d4j/time_'+sys.argv[1]+'_buggy/'
testDir = homeDir+'/src/test/'
os.makedirs(homeDir+'target/jacocoexec',exist_ok=True)


###############################################################################
#LANG向けの処理
###############################################################################
#pomファイルにjacocoの依存関係を入れる
os.system("python3 addJacocoPomFile.py "+sys.argv[1])

#surefire-pluginのバージョンが無いため，追加する
os.system("python3 addSurefireVersion.py "+sys.argv[1])

#maven.compileのバージョンが不適切なため，修正する
os.system("python3 modifyCompileVersion.py "+sys.argv[1])

#"[ERROR] /home/h-yosiok/Lab/d4j/time_24_buggy/src/test/java/org/apache/commons/time3/reflect/TypeUtilsTest.java:[507,40] エラー: 不適合な型: 推論型が上限に適合しません"のバグを，TypeUtilsTestの中身を削除することで取り除く.
os.system("python3 removeTypeUtilsTest.py "+sys.argv[1])

###############################################################################
#TIME向けの処理
###############################################################################
os.system("python3 modify_junit_version.py "+sys.argv[1])

# JUnitのバージョンが3.0.0-M1で無いものはこれに書き換える
os.system("python3 modify_surefire_version.py "+sys.argv[1])

executionTestFQNs = set()

failedTestClass(homeDir)

files = glob.glob(testDir+"/**/*.java",recursive=True)
currentdir = ""
for file in files:
    f = open(file)
    tmp=file.replace("/",".").split(".")
    currentdir=tmp[len(tmp)-2]
    datalist = f.readlines()
    setTestList(currentdir,datalist)

os.chdir(homeDir)

testNameFile = open('testNames','w')
print(failedTestClass(homeDir))

for test in executionTestFQNs:
    for failed in failedTestClass(homeDir):
        className = failed.split('.')
        if className[len(className)-1] in test:
            testNameFile.write(test+"\n")
            print(test+"\n")
            if os.path.exists(homeDir+"target/jacoco.exec"):
                os.remove(homeDir+"target/jacoco.exec")
            print("mvn jacoco:report test -Dtest="+test)
            os.system("mvn jacoco:report test -Dtest="+test)
            if os.path.exists(homeDir+"target/jacoco.exec"):
                shutil.copy(homeDir+"target/jacoco.exec",homeDir+"target/jacocoexec/"+test+".exec")



f.close()
testNameFile.close()

os.chdir(currentDir)
os.system('gradle run --args /home/h-yosiok/Lab/d4j/time_'+sys.argv[1]+'_buggy')