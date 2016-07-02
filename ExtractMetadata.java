import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.TagRatioParser;
import org.apache.tika.parser.geo.topic.GeoParser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ExtractMetadata {
	static JSONObject idMap = new JSONObject();
	private static TagRatioParser tagRatioParser = new TagRatioParser();
	private static GeoParser geoParser = new GeoParser();
	private static Tika tika = null;
	private static String outputDir = "/home/siddharth/Desktop/IndexFiles";
	private static String contentDir = "/home/siddharth/Desktop/ContentFiles";
	private static int count = 0;
	private static boolean found = false;
	
	public static void main(String args[]) {
		initializeMap();
		try {
			tika = new Tika(new TikaConfig("/home/siddharth/Desktop/tika-config.xml"));
		} catch (TikaException | IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Initialized Map");
		for(File file : new File(args[0]).listFiles()) {
			if(count == 1500)
				break;
			if(idMap.containsKey(file.getName())) {

				// if(!found ) {
				// 	if(file.getName().equals("8720C53D18B23493064F94016FC64FC089B568C3187A01F53AE2A0B894177581"))
				// 		found = true;
				// 	continue;
				// }


				System.out.println(file.getAbsolutePath());
				extractMetadata(file);
				count++ ;
			}
		}
	}
	
	public static void extractMetadata(File file) {
		// System.out.println("Start Extract Metadata");
		Metadata old_metadata = new Metadata();
		ContentHandler contentHandler = new ToXMLContentHandler();
		ParseContext context = new ParseContext();
		try {
			tagRatioParser.parse(new FileInputStream(file), contentHandler, old_metadata, context);
			geoParser.parse(new ByteArrayInputStream(contentHandler.toString().getBytes(StandardCharsets.UTF_8)), contentHandler, old_metadata, context);
			old_metadata.add("original_file_size", file.length()+"");
			// printMetadata(old_metadata);
			Metadata new_metadata = new Metadata();
			extractNER(contentHandler.toString(), new_metadata, file.getName(), old_metadata);
			// System.out.println("After: ");
			// printMetadata(new_metadata);
		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// System.out.println("End Extract Metadata");
		
	}
	
	private static void printMetadata(Metadata metadata) {
		for(String name : metadata.names()) {
            System.out.println("\""+name + "\": " + metadata.get(name));
        }
	}
	
private static void extractNER(String content, Metadata metadata, String fileName, Metadata old_metadata) {
	
		// System.out.println("--------------");
		// System.out.println(content);
		// System.out.println("--------------");
		
		System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL, "org.apache.tika.parser.ner.regex.RegexNERecogniser");
		try {
//			System.out.println("String content:"+content);
			tika.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata);
			
				//SWEET CONCEPTS
				Set<String> measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_SWEET_CONCEPT")));
				JSONArray sweet_array = null;
				if(!measurements.isEmpty()) {
					sweet_array = new JSONArray();
					for(String m : measurements) 
						sweet_array.put(m);
				}
				
				//Temperature
				measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_R_TEMP_MEASURE")));
				JSONArray temperature_array = null;
				if(!measurements.isEmpty()) {
					temperature_array = new JSONArray();
					for(String m : measurements) 
						temperature_array.put(m);
				}
				
				//mass
				measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_R_MASS_MEASURE")));
				JSONArray mass_array = null;
				if(!measurements.isEmpty()) {
					mass_array = new JSONArray();
					for(String m : measurements) 
						mass_array.put(m);
				}
				
				//time
				measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_R_TIME_MEASURE")));
				JSONArray time_array = null;
				if(!measurements.isEmpty()) {
					time_array = new JSONArray();
					for(String m : measurements) 
						time_array.put(m);
				}
				
				//length
				measurements = new HashSet<>(Arrays.asList(metadata.getValues("NER_R_LENGTH_MEASURE")));
				JSONArray length_array = null;
				if(!measurements.isEmpty()) {
					length_array = new JSONArray();
					for(String m : measurements) 
						length_array.put(m);
				}
				
				JSONObject obj = new JSONObject();
				obj.put("id", idMap.get(fileName));
				for(String name : old_metadata.names()) {
		            obj.put(name, old_metadata.get(name));
		        }
		        String parserInfo[] = old_metadata.getValues("X-Parsed-By");
		        JSONArray parserArray = new JSONArray();
		        for(String info : parserInfo) 
		        	parserArray.put(info);

		        obj.put("X-Parsed-By", parserArray);
				
				if(temperature_array != null)
					obj.put("temperature_measurements", temperature_array);
				if(sweet_array != null)
					obj.put("sweet_concepts",  sweet_array);
				if(length_array != null)
					obj.put("length_measurements", length_array);
				if(mass_array != null)
					obj.put("mass_measurements", mass_array);
				if(time_array != null)
					obj.put("time_measurements", time_array);
				
				//inserting the size of metadata
				obj.put("metadata_size", obj.toJSONString().getBytes().length+"");
				
				//inserting the size/length of extracted tag-ratio content
				obj.put("content_size", content.getBytes().length+"");
				
				// PrintWriter output = new PrintWriter(contentDir+"/"+fileName);
				// output.println(content);
				// output.close();

				
				PrintWriter output = new PrintWriter(outputDir+"/"+fileName+".json");
				output.println("["+obj.toJSONString()+"]");
				output.close();
				
			System.clearProperty(NamedEntityParser.SYS_PROP_NER_IMPL);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initializeMap() {
		System.out.println("--");
		JSONParser parser = new JSONParser();
		try {
			idMap = (JSONObject) parser.parse(FileUtils.readFileToString(new File("URL_MAP.txt")));
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
