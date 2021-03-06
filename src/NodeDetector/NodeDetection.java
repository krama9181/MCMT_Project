package NodeDetector;

import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class NodeDetection {

	static final double CHUNK_SCORE = 1.0;

	public void NodeDectector(LinkedHashMap<String, LinkedHashSet<String>> splitSentence_for_each_line,
			String TriggerDictionaryPath, String EntityOneDictionaryPath, String AnnFileFolder) throws Exception {

		Metamap mm = new Metamap();
		MetaMapApi api = new MetaMapApiImpl();
		api.setOptions("-y -k <aapp,acty,aggp,amas,amph,anim,anst,antb,arch,bacs,bact,bdsu,bdsy,bhvr,bird,blor,bmod,bodm,bsoj,carb,celc,celf,cell,chem,chvf,chvs,clas,clnd,cnce,crbs,diap,dora,drdd,edac,eehu,eico,elii,emst,enty,enzy,euka,evnt,famg,ffas,fish,fngs,food,ftcn,genf,geoa,gngm,gora,grpa,grup,hcpp,hcro,hlca,horm,humn,idcn,imft,inbe,inch,inpr,irda,lang,lbpr,lipd,mamm,mbrt,mcha,medd,mnob,moft,mosq,nnon,npop,nsba,nusq,ocac,ocdi,opco,orch,orga,orgf,orgm,orgt,ortf,phob,phpr,phsu,plnt,podg,popg,prog,pros,qlco,qnco,rcpt,rept,resa,resd,rnlw,sbst,shro,socb,spco,strd,tmco,topp,virs,vita,vtbt>");
		
		/*
		 * 
		 * Dictionary loading
		 * 
		 */

		// Phenotype Dictionary load
//		MapDictionary<String> Phenotype_dictionary = new MapDictionary<String>();
//		File PDicfile = new File(UMLSDictionaryPath);
//		BufferedReader PDic_br = new BufferedReader(new FileReader(PDicfile));
//		String Pline = null;
//
//		System.out.println("Dictionary loading....");
//		while ((Pline = PDic_br.readLine()) != null) {
//			String[] contents = Pline.split("\t");
//			String CUI = contents[0];
//			String TUI = contents[1];
//			String STR = contents[2];
//
//			Phenotype_dictionary.addEntry(new DictionaryEntry<String>(STR, CUI + "\t" + TUI, CHUNK_SCORE));
//
//		}
//		System.out.println("Phenotype Dictionary is sucessfully loaded");

		// Trigger Dictionary load
		MapDictionary<String> Trigger_dictionary = new MapDictionary<String>();
		File TDicfile = new File(TriggerDictionaryPath);
		BufferedReader TDic_br = new BufferedReader(new FileReader(TDicfile));
		String Tline = null;

		System.out.println("Trigger Dictionary loading....");
		while ((Tline = TDic_br.readLine()) != null) {
			String[] contents = Tline.split("\t");
			String word = contents[0];
			String id = contents[1];

			Trigger_dictionary.addEntry(new DictionaryEntry<String>(word, id, CHUNK_SCORE));

		}
		System.out.println("Trigger Dictionary is sucessfully loaded");

		// EntityOne Dictionary load
		MapDictionary<String> EntityOne_dictionary = new MapDictionary<String>();
		File EntityOneDicfile = new File(EntityOneDictionaryPath);
		BufferedReader EntityOneDic_br = new BufferedReader(new FileReader(EntityOneDicfile));
		String EntityOneline = null;
		
		System.out.println("EntityOne Dictionary loading....");
		while ((EntityOneline = EntityOneDic_br.readLine()) != null) {
			String[] contents = EntityOneline.split("\t");
			String EntityOneName = contents[1];
			String EntityOneRef_ID = contents[0];
			
			EntityOne_dictionary.addEntry(new DictionaryEntry<String>(EntityOneName, EntityOneRef_ID, CHUNK_SCORE));

		}
		System.out.println("EntityOne Dictionary is sucessfully loaded");

		
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Making annotation files in progress........................ ");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");

//		ExactDictionaryChunker P_dictionaryChunkerTF = new ExactDictionaryChunker(Phenotype_dictionary,
//				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		ExactDictionaryChunker T_dictionaryChunkerTF = new ExactDictionaryChunker(Trigger_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		ExactDictionaryChunker EntityOne_dictionaryChunkerTF = new ExactDictionaryChunker(EntityOne_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		
		LinkedHashSet<String> Ptemp_chunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Pchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Tchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> EntityOnechunk_result = new LinkedHashSet<String>();
		
		int processCounting = 0;
		
		for (Entry<String, LinkedHashSet<String>> entry : splitSentence_for_each_line.entrySet()) { // split sentences annotated phenotype.

			String key = entry.getKey();
			String[] key_split = key.split("\t");
			String KindofText = key_split[0];
			String dataType = key_split[1];
			String EntityOneRef_ID = key_split[2];
			String EntityOneName = key_split[3].toLowerCase();
			String allType_text = key_split[4];
						
			StringBuffer makeReference = new StringBuffer();			
			for (int j = 0; j < EntityOneRef_ID.length(); j++) {
				char c = EntityOneRef_ID.charAt(j);
				String c_string = "" + c;
				if (c_string.equals("[")) {
					break;
				} else {
					makeReference.append(c_string);
				}
			}
			
			String EntityOneReference = makeReference.toString();
			String EntityOneID = EntityOneRef_ID.substring(makeReference.length());
						
			LinkedHashSet<String> value = new LinkedHashSet<String>();
			value.clear();
			value = entry.getValue();
			
			String FileName = "";
			FileName = EntityOneRef_ID;
			if(EntityOneRef_ID.length() > 100){
				FileName = EntityOneRef_ID.substring(0, 95);
			}
			else{
				FileName = EntityOneRef_ID;
			}
			
			FileName = RemoveMark(FileName);
			FileName = FileName.trim();
			
			File files = new File(AnnFileFolder + FileName + ".ann");
			BufferedWriter out = new BufferedWriter(new FileWriter(files, true));

			out.write("ID_OriginalText_Reference:" + KindofText + "\t" + dataType + "\t" + EntityOneID + "\t" + EntityOneName + "\t" + EntityOneReference.toUpperCase() + "\t" + allType_text);
			out.newLine();
			

			
			for (String val : value) {
				processCounting++;
				
				if (val.contains("co,ma")) {
					val = val.substring(5);
					out.write("SplitSentence:co,ma\t"+ val);
					
				}
				else{
					out.write("SplitSentence:"+ val);
				}
				
				
				out.newLine();
				
				/*
				 *
				 * Put in the code
				 * 
				 */
//				Ptemp_chunk_result.clear();
//				Pchunk_result.clear();
//				Ptemp_chunk_result = Pchunk(P_dictionaryChunkerTF, val.toLowerCase(), "BROMFED-DM", "9");
//				Pchunk_result = filtering(Ptemp_chunk_result);
//				for (String p : Pchunk_result) {
//					out.write("Phenotype:" + p);
//					out.newLine();
//				}
				
				Ptemp_chunk_result.clear();
				Pchunk_result.clear();
				String tmp_string = mm.preprocessing(val);
				Ptemp_chunk_result = mm.metamap(api, tmp_string);
				Pchunk_result = filtering(Ptemp_chunk_result);
				
				
				for (String p : Pchunk_result) {
					out.write("Phenotype:" + p);
					out.newLine();
				}

				Tchunk_result.clear();
				Tchunk_result = Tchunk(T_dictionaryChunkerTF, val.toLowerCase().trim(), "BROMFED-DM", "9");
				for (String t : Tchunk_result) {
					out.write("Trigger:" + t);
					out.newLine();
				}
				EntityOnechunk_result.clear();
				EntityOnechunk_result = EntityOnechunk(EntityOne_dictionaryChunkerTF, val.toLowerCase().trim(), "BROMFED-DM", "9");
				
				
				
				for (String o : EntityOnechunk_result) {
					if(dataType.equals("sent")){
						out.write("EntityOne:" + o);
						out.newLine();
					}
					else{}
				}
					
				// For delimiters in the ann file.
				out.write("@@@");
				out.newLine();
				if(processCounting % 3000 == 0){
					System.out.println("I just did " + processCounting);
				}
			}
			
			out.close();
		}
	}

	// Annotated phenotype mention filtering (e.g., cold vs common cold -> common cold)
	static LinkedHashSet<String> filtering(LinkedHashSet<String> hash) {
		LinkedHashSet<String> new_chunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> new_new_chunk_result = new LinkedHashSet<String>();

		for (String h1 : hash) {
			String[] contents = h1.split("\t");
			String phrase = contents[0];
			String start = contents[1];
			String end = contents[2];
			String cui = contents[3];
			String tui = contents[4];
			String saveEnd = "";
			int counter = 0;
			for (String h2 : hash) {
				String[] contents_new = h2.split("\t");
				if (start.toLowerCase().trim().equals(contents_new[1].toLowerCase().trim())) {
					saveEnd = saveEnd + contents_new[2] + "\t";
					++counter;
				}
			}
			if (counter == 0) {
				int counter_new = 0;
				for (String h3 : hash) {
					String[] contents_new = h3.split("\t");
					if (end.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
						if (Integer.valueOf(start.toLowerCase().trim()) > Integer
								.valueOf(contents_new[1].toLowerCase().trim())) {
							++counter_new;
						}
					}
				}
				if (counter_new == 0) {
					new_chunk_result.add(h1);
				} else {
					continue;
				}
			} else {
				String[] End_contents = saveEnd.split("\t");
				String newEnd = "";
				for (String n : End_contents) {
					newEnd = n;
				}
				int counter_new_new = 0;
				for (String h3 : hash) {
					String[] contents_new = h3.split("\t");
					if (newEnd.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
						if (Integer.valueOf(start.toLowerCase().trim()) > Integer
								.valueOf(contents_new[1].toLowerCase().trim())) {
							++counter_new_new;
						}
					}
				}
				if (counter_new_new == 0) {
					for (String h3 : hash) {
						String[] contents_new = h3.split("\t");
						if (newEnd.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
							if (start.toLowerCase().trim().equals(contents_new[1].toLowerCase().trim())) {
								new_chunk_result.add(h3);
							}
						}
					}
				} else {
					continue;
				}
			}
		}

		// term1+term2+term3
		for (String str : new_chunk_result) {
			String[] contents = str.split("\t");
			int counter = 0;

			for (String str_new : new_chunk_result) {
				String[] contents2 = str_new.split("\t");
				if (Integer.valueOf(contents[1]) > Integer.valueOf(contents2[1])) {
					if (Integer.valueOf(contents[2]) < Integer.valueOf(contents2[2])) {
						++counter;
					}
				}
			}
			if (counter == 0) {
				new_new_chunk_result.add(str);
			} else {
				continue;
			}
		}
		return new_new_chunk_result;
	}

	static LinkedHashSet<String> EntityOnechunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID)
			throws IOException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		result.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words

			result.add(phrase + "\t" + start + "\t" + end + "\t" + type);
		}
		return result;
	}

	static LinkedHashSet<String> Tchunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID)
			throws IOException {
		LinkedHashMap<String, String> result_map = new LinkedHashMap<String, String>();
		LinkedHashSet<String> result_set = new LinkedHashSet<String>();
		result_map.clear();
		result_set.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words
			if (end + 3 <= text.length()) {
				if (text.toLowerCase().trim().substring(start, end + 3).equals(phrase.toLowerCase().trim() + " by")) {
					continue;
				}
			}
			if (result_map.containsKey(phrase + "\t" + start + "\t" + end)) {
				result_map.put(phrase + "\t" + start + "\t" + end, result_map.get(phrase + "\t" + start + "\t" + end) + "|" + type);
			} else {
				result_map.put(phrase + "\t" + start + "\t" + end, type);
			}
		}

		for (Entry<String, String> entry : result_map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result_set.add(key + "\t" + value);
		}
		return result_set;
	}
	
