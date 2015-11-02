package apriorihash;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;


public class AprioriHash {

    int Tlim,Ilim,minsupport;
    boolean DatasetTable [][];
    File f;
    HashMap<HashSet<Integer>,Integer>k_frequentItemSet=null;
    int [] bucketCount;

    AprioriHash(int Tlim,int Ilim,int minsupport,File f){
        this.Tlim=Tlim;
        this.Ilim=Ilim;
        this.minsupport=minsupport;
        this.f=f;
        DatasetTable =new boolean[Tlim][Ilim];
        k_frequentItemSet=new HashMap<HashSet<Integer>,Integer>();
        bucketCount=new int[7];
        for(int i=0;i<7;i++)
            bucketCount[i]=0;
    }
    
    void CreateRandomDataset() throws IOException{
       int n; 
       Scanner s=new Scanner(System.in);       
       Vector<Integer> v=new Vector();
        FileWriter fw=null;
        
        try{
            fw=new FileWriter(f);
        }
        catch(IOException e){
            System.out.println("Problem opening file");
            System.exit(-1);
        }
        
        fw.write("TID\tList Of ItemIDs\n");
        for(int i=0;i<Tlim;i++){
            fw.write("T"+i+"\t");
            
            v.clear();
            System.out.println("Enter no of items for T "+ i);
            n=s.nextInt();
            int j=0;
            while(j<n){
                int t=(int)(Math.random()*Ilim);
                if(!v.contains(t)){
                    v.add(t);
                    j++;
                }    
            }
            Collections.sort(v);
            System.out.println("Size"+v.size());
                    
            for(int iter=0;iter<v.size();iter++)
                fw.write("I"+v.get(iter)+" ");
                        
            fw.write("\n");
            //fw.flush();
        }
        fw.close();
    }
    
    void ReadDatasetFile() throws FileNotFoundException,IOException{
       FileReader fr=new FileReader(f);
       for(int i=0;i<Tlim;i++)
        for(int j=0;j<Ilim;j++)
            DatasetTable[i][j]=false;


        while(fr.read()!='\n');
        int ch=fr.read();
        int transno=0,itemno=0;

        while(ch!=-1){
            if(ch=='\n')
                transno++;
            if((char)ch=='I'){
                itemno=0;
                while((char)ch!=' '){
                    int c=fr.read();
                    itemno=itemno*10+c-48;
                    ch=fr.read();
                }
                DatasetTable[transno][itemno]=true;
            }
            ch=fr.read();
        }

        System.out.println("Table created");
    }
    
    
    void fillBuckets(int[] set,int k){
        System.out.print("Filling buckets for : ");
        for(int i=0;i<set.length;i++)
            System.out.print(set[i]+"  ");
        System.out.println();int bno=0;
        int factor=(int) Math.pow(10, k-1);
        for(int i=0;i<set.length;i++){
            bno+=set[i]*factor;
            factor/=10;
        }
        bno=bno%7;
        bucketCount[bno]++;
        System.out.print("bucket count: ");
        for(int i=0;i<7;i++)
        System.out.print(bucketCount[i]+"   ");
        System.out.println();
    }
    
    void checkCount(int[] subset,int k,HashMap<HashSet<Integer>,Integer> tempfrequent){
        HashSet<Integer> item=new HashSet<Integer>();
        for(int i=0;i<subset.length;i++)
            item.add(subset[i]);
        int d=10;int bno=0;
        int factor=(int) Math.pow(d, k-1);
        for(int i=0;i<subset.length;i++){
            bno+=subset[i]*factor;
            factor/=10;
        }
        bno=bno%7;
        
        
        if(bucketCount[bno]>=minsupport){
             if(tempfrequent.containsKey(item)){
                  int v=tempfrequent.get(item);    
                      tempfrequent.remove(item);
                      tempfrequent.put(item, v+1);
                  }
             else
                  tempfrequent.put(item, 1);
                
            }
    }
    
