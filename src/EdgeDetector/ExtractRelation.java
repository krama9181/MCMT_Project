package EdgeDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import NodeDetector.Metamap;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class ExtractRelation {

	static String sentence = "";
	static String EntityOneID = "";
	static String EntityOneName = "";
	static String EntityOneReference = "";
	static String dataType = "";
	static String KindofText = "";
	static String originalSentence = "";
		
	public ExtractRelation() {
		sentence = "";
		EntityOneID = "";
		EntityOneName = "";
		EntityOneReference = "";
		dataType = "";
		KindofText = "";
		originalSentence = "";
	}

	static String modelPath = DependencyParser.DEFAULT_MODEL;
	static String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	static DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
	static String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	static String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };

	static LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
	static TreebankLanguagePack tlp = lp.getOp().langpack();
	static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

	public void RelationDetector(String AnnFileFolder, File files) throws IOException {

		BufferedWriter out = new BufferedWriter(new FileWriter(files));
		String default_relation = "";

		/*
		 * 
		 * Get Relation from annotated_FunctionFolder
		 * 
		 */

		LinkedHashSet<String> Result = new LinkedHashSet<String>();
		Result.clear();

		File Functionfolder = new File(AnnFileFolder);
		File[] Function_listOfFiles = Functionfolder.listFiles();

		out.write("Entity1 ID" + "\t" + "Entity1 Name" + "\t" + "reference" + "\t" + "Entity1_start" + "\t"
				+ "Entity2_end" + "\t" + "Relation type" + "\t" + "trigger" + "\t" + "trigger_start" + "\t"
				+ "trigger_end" + "\t" + "phenotype_name" + "\t" + "UMLS_ID" + "\t" + "UMLS_Symantic_type" + "\t"
				+ "pheonotype_start" + "\t" + "phenotype_end" + "\t" + "sentence" +"\t" + "original_Entity1_start" + "\t" 
				+ "original_Entity1_end"+ "\t" + "original_trigger_start" + "\t" + "original_trigger_end"  + "\t" 
				+ "original_pheonotype_start" + "\t" + "original_pheonotype_end" + "\t" + "original_sentence");
		
		out.newLine();

		for (File file : Function_listOfFiles) {
			if (file.isFile()) {
				String fileName = file.getName().replace(".txt", "");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;

				LinkedHashSet<String> annotatedPhenotype = new LinkedHashSet<String>();
				LinkedHashSet<String> annotatedTrigger = new LinkedHashSet<String>();
				LinkedHashSet<String> annotatedEntityOne = new LinkedHashSet<String>();
				String checkLine = "ID_";
				int originalOffsetBase = 0;
				boolean comma_checker = false;	
				
				while ((line = br.readLine()) != null) {
					
					if (checkLine.equals(line.substring(0, 3))) {
						String temp = line.substring(0 + "ID_OriginalText_Reference:".length(), line.length());
						//System.out.println(temp);
						String[] temp_split = temp.split("\t");
						KindofText = temp_split[0];
						dataType = temp_split[1];
						EntityOneID = temp_split[2];
						EntityOneName = temp_split[3];
						EntityOneReference = temp_split[4];
						originalSentence = temp_split[5];
						continue;
					}
					
					String relationDefault = "";

					if (KindofText.equals("fun")) {
						relationDefault = "treat";
					} else if (KindofText.equals("moa")) {
						relationDefault = "treat";
					} else if (KindofText.equals("tox")) {
						relationDefault = "precaution";
					} else {
						relationDefault = "cause";
					}

					if (line.contains("SplitSentence:")) {
						sentence = line.substring(0 + "SplitSentence:".length(), line.length());
						
						if(sentence.contains("co,ma")){
							comma_checker = true;
							String[] temp_sentence = sentence.split("\t");
							sentence = temp_sentence[1];
						}
						
					}
					String phenotype = "";
					if (line.contains("Phenotype:")) {
						phenotype = line.substring(0 + "Phenotype:".length(), line.length());
						annotatedPhenotype.add(phenotype);
					}
					String trigger = "";
					if (line.contains("Trigger:")) {
						trigger = line.substring(0 + "Trigger:".length(), line.length());
						annotatedTrigger.add(trigger);
					}
					String entityOne = "";
					if (line.contains("EntityOne:")) {
						entityOne = line.substring(0 + "EntityOne:".length(), line.length());
						annotatedEntityOne.add(entityOne);
					}
					if (line.equals("@@@")) {
						if (annotatedEntityOne.size() >= 0 && annotatedTrigger.size() >= 0	&& annotatedPhenotype.size() > 0) {
							////// no relation /////
							
							if (annotatedTrigger.size() == 0) {
								if (dataType.equals("term")) {
									if (annotatedEntityOne.size() == 0) {
										for (String p : annotatedPhenotype) {
											String[] pheno = p.split("\t");

											int originalEntityOne_start = 0;
											int originalEntityOne_end = 0;
											int originalTrigger_start = 0;
											int originalTrigger_end = 0;
											int originalPheno_start = Integer.parseInt(pheno[1]) + originalOffsetBase;
											int originalPheno_end = Integer.parseInt(pheno[2]) + originalOffsetBase;

											Result.add("NA" + "\t" + "NA" + "\t" + relationDefault + "\t" + "NA" + "\t"
													+ "NA" + "\t" + "NA" + "\t" + pheno[0] + "\t" + pheno[3] + "\t"
													+ pheno[4] + "\t" + pheno[1] + "\t" + pheno[2] + "\t" + sentence
													+ "\t" + "NA" + "\t" + "NA" + "\t" + "NA" + "\t" + "NA" + "\t"
													+ originalPheno_start + "\t" + originalPheno_end);
										}
									} else {
										for (String e : annotatedEntityOne) {
											String[] entiOne = e.split("\t");
											int e_begin = Integer.valueOf(entiOne[1]);
											int e_end = Integer.valueOf(entiOne[2]);

											for (String p : annotatedPhenotype) {
												String[] pheno = p.split("\t");
												int p_begin = Integer.valueOf(pheno[1]);
												int p_end = Integer.valueOf(pheno[2]);
												int p_begin_minus_e_begin = p_begin - e_begin;

												if (p_begin_minus_e_begin > 0) {

													int originalEntityOne_start = Integer.parseInt(entiOne[1])
															+ originalOffsetBase;
													int originalEntityOne_end = Integer.parseInt(entiOne[2])
															+ originalOffsetBase;
													int originalTrigger_start = 0;
													int originalTrigger_end = 0;
													int originalPheno_start = Integer.parseInt(pheno[1])
															+ originalOffsetBase;
													int originalPheno_end = Integer.parseInt(pheno[2])
															+ originalOffsetBase;

													Result.add(entiOne[1] + "\t" + entiOne[2] + "\t" + relationDefault
															+ "\t" + "NA" + "\t" + "NA" + "\t" + "NA" + "\t" + pheno[0]
															+ "\t" + pheno[3] + "\t" + pheno[4] + "\t" + pheno[1] + "\t"
															+ pheno[2] + "\t" + sentence + "\t"
															+ originalEntityOne_start + "\t" + originalEntityOne_end
															+ "\t" + "NA" + "\t" + "NA" + "\t" + originalPheno_start
															+ "\t" + originalPheno_end);
												}
											}

										}
									}
								}
								else{}
							}
							
							// ### when only one trigger's detected.
							else if (annotatedTrigger.size() == 1) {
								if (annotatedEntityOne.size() == 0) {
									for (String t : annotatedTrigger) {
										String[] trig = t.split("\t");
										int t_begin = Integer.valueOf(trig[1]);
										int t_end = Integer.valueOf(trig[2]);

										for (String p : annotatedPhenotype) {
											String[] pheno = p.split("\t");
											int p_begin = Integer.valueOf(pheno[1]);
											int p_end = Integer.valueOf(pheno[2]);
											int p_begin_minus_t_begin = p_begin - t_begin;

											if (p_begin_minus_t_begin > 0) {
												int[] dep_distance = new int[2];
												dep_distance = cal_distance(sentence, pheno[0], trig[0]);
												int result = Math.abs(dep_distance[0] - dep_distance[1]);
												if (result < 5) {
													
													int originalEntityOne_start = 0;
													int originalEntityOne_end = 0;
													int originalTrigger_start = Integer.parseInt(trig[1])+originalOffsetBase;
													int originalTrigger_end = Integer.parseInt(trig[2])+originalOffsetBase;
													int originalPheno_start = Integer.parseInt(pheno[1])+originalOffsetBase;
													int originalPheno_end = Integer.parseInt(pheno[2])+originalOffsetBase;
													
													Result.add("NA" + "\t" + "NA" + "\t" + trig[3] + "\t" + trig[0]
															+ "\t" + trig[1] + "\t" + trig[2] + "\t" + pheno[0] + "\t"
															+ pheno[3] + "\t" + pheno[4] + "\t" + pheno[1] + "\t"
															+ pheno[2] + "\t" + sentence  + "\t" + "NA" + "\t" 
															+ "NA" + "\t" +originalTrigger_start + "\t" + originalTrigger_end + "\t" + 
															originalPheno_start + "\t" + originalPheno_end);
												}
											}
										}
									}
								}

								else {
									for (String e : annotatedEntityOne) {
										String[] entiOne = e.split("\t");
										int e_begin = Integer.valueOf(entiOne[1]);
										int e_end = Integer.valueOf(entiOne[2]);

										for (String t : annotatedTrigger) {
											String[] trig = t.split("\t");
											int t_begin = Integer.valueOf(trig[1]);
											int t_end = Integer.valueOf(trig[2]);
											int t_begin_minus_e_begin = t_begin - e_begin;

											if (t_begin_minus_e_begin > 0) {
												for (String p : annotatedPhenotype) {
													String[] pheno = p.split("\t");
													int p_begin = Integer.valueOf(pheno[1]);
													int p_end = Integer.valueOf(pheno[2]);
													int p_begin_minus_t_begin = p_begin - t_begin;

													if (p_begin_minus_t_begin > 0) {
														int[] dep_distance = new int[2];
														dep_distance = cal_distance(sentence, pheno[0], trig[0]);
														int result = Math.abs(dep_distance[0] - dep_distance[1]);
														if (result < 5) {
															
															int originalEntityOne_start = Integer.parseInt(entiOne[1])+originalOffsetBase;
															int originalEntityOne_end = Integer.parseInt(entiOne[2])+originalOffsetBase;
															int originalTrigger_start = Integer.parseInt(trig[1])+originalOffsetBase;
															int originalTrigger_end = Integer.parseInt(trig[2])+originalOffsetBase;
															int originalPheno_start = Integer.parseInt(pheno[1])+originalOffsetBase;
															int originalPheno_end = Integer.parseInt(pheno[2])+originalOffsetBase;
															
															Result.add(entiOne[1] + "\t" + entiOne[2] + "\t" + trig[3]
																	+ "\t" + trig[0] + "\t" + trig[1] + "\t" + trig[2]
																	+ "\t" + pheno[0] + "\t" + pheno[3] + "\t"
																	+ pheno[4] + "\t" + pheno[1] + "\t" + pheno[2]
																	+ "\t" + sentence + "\t" +originalEntityOne_start + "\t" 
																	+ originalEntityOne_end + "\t" + originalTrigger_start + "\t" + originalTrigger_end + "\t" + 
																	originalPheno_start + "\t" + originalPheno_end);
														}
													}
												}
											}
										}
									}
								}
							}

							// ### when multiple triggers are detected.
							else {
								if (annotatedEntityOne.size() == 0) {
									for (String p : annotatedPhenotype) {
										String[] pheno = p.split("\t");
										int p_begin = Integer.valueOf(pheno[1]);
										int p_end = Integer.valueOf(pheno[2]);
										int first;
										ArrayList<Integer> myList = new ArrayList<Integer>();
										LinkedHashMap<Integer, String> temp_annotatedTrigger = new LinkedHashMap<Integer, String>();

										for (String t : annotatedTrigger) {
											String[] trig = t.split("\t");
											int t_begin = Integer.valueOf(trig[1]);
											int t_end = Integer.valueOf(trig[2]);
											int p_begin_minus_t_begin = p_begin - t_begin;
											if (p_begin_minus_t_begin > 0) {
												myList.add(p_begin_minus_t_begin);
												temp_annotatedTrigger.put(p_begin_minus_t_begin, t);
											}
										}
										Integer[] arrNum = (Integer[]) myList.toArray(new Integer[myList.size()]);
										first = print2Smallest(arrNum);

										for (Entry<Integer, String> entry : temp_annotatedTrigger.entrySet()) {
											int key = entry.getKey();
											String value = entry.getValue();
											String[] value_split = value.split("\t");

											if (first == key) {
												int[] dep_distance = new int[2];
												dep_distance = cal_distance(sentence, pheno[0], value_split[0]);
												int result = Math.abs(dep_distance[0] - dep_distance[1]);

												if (result < 5) {
													
													int originalEntityOne_start = 0;
													int originalEntityOne_end = 0;
													int originalTrigger_start = Integer.parseInt(value_split[1])+originalOffsetBase;
													int originalTrigger_end = Integer.parseInt(value_split[2])+originalOffsetBase;
													int originalPheno_start = Integer.parseInt(pheno[1])+originalOffsetBase;
													int originalPheno_end = Integer.parseInt(pheno[2])+originalOffsetBase;
													
													Result.add("NA" + "\t" + "NA" + "\t" + value_split[3] + "\t"
															+ value_split[0] + "\t" + value_split[1] + "\t"
															+ value_split[2] + "\t" + pheno[0] + "\t" + pheno[3] + "\t"
															+ pheno[4] + "\t" + pheno[1] + "\t" + pheno[2] + "\t"
															+ sentence + "\t" +"NA" + "\t" 
															+ "NA" + "\t" + originalTrigger_start + "\t" +originalTrigger_end + "\t" + 
															originalPheno_start + "\t" + originalPheno_end);
												}
											}
										}
									}
								}

								else {
									for (String e : annotatedEntityOne) {
										String[] entiOne = e.split("\t");
										int e_begin = Integer.valueOf(entiOne[1]);
										int e_end = Integer.valueOf(entiOne[2]);

										for (String p : annotatedPhenotype) {
											String[] pheno = p.split("\t");
											int p_begin = Integer.valueOf(pheno[1]);
											int p_end = Integer.valueOf(pheno[2]);
											int first;
											ArrayList<Integer> myList = new ArrayList<Integer>();
											LinkedHashMap<Integer, String> temp_annotatedTrigger = new LinkedHashMap<Integer, String>();

											for (String t : annotatedTrigger) {
												String[] trig = t.split("\t");
												int t_begin = Integer.valueOf(trig[1]);
												int t_end = Integer.valueOf(trig[2]);
												int p_begin_minus_t_begin = p_begin - t_begin;
												int t_begin_minus_e_begin = t_begin - e_begin;

												if ((p_begin_minus_t_begin > 0) && (t_begin_minus_e_begin > 0)) {
													myList.add(p_begin_minus_t_begin);
													temp_annotatedTrigger.put(p_begin_minus_t_begin, t);
												}
											}
											Integer[] arrNum = (Integer[]) myList.toArray(new Integer[myList.size()]);
											first = print2Smallest(arrNum);

											for (Entry<Integer, String> entry : temp_annotatedTrigger.entrySet()) {
												int key = entry.getKey();
												String value = entry.getValue();
												String[] value_split = value.split("\t");

												if (first == key) {
													int[] dep_distance = new int[2];
													dep_distance = cal_distance(sentence, pheno[0], value_split[0]);
													int result = Math.abs(dep_distance[0] - dep_distance[1]);

													if (result < 5) {
														
														int originalEntityOne_start = Integer.parseInt(entiOne[1])+originalOffsetBase;
														int originalEntityOne_end = Integer.parseInt(entiOne[2])+originalOffsetBase;
														int originalTrigger_start = Integer.parseInt(value_split[1])+originalOffsetBase;
														int originalTrigger_end = Integer.parseInt(value_split[2])+originalOffsetBase;
														int originalPheno_start = Integer.parseInt(pheno[1])+originalOffsetBase;
														int originalPheno_end = Integer.parseInt(pheno[2])+originalOffsetBase;
														
														Result.add(entiOne[1] + "\t" + entiOne[2] + "\t"
																+ value_split[3] + "\t" + value_split[0] + "\t"
																+ value_split[1] + "\t" + value_split[2] + "\t"
																+ pheno[0] + "\t" + pheno[3] + "\t" + pheno[4] + "\t"
																+ pheno[1] + "\t" + pheno[2] + "\t" + sentence + "\t" + originalEntityOne_start + "\t" 
																+ originalEntityOne_end + "\t" + originalTrigger_start + "\t" + originalTrigger_end + "\t" + 
																originalPheno_start + "\t" + originalPheno_end);
													}
												}
											}
										}
									}
								}
							}
						}
						
						
						for (String o : Result) {
							out.write(EntityOneID.toUpperCase() + "\t" + EntityOneName + "\t" + EntityOneReference
									+ "\t" + o + "\t" + originalSentence);
							out.newLine();
						}

						Result.clear();
						annotatedPhenotype.clear();
						annotatedTrigger.clear();
						annotatedEntityOne.clear();
						
						if(dataType.equals("sent")){
							originalOffsetBase += sentence.length()+1;
						}
						
						else if (dataType.equals("phra")) {
							if (comma_checker){
								originalOffsetBase += sentence.length() + 2;
								comma_checker = false;
							}
								
							else
								originalOffsetBase += sentence.length() + 3;
							
						}
						
						
					}
				}
			}
		}
		out.close();
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("All the processes done........................ ");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
	}

	static int print2Smallest(Integer arr[]) {

		int first, second, arr_size = arr.length;

		first = Integer.MAX_VALUE;
		for (int i = 0; i < arr_size; i++) {
			if (arr[i] < first) {
				first = arr[i];
			}
		}
		return first;
	}

	/// cal distance from word to root
	static int[] cal_distance(String main_sentence, String entity2, String trigger) {

		int[] distance = new int[2]; // 0 : from entity2, 1 : from trigger

		String text = main_sentence;
		text = text.toLowerCase();
		text = text.replace(".", "");

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

		Iterator<List<HasWord>> it = tokenizer.iterator();
		List<HasWord> sentence = null;

		while (it.hasNext()) {
			sentence = it.next();
		}
		Tree parse = lp.apply(sentence);
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection tdl = gs.typedDependencies();

		int length = tdl.size();

		String[] array = new String[length];
		String[] array_temp = new String[length];
		String[] token_array = new String[length];

		int count_2 = 0;
		for (Iterator<TypedDependency> iter = tdl.iterator(); iter.hasNext();) {
			TypedDependency var = iter.next();
			String dependencyType = var.reln().getShortName();

			String token = var.dep().toString();
			String Parent = var.gov().toString();
			// Parent = Parent.substring(0, Parent.length()-3);

			String token_ch = token;
			String parent_ch = Parent;

			String regBigAlpha = "ABCDEFGHIJKMNLOPQRSTUVWXYZ";

			StringBuffer sb = new StringBuffer();
			////////////////// Token ////////////////////////////
			for (int j = 0; j < token_ch.length(); j++) {
				char c = token_ch.charAt(j);
				String c_string = "" + c;

				if (regBigAlpha.contains(c_string)) {
				} else {
					sb.append(c_string);
				}
			}
			String token_result = sb.toString();

			if (token_result.length() > 1) {
				token_result = token_result.substring(0, token_result.length() - 1);
			}
			///////////////////// Parent ////////////////////////////
			StringBuffer sb_2 = new StringBuffer();
			for (int j = 0; j < parent_ch.length(); j++) {
				char c = parent_ch.charAt(j);
				String c_string = "" + c;
				if (regBigAlpha.contains(c_string)) {
				} else {
					sb_2.append(c_string);
				}
			}
			String parent_result = sb_2.toString();
			if (parent_result.length() > 1) {
				parent_result = parent_result.substring(0, parent_result.length() - 1);
			} else {
				parent_result = "root";
			}

			array[count_2] = token_result + "\t" + parent_result;
			array_temp[count_2] = token_result + "\t" + parent_result;
			token_array[count_2] = token_result;
			count_2++;
		}

		String entity2_clear = "";
		String entity2_up = "";
		String entity2_down = "";
		String[] array_2 = null;

		int count_3 = 0;

		String[] array_entity2 = null;
		array_entity2 = entity2.split(" ");

		for (int x = 0; x < count_2; x++) {
			for (int y = 0; y < array_entity2.length; y++) {
				if (array_entity2[y].equals(token_array[x])) {
					entity2_clear = array_entity2[y];
				}
			}
		}

		/////// get the distance from entity2 to root///////

		while (!("root".equals(entity2_up)) && count_3 < length) { // array_2[0]
																	// ->
																	// entity2_up
			array_2 = array[count_3].split("\t");
			if (entity2_clear.equals(array_2[0])) {
				array[count_3] = "99999" + "\t" + "99999";
				entity2_down = array_2[0];
				entity2_up = array_2[1];
				entity2_clear = entity2_up;
				count_3 = 0;
				distance[0]++;
				// System.out.println(array_2[0] + " " + entity2_up + " " +
				// entity2_clear);
			}
			count_3++;
		}

		count_3 = 0;
		entity2_clear = trigger;
		entity2_up = "";

		while (!("root".equals(entity2_up)) && count_3 < length) { // array_2[0]
																	// ->
																	// entity2_up
			array_2 = array_temp[count_3].split("\t");
			if (entity2_clear.equals(array_2[0])) {
				array_temp[count_3] = "99999" + "\t" + "99999";
				entity2_down = array_2[0];
				entity2_up = array_2[1];
				entity2_clear = entity2_up;
				count_3 = 0;
				distance[1]++;
			}
			count_3++;
		}

		//////// trigger and entity2 are same line ////////
		// while (!(trigger.equals(entity2_up)) && count_3 < length) {
		// //array_2[0] -> entity2_up
		// array_2 = array_temp[count_3].split("\t");
		// if (entity2_clear.equals(array_2[0]) ) {
		// array_temp[count_3] = "99999"+"\t"+"99999";
		// entity2_down = array_2[0];
		// entity2_up = array_2[1];
		// entity2_clear = entity2_up;
		// count_3 = 0;
		// distance[0]++;
		// }
		// count_3++;
		// }

		return distance;
	}
}
