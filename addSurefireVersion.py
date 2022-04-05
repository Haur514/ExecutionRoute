import sys
import os
import glob
import re
import shutil

# 一部のプロジェクトにおいて，pom.xml内のsurefire-pluginのバージョンが適正でありません．
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
neopom = open(homeDir+"pom_addVersion.xml","w")
pomfile = f.readlines()
flag = False
reportingFlag = False
for index in range(len(pomfile)):
    if "<artifactId>maven-surefire-plugin</artifactId>" in pomfile[index] and not "<version>" in pomfile[index+1]:
        neopom.writelines(pomfile[index])
        neopom.writelines("<version>2.22.2</version>\n")
    else:
        neopom.writelines(pomfile[index])
neopom.close()
f.close()


#生成したファイルを元のファイルと置き換える
shutil.copy(homeDir+"pom_addVersion.xml",homeDir+"pom.xml")
