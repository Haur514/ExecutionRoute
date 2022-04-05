from asyncio import format_helpers
import sys
import os
import glob
import re
import shutil

# original_pom.xmlをpom.xmlの名前に改名します.
# その後，original_pom.xmlを削除します.

def checkPomExistence():
    if not os.path.exists(homeDir+"pom.xml"):
        print("There is no pom.xml")
        sys.exit(1)


homeDir = '/home/h-yosiok/Lab/d4j/lang_'+sys.argv[1]+'_buggy/'

if not os.path.exists(homeDir+"original_pom.xml"):
    sys.exit(1)

shutil.copy(homeDir+"original_pom.xml",homeDir+"pom.xml")
os.remove(homeDir+"original_pom.xml")