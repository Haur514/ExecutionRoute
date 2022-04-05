import sys
import os
import glob
import re
import shutil

# - <maven.compile.source>1.5</maven.compile.source>
# - <maven.compile.target>1.5</maven.compile.target>
# + <maven.compile.source>1.6</maven.compile.source>
# + <maven.compile.target>1.6</maven.compile.target>

# 一部のプロジェクトにおいて，pom.xml内のmaven.compileのバージョンが適正でありません．
# このプログラムを用いることでそのような問題を解決します.


def checkPomExistence():
    if not os.path.exists(homeDir+"pom.xml"):
        print("There is no pom.xml")
        sys.exit(1)


homeDir = '/home/h-yosiok/Lab/d4j/lang_'+sys.argv[1]+'_buggy/'
testDir = homeDir+'/src/test/'

os.makedirs(homeDir+'target/jacocoexec',exist_ok=True)
executionTestFQNs = list()

checkPomExistence()
    
f = open(homeDir+"pom.xml")
neopom = open(homeDir+"pom_modifyCompileVersion.xml","w")
pomfile = f.readlines()
flag = False
reportingFlag = False
for index in range(len(pomfile)):
    if "<maven.compile.source>1.5</maven.compile.source>" in pomfile[index]:
        neopom.writelines("<maven.compile.source>1.6</maven.compile.source>\n")
    elif "<maven.compile.target>1.5</maven.compile.target>" in pomfile[index]:
        neopom.writelines("<maven.compile.target>1.6</maven.compile.target>")
    else:
        neopom.writelines(pomfile[index])
neopom.close()
f.close()


#生成したファイルを元のファイルと置き換える
shutil.copy(homeDir+"pom_modifyCompileVersion.xml",homeDir+"pom.xml")
