from asyncio import format_helpers
import sys
import os
import glob
import re
import shutil

# /home/h-yosiok/Lab/d4j/lang_24_buggy/src/test/java/org/apache/commons/lang3/reflect/TypeUtilsTest.java:[507,40] エラー: 不適合な型: 推論型が上限に適合しません
# lang_3-30で出現するこのバグを取り除く
# TypeUtilsTest.javaにおける，該当テストの中身を削除することで対応


def checkPomExistence():
    if not os.path.exists(homeDir+"pom.xml"):
        print("There is no pom.xml")
        sys.exit(1)


homeDir = '/home/h-yosiok/Lab/d4j/time_'+sys.argv[1]+'_buggy/'
test_file_dir = homeDir+"src/test/java/org/apache/commons/lang3/reflect/"
test_file_name = "TypeUtilsTest.java"


if not os.path.exists(test_file_dir+test_file_name):
    sys.exit(1)

testDir = homeDir+'/src/test/'

os.makedirs(homeDir+'target/jacocoexec',exist_ok=True)
executionTestFQNs = list()

checkPomExistence()
    
f = open(test_file_dir+test_file_name)
neopom = open(homeDir+"modified"+test_file_name,"w")
pomfile = f.readlines()
comment_out_flag = False

# @Testが付いているかを判定
annotation_exist_flag = False

for index in range(len(pomfile)):
    if "public void testTypesSatisfyVariables()" in pomfile[index]:
        comment_out_flag = True
        if "@Test" in pomfile[index-1]:
            annotation_exist_flag = True
    if comment_out_flag:
        neopom.writelines("//")
    neopom.writelines(pomfile[index])
    if comment_out_flag and "}" in pomfile[index] and not annotation_exist_flag:
        comment_out_flag = False
    if comment_out_flag and "@Test" in pomfile[index] and annotation_exist_flag:
        comment_out_flag = False
neopom.close()
f.close()


#生成したファイルを元のファイルと置き換える
shutil.copy(homeDir+"modified"+test_file_name,test_file_dir+test_file_name)