    void pruneSubsets(int[] set, int[] subset, int subsetSize, int nextIndex, HashMap<HashSet<Integer>,Integer> tempfrequent) {
        if (subsetSize == subset.length) {
            checkCount(subset,subsetSize,tempfrequent);
        } else {
            for (int j = nextIndex; j < set.length; j++) {
                subset[subsetSize] = set[j];
                pruneSubsets(set, subset, subsetSize + 1, j + 1,tempfrequent);
            }
        }
    }
    
    
   HashMap<HashSet<Integer>,Integer> generateCandidateItemSet(int k){
        
        HashSet<Integer> item=null;
        List<Integer> set=null;
        HashMap<HashSet<Integer>,Integer> tempfrequent=new  HashMap<HashSet<Integer>,Integer>();
        for(int tno=0;tno<Tlim;tno++){
            set=new ArrayList<Integer>();
            for(int ino=0;ino<Ilim;ino++){
                if(DatasetTable[tno][ino])
                    set.add(ino);
            }
        int[] bigset=new int[set.size()];
        for(int it=0;it<set.size();it++)
            bigset[it]=set.get(it);
        int[] subset = new int[k];
        pruneSubsets(bigset, subset, 0, 0,tempfrequent);
        HashMap<HashSet<Integer>,Integer> temp=new HashMap<HashSet<Integer>,Integer>();
        }
        
        return tempfrequent;

    }
    
        
    HashMap<HashSet<Integer>,Integer> compareCounts(HashMap<HashSet<Integer>,Integer>CandidateItemSet){
        Set<Entry<HashSet<Integer>,Integer>> EntrySet= CandidateItemSet.entrySet();
        HashMap<HashSet<Integer>,Integer> finalset=new HashMap<HashSet<Integer>,Integer>();
        for(Map.Entry<HashSet<Integer>,Integer> entry:EntrySet){
            if(entry.getValue()>=minsupport)
                finalset.put(entry.getKey(), entry.getValue());
        }
        return finalset;
    }
   
    
   void processSubsets(int[] set, int k) {
        int[] subset = new int[k];
        processLargerSubsets(set, subset, 0, 0);
    }

   void processLargerSubsets(int[] set, int[] subset, int subsetSize, int nextIndex) {
        if (subsetSize == subset.length) {
            fillBuckets(subset,subsetSize);
        } else {
            for (int j = nextIndex; j < set.length; j++) {
                subset[subsetSize] = set[j];
                processLargerSubsets(set, subset, subsetSize + 1, j + 1);
            }
        }
    } 
    
    
    
    
    
    
    

    void hashAlgo(int k){
       HashMap<HashSet<Integer>,Integer> CandidateItemSet=null;
       for(int j=0;j<7;j++)
           bucketCount[j]=0;
        int tno,ino;
        List<Integer>itemlist=null; 
        HashSet<Integer> item = null;
        for(tno=0;tno<Tlim;tno++){
            itemlist=new ArrayList();
            //Make itemlist
            for(ino=0;ino<Ilim;ino++){
                if(DatasetTable[tno][ino]){
                    itemlist.add(ino);
                    if(k==2){
                        item=new HashSet<Integer>();
                        item.add(ino);
                        if(k_frequentItemSet.containsKey(item))
                        {
                            int v=k_frequentItemSet.get(item);
                            k_frequentItemSet.remove(item);
                            k_frequentItemSet.put(item,v+1);
                        }
                        else
                            k_frequentItemSet.put(item, 1);
                    }
                }

            }
            int [] items=new int[itemlist.size()];
            for(int l=0;l<itemlist.size();l++)
                items[l]=itemlist.get(l);
            processSubsets(items,k);
            
        }
        
                
       if(k==2) 
       {System.out.println("\n\n1-Candidateitemset "+k_frequentItemSet);
       k_frequentItemSet=compareCounts(k_frequentItemSet);
       System.out.println("1-Itemset "+k_frequentItemSet+"\n\n");}
       
       
       CandidateItemSet=generateCandidateItemSet(k);
       System.out.println("\n\n"+k+ " -CandidateItemSet "+CandidateItemSet);
       k_frequentItemSet=compareCounts(CandidateItemSet);
       System.out.println(k+" -Frequent Itemset "+k_frequentItemSet);
    }

    
      
    public static void main(String[] args) throws IOException {
        int Tlim=9,Ilim=5,minsupport=2;
        File f=new File("dataset.txt");
        Scanner s=new Scanner(System.in);       
       // System.out.println("Enter no of transactions");
        //Tlim=s.nextInt();
       // System.out.println("Enter no of items");
       // Ilim=s.nextInt();
        //Initialisation Completed

       AprioriHash a=new AprioriHash(Tlim,Ilim,minsupport,f);
         
       // a.CreateRandomDataset();//write a random dataset into file
        a.ReadDatasetFile();//store transaction details into 2-D array "DatasetTable "
        //a.onefrequentItemset();
        
        for(int i=2;i<Ilim;i++){
            a.hashAlgo(i);
          
    }
    }
}
