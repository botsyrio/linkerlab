//David A. Foley (df1593)
//Operating Systems, Prof. Gottlieb
//Lab 1
//9.19.17

package linkerLab;
import java.util.*;

public class Linker {
	public static void main (String[] args){
		Scanner scan = new Scanner(System.in);
		int numModules = scan.nextInt();//captures number of modules
		scan.nextLine();
		int numLines = numModules*3;//extrapolates number of lines from the number of modules
		String lines [][] = new String [numLines][];//creates 2-dimensional array which will store the input
		
		for(int i=0; i<numLines; i++){//input capture
			int tokens = Integer.valueOf(scan.next());
			
			if(i%3==1||i%3==0) {lines[i] = new String[2*tokens+1];}//case where a definition/use list is captured
			//accounts for the fact that each entry in the definition/use list is 2 strings long
			
			else {lines[i] = new String[tokens+1];}//case where a line of program text is captured
			//accounts for the fact that each entry in the line of program text is 1 string long
			
			lines[i][0]=Integer.valueOf(tokens).toString();//capture the first number in the line
			
			if(i%3==0 || i%3 ==1){//capture definition/use lists
				for(int j=1; j<=tokens*2; j++){
					lines[i][j] = scan.next();
				}
			}//end capture definition/use list
			
			else{//capture program text
				for(int j=1; j<=tokens; j++){
					lines[i][j] = scan.next();
				}
			}//end capture program text
			
		}//end input capture
		scan.close();
		
		ArrayList<String> symNames = new ArrayList<String>();//table of symbol names
		ArrayList<Integer> symIndices = new ArrayList<Integer>();//table of corresponding symbol locations
		ArrayList<Integer> nodeList = new ArrayList<Integer>();//list of nodes post-relative address relocation and external reference resolution
		int baseAdds [] = new int [numModules];//base addresses of the modules
		
		int numSyms;//these 4 are used in pass 1
		String symName;
		Integer symIndex;
		int currBaseAdd = 0;

		for(int i=0; i<numLines; i+=3){//start pass 1
			int j = 0;//index of the current element in the current line
			if(!(lines[i][j].equals("0"))){//makes sure we're not working with an empty definition list
				numSyms = Integer.valueOf(lines[i][j]);//number of symbols defined on this line
				j++;
			
				while (numSyms>0){ 
					symName = lines[i][j];
					if(symNames.contains(symName)){//error checking
						System.out.println("ERROR: "+symName+ " is multiply defined");
						j+=2;//iterates over the redundant element definition
					}
					else{//case where new symbol is defined
						symNames.add(symName);
						j++;
						symIndex = Integer.valueOf(lines[i][j]);// captures the location of the symbol as given in the definition list
						
						if(symIndex.compareTo(Integer.valueOf(lines[i+2][0])-1)>0){//error check
						System.out.println("ERROR: The address in the definition of "+symName+ " (module "+i/3+") exceeds the size of the module");
							symIndex = 0;//treats location as 0 (relative)
						}//end error check
						
						symIndices.add(symIndex+currBaseAdd);//stores symbol index, offsetting by the relative address of the current module
						j++;
					}//end case where a new symbol is defined	
					numSyms--;//move to next symbol definition in the line
				}
			}
			baseAdds[i/3]=currBaseAdd;//store base address of each module
			currBaseAdd += Integer.valueOf(lines[i+2][0]);
		}//end pass 1
		
		ArrayList<Boolean> hasBeenUsed = new ArrayList<Boolean>();//creates arraylist to keep track of whether all the symbols have been used
		for(int i=0; i<symNames.size(); i++)
			hasBeenUsed.add(false);
		
		
		for(int i=0; i<numModules; i++){//start pass 2
			int uses = i*3+1;//line number of the use list in the current module
			int prog = uses+1;//line number of the address list in the current module
			
			if(!(lines[uses][0].equals("0"))){//resolve external references
				for(int res=1; res<lines[uses].length; res+=2){
						int nextNode = Integer.valueOf(lines[uses][res+1]);
						while(nextNode!=777){//makes sure we haven't arrived at the end of the linked list of uses
							int node = Integer.valueOf(lines[prog][nextNode+1]);
							if(node%10==1) System.out.println("ERROR: the address "+node/10+ " which appears in the use list of module " +i+" is an immediate address");
							node = node/10;
							int lastNode=nextNode;
							nextNode = node%1000;
							node = (node/1000)*1000;
							if(symNames.contains(lines[uses][res])){//locates the symbol in the table, uses the memory location of the variable to resolve the reference
								hasBeenUsed.set(symNames.indexOf(lines[uses][res]), true);//records the fact that the symbol has been used
								node+=symIndices.get(symNames.indexOf(lines[uses][res]));
							}//end address modification	
							else System.out.println("ERROR: symbol "+lines[uses][res]+" is used in module "+i+" but not defined");//uses value 0
							lines[prog][lastNode+1] = Integer.toString(node);//saves the resolved external address in the table
						}
					
				}	
			}//end resolving external references
			
			if(!(lines[prog][0].equals("0"))){//relocate relative addresses and prepare all nodes to be entered into the memory map
				for(int relocate = 1; relocate<=Integer.valueOf(lines[prog][0]); relocate++){
					int node = Integer.valueOf(lines[prog][relocate]);
					if(node>9999 && node%10==4){//catch unused external addresses
						System.out.println("ERROR: external address "+node+" in module "+i+" is not on a use list");
						node = node/10;
					}
					else if(node>9999 && node%10==3) {node=(node/10)+baseAdds[i];}//relocate relative addresses
					else if(node>9999&&!(node%10==4)) node = node/10;//prepare immediate and absolute addresses
					nodeList.add(node);
				}
			}//nodes loaded and ready for the memory map
		}//end pass 2
		
		for(int i=0; i<hasBeenUsed.size(); i++){//error check
			if(hasBeenUsed.get(i)==false){
				System.out.println("WARNING: symbol " +symNames.get(i)+" was defined but not used");
			}
		}
		
		System.out.println();//print symbol table
		System.out.println("Symbol Table: ");
		for(int i = 0; i<symNames.size(); i++)
			System.out.println(symNames.get(i)+"="+symIndices.get(i));
		//end print symbol table
		
		System.out.println();//print memory map
		System.out.println("Memory Map: ");
		for(int i = 0; i<nodeList.size(); i++)
			System.out.println(i+":  "+nodeList.get(i));
		//end print memory map
	}
}
