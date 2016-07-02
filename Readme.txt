createFileSizeJSON.py 
This file takes one argument as input. The argument must be Absolute Path to the directory which contains all the index json files.
It creates a JSON which is used in creating the File Size Distribution VIsualization. You can find the json inside Graph/FileSizeDiversity.


CreateParserChainJSON.py 
This file takes one argument as input. The argument must be Absolute Path to the directory which contains all the index json files.
It creates a JSON which is used in creating the File Size Distribution VIsualization. You can find the json inside Graph/ParserChain.


GetTopSWEETConcepts.py and GetTopWords.py
These files takes one argument as input. The argument must be Absolute Path to the directory which contains all the index json files.
It creates a JSON which is used in creating the File Size Distribution Visualization. You can find the json inside Graph/WordCloud.


ExtractMetadata.java
This file take one argument as input. The argument must be absolute path to the directory which contains all the actual data files. It produces two output for each input file. 1. Index file which stores metadata. 2. Content File which stores the extracted text output. 


CCAdataParsing.java
This files takes the CCA data directory as input and recursively parses the CCA data, fetches url content and body content and creates a json output which indicates whether the crawler finds most relevant pages. This file is also used for NER parsing.


Grobidquantity.py
This file is used to extract quantities measurement from the data. The input is the current directory which contains file and the output is json file which contains all measurements. The grobid quantity service should be running.


CompositeNERAgreementParser.py
This file produces the intersection of all the entities parsed by opennlp, corenlp and nltk. The input to this is the json files from all the 3 parsers and it outputs the intersection of entity present in all these 3 NER parsers.


D3 Visualizations :
You can find all the D3 visualizations in Graphs Folder. Each visualization has a self explanatory folder name under Graphs folder. To view, just right-click open all html file with Mozilla Firefox. (Note : Please have internet connection as many html files are importing d3.js from a CDN)


Measurements_part10.py
This file produces the range of measurements in the directory. The input of this is the entire files set in the directory and it outputs the range of measurements. Before running the program, the ‘setupfile.py’ should be executed.