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
        if ')' in test_name[len(test_name)-1-i]:
            i += 1
        elif '(' in test_name[len(test_name)-1-i]:
            i -= 1
            if i == 0:
                return test_name[0:len(test_name)-2-i]
    return None
    
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


homeDir = '/home/h-yosiok/Lab/d4j/lang_'+sys.argv[1]+'_buggy/'
testDir = homeDir+'/src/test/'
os.makedirs(homeDir+'target/jacocoexec',exist_ok=True)


#pomファイルにjacocoの依存関係を入れる
os.system("python3 addJacocoPomFile.py "+sys.argv[1])

#surefire-pluginのバージョンが無いため，追加する
os.system("python3 addSurefireVersion.py "+sys.argv[1])

#maven.compileのバージョンが不適切なため，修正する
os.system("python3 modifyCompileVersion.py "+sys.argv[1])



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
            # if os.path.exists(homeDir+"target/jacoco.exec"):
            #     os.remove(homeDir+"target/jacoco.exec")
            # os.system("mvn jacoco:prepare-agent test -Dtest="+test)
            # if os.path.exists(homeDir+"target/jacoco.exec"):
            #     shutil.copy(homeDir+"target/jacoco.exec",homeDir+"target/jacocoexec/"+test+".exec")



f.close()
testNameFile.close()


# os.system('gradle run --args /home/h-yosiok/Lab/d4j/lang_'+sys.argv[1]+'_buggy')