//	static LinkedHashSet<String> EntityOnechunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID) throws IOException {
//		LinkedHashMap<String, String> result_map = new LinkedHashMap<String, String>();
//		LinkedHashSet<String> result_set = new LinkedHashSet<String>();
//		result_map.clear();
//		result_set.clear();
//
//		Chunking chunking = chunker.chunk(text);
//		for (Chunk chunk : chunking.chunkSet()) {
//			int start = chunk.start();
//			int end = chunk.end();
//			String type = chunk.type();
//			double score = chunk.score();
//			String phrase = text.substring(start, end);
//
//			// stop words
//			if (end + 3 <= text.length()) {
//				if (text.toLowerCase().trim().substring(start, end + 3).equals(phrase.toLowerCase().trim() + " by")) {
//					continue;
//				}
//			}
//			if (result_map.containsKey(phrase + "\t" + start + "\t" + end)) {
//				result_map.put(phrase + "\t" + start + "\t" + end, result_map.get(phrase + "\t" + start + "\t" + end) + "|" + type);
//			} else {
//				result_map.put(phrase + "\t" + start + "\t" + end, type);
//			}
//		}
//
//		for (Entry<String, String> entry : result_map.entrySet()) {
//			String key = entry.getKey();
//			String value = entry.getValue();
//
//			result_set.add(key + "\t" + value);
//			
//			System.out.println(key);
//		}
//		
//		return result_set;
//	}


	static String RemoveMark (String text){
		String clearText = "";
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		clearText =text.replaceAll(match, " ");
		clearText = clearText.replaceAll("\\p{Z}","");
		return clearText;
	}
		
	}


