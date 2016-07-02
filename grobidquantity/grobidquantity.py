import json
from tika.tika import callServer, checkTikaServer
import os
import tika
from tika.parser import _parse

def main():

    count = 0
    data_files = '/home/nimesh/Desktop/1000'

    output_file = open('grobidquantity_data_test.json', 'w+')
    for root, dirs, files in os.walk(data_files):
        for file in files:

             try:
                 path=''
                 if(file!='.DS_Store'):
                    count+=1
                    print count
                    path=os.path.join(root, file)
                    tika.initVM()
                    parsed = tika.parser.from_file(path)

                    if("content" in parsed.keys()):
           
                        type=parsed.get("metadata").get("Content-Type")
                        print type
                        content=parsed["content"]
                        mycontent = content.encode("UTF-8")
                        
                        if(content is not None and ('application/pdf' in type or 'application/xml' in type or 'text/plain' in type)):
                            p=os.popen('curl -GET --data-urlencode'+' '+'"text='+mycontent+'"'+' '+'localhost:8080/processQuantityText').read()
                            json.dump(p, output_file)
                        output_file.write('\n')
             except:
                continue           
    output_file.close()


if __name__ == '__main__':
    main()