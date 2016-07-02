import json
import codecs
import pprint

def parseJSON(filename,ID):
	data = []
	ignore = ['X-Parsed-By','id','Content-Type']

	with open(filename,'r+') as json_fp:
		for line in json_fp:
			data.append(json.loads(line))

	instances = set()
	count_hash = {}

	for line in data:

		for key in line:
			if key in ignore:
				continue
			if isinstance(line[key],list):
				for item in line[key]:
					if item in count_hash:
						count_hash[item] += 1
					else:
						count_hash[item] = 1
					instances.add(item)
			else:
				item = line[key]
				if item in count_hash:
					count_hash[item] += 1
				else:
					count_hash[item] = 1
				instances.add(item)


	return instances, count_hash


def writeSets(data,ID):
	fp = codecs.open(ID+'.txt','w+','utf8')
	for item in data:
			fp.write(item + '\n')

def readTemp(filename):
	data = set()
	with open(filename,'r') as fp:
		for item in fp:
			data.add(item)

	return data

def createCompareNER(nltk_count,onlp_count,cnlp_count):
	Hash = {'labels':[],'series':[]}
	for key in nltk_count:
		Hash['labels'].append(key)

	nltk_val = {'name':'nltk','value':[]}
	onlp_val = {'name':'opennlp','value':[]}
	cnlp_val = {'name':'corenlp','value':[]}

	for label in Hash['labels']:
		nltk_val['value'].append(nltk_count[label])
		try:
			onlp_val['value'].append(onlp_count[label])
		except:
			onlp_val['value'].append(0)
		try:
			cnlp_val['value'].append(cnlp_count[label])
		except:
			cnlp_val['value'].append(0)

	Hash['series'].extend([nltk_val,onlp_val,cnlp_val])
	print Hash


def getunion():
	nltk_data, nltk_count = parseJSON('nltk.json','nltk')
	onlp_data, onlp_count = parseJSON('opennlpl.json','onlp')
	cnlp_data, cnlp_count = parseJSON('corenlp.json','cnlp')

	createCompareNER(nltk_count,onlp_count,cnlp_count)
	uniques = set()

	uniques = set.intersection(nltk_data,onlp_data,cnlp_data)
	writeSets(uniques,'intersected_data')

getunion()
