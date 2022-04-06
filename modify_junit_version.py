import sys
import os
import glob
import re
import shutil

# - 

# 一部のプロジェクトにおいて，JUnitのバージョンが4未満であるため，メソッド単位でのテストケースの実行ができません．そのため，本スクリプトを用いることでJUnitのバージョンを4.7に書き換えます.


def checkPomExistence():
    if not os.path.exists(homeDir+"pom.xml"):
        print("There is no pom.xml")
        sys.exit(1)


homeDir = '/home/h-yosiok/Lab/d4j/time_'+sys.argv[1]+'_buggy/'
testDir = homeDir+'/src/test/'

checkPomExistence()
    
f = open(homeDir+"pom.xml")
neopom = open(homeDir+"pom_modifyCompileVersion.xml","w")
pomfile = f.readlines()
junit_flag = False
for index in range(len(pomfile)):
    if "junit" in pomfile[index]:
        junit_flag = True
    if "<version>3.8.2</version>" in pomfile[index]:
        neopom.writelines("<version>4.7</version>\n")
    else:
        neopom.writelines(pomfile[index])
neopom.close()
f.close()


#生成したファイルを元のファイルと置き換える
shutil.copy(homeDir+"pom_modifyCompileVersion.xml",homeDir+"pom.xml")
