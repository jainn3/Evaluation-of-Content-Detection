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
mimetype_parserChain = {}

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
    	# print(str(original_file_size)+" "+str(content_size)+" "+str(metadata_size))
    	for parser in parserChain:
    		currentFileParserChain=currentFileParserChain+parser+"-"
    	if mimetype in mimetype_parserChain :
    		if currentFileParserChain not in mimetype_parserChain[mimetype] :
    			stats = {}
    			stats["count"]=1
    			stats["text_ratio"]=(content_size/original_file_size)
    			stats["metadata_ratio"]=(metadata_size/original_file_size)
    			mimetype_parserChain[mimetype][currentFileParserChain]=stats
    		else:
    			# get old counts
    			old_count=mimetype_parserChain[mimetype][currentFileParserChain]["count"]
    			new_text_ratio = mimetype_parserChain[mimetype][currentFileParserChain]["text_ratio"]
    			new_metadata_ratio = mimetype_parserChain[mimetype][currentFileParserChain]["metadata_ratio"]
    			# calculate new stats
    			new_text_ratio = ((old_count*new_text_ratio) + (content_size/original_file_size))/(old_count + 1)
    			new_metadata_ratio = ((old_count*new_metadata_ratio) + (metadata_size/original_file_size)) / (old_count+1)
    			# update with new stats
    			mimetype_parserChain[mimetype][currentFileParserChain]["count"]=old_count+1
    			mimetype_parserChain[mimetype][currentFileParserChain]["text_ratio"]=new_text_ratio
    			mimetype_parserChain[mimetype][currentFileParserChain]["metadata_ratio"]=new_metadata_ratio
    	else:
    		print(str(original_file_size)+" "+str(content_size)+" "+str(metadata_size))
    		new_stats = {}
    		new_stats["count"]=1
    		new_stats["text_ratio"]=(content_size/original_file_size)
    		new_stats["metadata_ratio"]=(metadata_size/original_file_size)
    		file_parser_chain={}
    		file_parser_chain[currentFileParserChain]=new_stats
    		mimetype_parserChain[mimetype]=file_parser_chain
    	# print(mimetype_parserChain)
print(mimetype_parserChain)
json_string = json.dumps(mimetype_parserChain)
output_file = open("AllParserChain.json", "w+")
output_file.write(json_string)
