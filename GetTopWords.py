from nltk import word_tokenize
from nltk.corpus import stopwords

import string
import argparse
import operator
import os
import json
import io

def getWordsAndCountForFile(file_name, stop):
	file_word_map = {}
	# for text in file_name:
	remainder = [i for i in word_tokenize(file_name.lower()) if i not in stop]
	for word in remainder:
		if word in file_word_map :
			file_word_map[word] = long(file_word_map[word]) + 1
		else:
			file_word_map[word] = 1
	# print(file_word_map)
	return file_word_map


parser = argparse.ArgumentParser()
parser.add_argument("input_directory")
args = parser.parse_args()
global_word_map = {}

stop = stopwords.words('english')
for i in string.punctuation:
	stop.append(i)

for root, dirs, files in os.walk(args.input_directory):
    for file_name in files:
    	print(os.path.join(root, file_name))
    	file_content = io.open(os.path.join(root, file_name), encoding="UTF-8").read()
    	# print(file_content)
    	file_word_map = getWordsAndCountForFile(file_content, stop)
    	for word in file_word_map:
    		if word in global_word_map : 
    			global_word_map[word] = long(global_word_map[word]) + long(file_word_map[word])
    		else:
    			global_word_map[word] = file_word_map[word]
print(global_word_map)
json_string = json.dumps(global_word_map)
output_file = open("WordCount.json", "w+")
output_file.write(json_string)
