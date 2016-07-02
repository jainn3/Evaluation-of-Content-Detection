from nltk import word_tokenize
from nltk.corpus import stopwords

import string
import argparse
import operator
import os
import json
import io

parser = argparse.ArgumentParser()
parser.add_argument("input_directory")
args = parser.parse_args()
mimetype_sizeInfo = {}

for root, dirs, files in os.walk(args.input_directory):
    for file_name in files:
    	print(file_name)
    	file_content = io.open(os.path.join(root, file_name), encoding="UTF-8").read()
    	json_content = json.loads(file_content)
    	parserChain = json_content[0]["X-Parsed-By"]
    	mimetype = json_content[0]["Content-Type"]
    	mimetype = mimetype.split(";")[0]
    	currentFileParserChain = ""
    	original_file_size = float(json_content[0]["original_file_size"])
    	content_size = float(json_content[0]["content_size"])
    	metadata_size = float(json_content[0]["metadata_size"])
        if mimetype in mimetype_sizeInfo:
            mimetype_sizeInfo[mimetype]["file_sizes"].append(original_file_size)
            old_count=mimetype_sizeInfo[mimetype]["count"]
            old_ratio=mimetype_sizeInfo[mimetype]["metadata_ratio"]
            new_ratio=((old_count*old_ratio) + (metadata_size/original_file_size))/(old_count+1)
            # update values
            mimetype_sizeInfo[mimetype]["count"]=old_count+1
            mimetype_sizeInfo[mimetype]["metadata_ratio"]=new_ratio
        else:
            arr={}
            arr["file_sizes"]=[]
            arr["file_sizes"].append(original_file_size)
            arr["metadata_ratio"]=(metadata_size/original_file_size)
            arr["count"]=1
            mimetype_sizeInfo[mimetype]=arr
            # print(mimetype_sizeInfo)

print(mimetype_sizeInfo)
json_string = json.dumps(mimetype_sizeInfo)
output_file = open("ALlFileSize.json", "w+")
output_file.write(json_string)
