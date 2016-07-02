package org.apache.tika.example;

import org.apache.jackrabbit.commons.xml.ToXmlContentHandler;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.TagRatioParser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nimesh on 24/4/16.
 */
public class CCAdataParsing {

    static final AutoDetectParser parser = new AutoDetectParser();
    private static Parser myparser = null;
    private static Tika tika = null;
    private static Set<String>  hs = new HashSet<String>();
    private static TagRatioParser tagRatioParser = new TagRatioParser();
    static int mycnt = 0;
    static boolean flag =false;

    private static Map<String,Map<String,Integer>> mapper = new HashMap<String, Map<String, Integer>>();

    public static void main(String[] args) throws Exception {

        System.out.println("this is Nimesh");
        String path = "/home/nimesh/CCA";
        TikaConfig config = new TikaConfig("/home/nimesh/599/tika-1.12/tika-config.xml");
        tika = new Tika(config);
        processFile(new File(path));
        //NERWrapper(new File(path));
        //processMetadata(readFile(path, StandardCharsets.UTF_8));

    }


    private static void NERWrapper(File file)
    {
        try {
            String temp = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
            NERExtraction(temp, new Metadata());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(File f) {

        if(mycnt>=1000) {
            makeJson();
            return;
        }
        File files [] = f.listFiles();
        //System.out.println(files.length);
        for(File file : files) {
            if(file.isDirectory())
                processFile(file);
            else {
                try {

                    //extractContent(file);
                    //extractGoodContent(file);
                    extractURLRequestResponse(file);
                    mycnt++;
                }
                catch(Exception e)
                {
                    //System.out.println("Exception in file" + file.getAbsolutePath());
                    continue;
                }

            }
        }
    }

    private static void makeJson() {
        try {
            if (flag == true)
                return;
            flag = true;
            System.out.println("how many times");
            //PrintWriter pw = new PrintWriter(new File("/home/nimesh/newclassification.txt"));
            FileWriter writer = new FileWriter("/home/nimesh/new.csv");
            JSONObject p = new JSONObject();
            JSONArray pa = new JSONArray();
            p.put("name", "classification");
            p.put("children", pa);

            for (String key : mapper.keySet()) {
                JSONObject lcl = new JSONObject();
                JSONArray lcla = new JSONArray();

                Map<String, Integer> urlMapper = mapper.get(key);

                for (String nkey : urlMapper.keySet()) {
                    JSONObject lastObj = new JSONObject();
                    lastObj.put("name", nkey);
                    lastObj.put("size", (Integer) urlMapper.get(nkey));
                    lcla.add(lastObj);
                    writer.append(key+"-"+nkey +"-"+((Integer)urlMapper.get(nkey)).toString());
                    writer.append(',');
                    writer.append("1");
                    writer.append('\n');
                }

                if(lcla.size()>0) {
                    lcl.put("name", key);
                    lcl.put("children", lcla);
                    pa.add(lcl);
                }

            }

            writer.flush();
            writer.close();

            try (FileWriter file = new FileWriter("/home/nimesh/classfication.json")) {
                file.write(p.toJSONString());
                System.out.println("Successfully Copied JSON Object to File...");

                System.out.print(p.toJSONString());


            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }


        catch(Exception e)
        {
            System.out.print("json writing error");
            e.printStackTrace();
        }
        //JSONObject obj = new JSONObject(mapper);
        //System.out.print(obj.toString());

    }


    private static void extractURLRequestResponse(File file)
    {
        try {
            Metadata metadata = new Metadata();
            String temp = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
            int start = temp.indexOf('{');
            int end = temp.lastIndexOf('}');

            JSONParser jparser = new JSONParser();
            Object obj = (Object) jparser.parse(temp.substring(start,end+1));
            JSONObject jobj = (JSONObject)obj;
            //System.out.println("Now the String is after converting to json "+jobj.toJSONString());

            JSONObject response = (JSONObject)jobj.get("response");

            //ContentHandler contentHandler = new ToXMLContentHandler();
            ContentHandler contentHandler = new ToXmlContentHandler();
            Metadata old_metadata = new Metadata();
            ParseContext context = new ParseContext();
            //System.out.print(response.get("body").toString());
            PrintWriter out = new PrintWriter("t.html");
            out.println(response.get("body").toString());
            out.close();
            tagRatioParser.parse(new FileInputStream(new File("/home/nimesh/t.html")), contentHandler, old_metadata, context);
            System.out.print("Tagged Ratio is ");
            String mybody = contentHandler.toString().trim();
            String splitStr[] = mybody.split("\\s+");
            Map<String, Integer> wordCount = new HashMap<>();
            for (String word: splitStr) {
                word = word.toLowerCase();
                if (wordCount.containsKey(word)) {
                    // Map already contains the word key. Just increment it's count by 1
                    wordCount.put(word, wordCount.get(word) + 1);
                } else {
                    // Map doesn't have mapping for word. Add one with count = 1
                    wordCount.put(word, 1);
                }
            }
            int count = 0;
            String myurl = jobj.get("url").toString();
            String myurlarr[] = myurl.split("[^A-Za-z0-9]");
            for(String eword : myurlarr)
            {
                eword = eword.toLowerCase();
                if(wordCount.containsKey(eword))
                    count+=wordCount.get(eword);
            }
            URL aURL = new URL(jobj.get("url").toString());

            String host = aURL.getHost();
            Map<String, Integer> urlCount = null;
            if(mapper.containsKey(host))
            {
                urlCount = mapper.get(host);
            }
            else
            {
                urlCount = new HashMap<String, Integer>();
                mapper.put(host,urlCount);
            }

            if(count !=0) {
                urlCount.put(jobj.get("url").toString(),count);
                System.out.println("File is :" + file);
                System.out.println("Total Count is  :" + splitStr.length);
                System.out.println("Count is : " + count);
            }

            /*
            System.out.println("Info for URL is " + jobj.get("url").toString());
            System.out.println(aURL.getRef());
            System.out.println(aURL.getAuthority());
            System.out.println(aURL.getContent());
            System.out.println(aURL.getFile());
            System.out.println(aURL.getHost());
            System.out.println(aURL.getPath());
            */

            /*
            String query = aURL.getQuery();
            System.out.println(aURL.getHost());

            if(hs.contains(aURL.getHost().toLowerCase().toString()))
                System.out.println("found duplicate" + aURL.getHost());
            else
                hs.add(aURL.getHost().toLowerCase().toString());
            if(true)
                return;
            String q[] = null;
            if (query!=null) {
                q = query.split("=");

                String body = response.get("body").toString();
                if (body.contains(q[0])) {
                    System.out.println("contains" + q[0]);
                    Metadata m1 = new Metadata();
                    NERExtraction(q[0].toLowerCase(Locale.ENGLISH),m1);
                    System.out.println("NImesh Metadata m1 is : "+ m1.toString());
                }
                if (body.contains(q[1])) {
                    System.out.println("contains" + q[1]);

                    Metadata m2 = new Metadata();
                    NERExtraction(q[1].toLowerCase(Locale.ENGLISH),m2);
                    System.out.println("NImesh Metadata m2 is : "+ m2.toString());
                }


            }
            System.out.println("Query" + query);

            NERExtraction(response.get("body").toString(), metadata);
            System.out.println("metadata is : "+ metadata.toString());
        */
        }

        catch (IOException | ParseException e ) {
            System.out.println("Exception in file" + file.getAbsolutePath());
            return;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }


    }
    private static void extractGoodContent(File file) {

        try {
            Metadata metadata = new Metadata();
            String temp = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
            int start = temp.indexOf('{');
            int end = temp.lastIndexOf('}');

            JSONParser jparser = new JSONParser();
            Object obj = (Object) jparser.parse(temp.substring(start,end+1));
            JSONObject jobj = (JSONObject)obj;
            System.out.println("Now the String is after converting to json "+jobj.toJSONString());

            JSONObject response = (JSONObject)jobj.get("response");


            NERExtraction(response.get("body").toString(), metadata);
            //System.out.println("metadata is : "+ metadata.toString());

        }
        catch (IOException | ParseException e ) {
            //System.out.println("Exception in file" + file.getAbsolutePath());
            return;
        }

    }

    //depreceated
    private static void extractContent(File file) {

        Metadata metadata = new Metadata();
        //ContentHandler contentHandler = new ToXMLContentHandler();
        BodyContentHandler contentHandler = new BodyContentHandler();
        ParseContext context = new ParseContext();

        try {
            parser.parse(new FileInputStream(file), contentHandler, metadata, context);
            //System.out.println("OG metadata is :"+ metadata);
            String temp = contentHandler.toString();

            temp = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);


            //System.out.println("Original String is: " + temp);
            byte[] b = temp.getBytes("UTF-8");
            String s = new String(b, "US-ASCII");
            temp =s;
            int start = temp.indexOf('{');
            int end = temp.lastIndexOf('}');
            //temp = temp.substring(temp.indexOf('\n')+1);
            //temp = "{"  +temp.substring(0, temp.lastIndexOf("\n")) + " } ";

            JSONParser jparser = new JSONParser();
            Object obj = (Object) jparser.parse(temp.substring(start,end+1));
            JSONObject jobj = (JSONObject)obj;
            System.out.println("Now the String is after converting to json "+jobj.toJSONString());

            JSONObject response = (JSONObject)jobj.get("response");


            NERExtraction(response.get("body").toString(), metadata);
            if(!response.get("status").equals(""))
                System.out.println("Yay" + file.getAbsolutePath() + " "+ response.get("status"));
            else
                System.out.print(response.get("status"));
        }
        catch (IOException | SAXException | TikaException | ParseException e ) {
            //System.out.println("Exception in file" + file.getAbsolutePath());
            return;
        }
    }


    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static void NERExtraction(String content, Metadata metadata)
    {
        //System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.regex.RegexNERecogniser");
        //System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser");
        System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.opennlp.OpenNLPNERecogniser");

        try {
            tika.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata);
            //System.out.println("NImesh Metadata is : "+ metadata.toString());
            //processMetadata(metadata.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void processMetadata(String s)
    {
        Map<String,List<String>> hm = new HashMap<>();
        String arr[] =s.split("NER_");
        for(String s1 : arr)
            System.out.println(s1.trim());

        /*
        for(String ent : arr)
        {
            String keyVal[] = ent.split("=");
            if(hm.containsKey(keyVal[0]))
            {
                List temp = hm.get(keyVal[0]);
                temp.add(keyVal[1]);
            }
            else
            {
                List<String> temp = new LinkedList();
                temp.add(keyVal[1]);
                hm.put(keyVal[0],temp);
            }

        }

        for(String key : hm.keySet())
        {
            System.out.println(key + " " + hm.get(key).size());
        }
        */
    }



}
