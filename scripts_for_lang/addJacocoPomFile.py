import sys
import os
import glob
import re
import shutil



homeDir = '/home/h-yosiok/Lab/d4j/lang_'+sys.argv[1]+'_buggy/'
testDir = homeDir+'/src/test/'

if os.path.exists(homeDir+"original_pom.xml"):
    sys.exit(0)

os.makedirs(homeDir+'target/jacocoexec',exist_ok=True)
executionTestFQNs = list()

# pomファイルの有無を調査
if not os.path.exists(homeDir+"pom.xml"):
    print("There is no pom.xml")
    sys.exit(1)

if not os.path.exists(homeDir+"original_pom.xml"):
    shutil.copy(homeDir+"pom.xml",homeDir+"original_pom.xml")
    
f = open(homeDir+"original_pom.xml")
neopom = open(homeDir+"pom.xml","w")
pomfile = f.readlines()
flag = False
reportingFlag = False
for index in range(len(pomfile)):
    if "<build>" in pomfile[index] and "<plugins>" in pomfile[index+1]:
        flag = True
    
    if "<reporting>" in pomfile[index] and "<plugins>" in pomfile[index+1]:
        reportingFlag = True
        
    neopom.writelines(pomfile[index])
    if flag and "<plugins>" in pomfile[index]:
        neopom.writelines("<plugin>\n")
        neopom.writelines("<groupId>org.jacoco</groupId>\n")
        neopom.writelines("<artifactId>jacoco-maven-plugin</artifactId>\n")
        neopom.writelines("<version>0.8.5</version>\n")
        neopom.writelines("<executions>\n")
        neopom.writelines("  <execution>\n")
        neopom.writelines("    <goals>\n")
        neopom.writelines("      <goal>prepare-agent</goal>\n")
        neopom.writelines("    </goals>\n")
        neopom.writelines("  </execution>\n")
        neopom.writelines("  <execution>\n")
        neopom.writelines("    <id>report</id>\n")
        neopom.writelines("    <phase>test</phase>\n")
        neopom.writelines("    <goals>\n")
        neopom.writelines("      <goal>report</goal>\n")
        neopom.writelines("    </goals>\n")
        neopom.writelines("  </execution>\n")
        neopom.writelines("</executions>\n")
        neopom.writelines("</plugin>\n")
        flag = False
    
    if reportingFlag and "<plugins>" in pomfile[index]:
        neopom.writelines("          <plugin>\n")
        neopom.writelines("<groupId>org.jacoco</groupId>\n")
        neopom.writelines("<artifactId>jacoco-maven-plugin</artifactId>\n")
        neopom.writelines("<reportSets>\n")
        neopom.writelines("  <reportSet>\n")
        neopom.writelines("    <reports>\n")
        neopom.writelines("      <report>report</report>\n")
        neopom.writelines("    </reports>\n")
        neopom.writelines("  </reportSet>\n")
        neopom.writelines("</reportSets>\n")
        neopom.writelines("</plugin>\n")
        reportingFlag = False
                          
neopom.close()
f.close()
