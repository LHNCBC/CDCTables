package gov.nih.nlm.mor.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class WordCheck {
	
	ArrayList<String> englishWords = new ArrayList<String>();
//	ArrayList<String> candidates = new ArrayList<String>();
//	ArrayList<String> words = new ArrayList<String>();
//	HashMap<String, String> map = new HashMap<String, String>();
//	String filename = "";
//	PrintWriter pw = null;

//	public static void main(String[] args) {
//		WordCheck check = new WordCheck();
//		check.config(args[0]);
//		check.run();
//		check.cleanup();
//	}
	
	public WordCheck() {
		config();		
	}
	

	
	public void config() {
//		readFile(filename, candidates, false);
		readFile("./config/dictionary.txt", englishWords, true);
//		try {
//			pw = new PrintWriter(new File("results.txt"));
//		} catch(Exception e) {
//			e.printStackTrace();
//			System.err.println("Unable to create the results file.");
//		}		
	}
	
//	public void run() {
//		for(String candidate : candidates) {
//			for(int i=0; i < englishWords.size(); i++) {
//				if(englishWords.get(i).equals(candidate.toLowerCase())) {
//					addToMap(candidate, englishWords.get(i), map);
//					break;
//				}
//			}
//		}
//		map.forEach((a,b) -> {
//			pw.println(a);
//			//pw.print("\n");
//			pw.flush();
//			
//		});
//	}
	
	public boolean isWord(String candidate) {
		if(englishWords.contains(candidate.toLowerCase())) return true;
		return false;
	}
	
	public void addToMap(String key, String word, HashMap<String, String> map) {
			String foundWord = word;
			map.put(key, foundWord);
	}
	
//	public void cleanup() {
//		pw.close();		
//	}
	
	private void readFile(String filename, ArrayList<String> list, boolean lc) {
		FileReader file = null;
		BufferedReader buff = null;
		try {
			file = new FileReader(filename);
			buff = new BufferedReader(file);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {	
					if( line != null ) {
						if( !lc ) {
						list.add(line.trim());
						}
						else {
							list.add(line.trim().toLowerCase());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				file.close();
			} catch (Exception e) {
				System.err.println("Error reading the file " + filename);
				e.printStackTrace();
			}
		}
	}

}
