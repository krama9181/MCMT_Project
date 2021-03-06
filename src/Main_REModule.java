import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import EdgeDetector.ExtractRelation;
import LingPipe.SentenceSplitter;
import NodeDetector.NodeDetection;

public class Main_REModule {
	
	static final double CHUNK_SCORE = 1.0;
	 
	
	///Remodel change////
	
	/*S
	 * 
	 * File Path Declaration ( You should change the path )
	 * 
	 */
	static String path = "C:/Users/admin/Desktop/workspace/";
	
	static String InputTextFile = path + "input/sentence_data_1.txt";
	//static String InputTextFile = path + "raw_inputData/TCMID_herb.txt"; // input text  
	static String AnnFileFolder = path + "annotated/"; // Folder path whose annotation files will be stored.
	//static String TriggerDictionaryPath = path + "dictionary/Integrated_Triggers.txt"; // Trigger dictionary path
	
	static String TriggerDictionaryPath = path + "dictionary/Revised_dictionary.txt";	// Revised dictionary path
	
	
	static String RelationResultOutputPath = path + "Output/sentenceData_output_1.tsv"; // Final output path
	
	//static String UMLSDictionaryPath = path + "dictionary/UMLS_DICTIONARY_FOR_20_SEM.txt"; // UMLS dictionary path
	public static void main(String[] args) throws Exception {
		////////
		createFolder();			
		SentenceSplitter splitter = new SentenceSplitter();
		NodeDetection node_detector = new NodeDetection();
		ExtractRelation relation = new ExtractRelation();

		/*
		 * 
		 * Enter Input Texts
		 * 
		 */
		File FunctionTextFile = new File(InputTextFile);

		/*
		 * 
		 * Sentence splitter
		 * 
		 */
		LinkedHashMap<String, LinkedHashSet<String>> splitSentence_for_each_line = new LinkedHashMap<String, LinkedHashSet<String>>();
		splitSentence_for_each_line = splitter.Splitter(FunctionTextFile);

		/*
		 * 
		 * Phenotype Detector, Trigger Detector
		 * 
		 */
		node_detector.NodeDectector(splitSentence_for_each_line, TriggerDictionaryPath,	InputTextFile, AnnFileFolder);
		/*
		 * 
		 * //Relation Extraction
		 * 
		 */
		LinkedHashSet<String> output = new LinkedHashSet<String>();
		File files = new File(RelationResultOutputPath); // print output
		relation.RelationDetector(AnnFileFolder, files);					
	}
	
	public static void createFolder() {
		File annotationFolder = new File(AnnFileFolder);
		if (!annotationFolder.exists()){
			annotationFolder.mkdir();
		}
		else{
			removeDIR(AnnFileFolder);
			annotationFolder.mkdir();
		}
	}
	
	public static void removeDIR(String source){
		File[] listFile = new File(source).listFiles(); 
		try{
			if(listFile.length > 0){
				for(int i = 0 ; i < listFile.length ; i++){
					if(listFile[i].isFile()){
						listFile[i].delete(); 
					}else{
						removeDIR(listFile[i].getPath());
					}
					listFile[i].delete();
				}
			}
		}catch(Exception e){
			System.err.println(System.err);
			System.exit(-1); 
		}
			
	}
	
}
