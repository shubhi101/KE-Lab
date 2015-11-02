package fptree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.List;

public class FPtree {
    protected class FPtreeNode {
        private ItemNode node = null; 
        private FPtreeNode[] childRefs = null;

    protected FPtreeNode() {
    }  
    protected FPtreeNode(ItemNode newNode) {
        node = newNode; //Add a new node to the fptree
    }
    
    }
    
    private class ItemNode {
        private int itemNo;
    private int itemCount;
    private ItemNode parentRef = null;
    private ItemNode nodeLink = null;
    
    private ItemNode() {
    }
    
    /** name = itemset identifier. 
    support= the support value for the itemset.
    backRef = link to the parent node. */
    
    private ItemNode(int name,ItemNode backRef) {
        itemNo = name;
        itemCount = 1;
        parentRef = backRef;
        }
    }
    
    protected class HeaderTable {
    protected int itemNo; //ItemID
        protected ItemNode nodeLink = null;
    protected HeaderTable (int n) {
        itemNo = n;
        }  
        }
    
    //====================initialise Global variables=================
    protected FPtreeNode root = null;
    protected HeaderTable[] headerTable;        
    /** Number of nodes created. */
    private int numNodes;
    /** Temporary storage for an index into an array of FP-tree nodes. </P>
    Used when reassigning child reference arrays. */
    private int tempIndex  = 0;     
    int Tlim;
    int Ilim;
    int minsupport;
    List <List<Integer>> DataArray;
    int[] supportCounts;
    File f;
    boolean[][] DatasetTable;
    //==============================Constructor========================

    public FPtree(int Tlim,int Ilim,int minsupport,File f){
        root = new FPtreeNode();
    headerTable = new HeaderTable[Ilim];
    for (int i=0;i<headerTable.length;i++) {
        headerTable[i] = new HeaderTable(i);
    }
        this.Tlim=Tlim;
        this.Ilim=Ilim;
        DataArray = new ArrayList();
        this.f=f;
        this.minsupport=minsupport;
        DatasetTable=new boolean[Tlim][Ilim];
        supportCounts=new int[Ilim];
        for(int i=0;i<supportCounts.length;i++)
            supportCounts[i]=0;
    }
    //=============================Methods============================
    
       void ReadDatasetFile() throws FileNotFoundException,IOException{
       FileReader fr=new FileReader(f);
       for(int i=0;i<Tlim;i++)
        for(int j=0;j<Ilim;j++)
            DatasetTable[i][j]=false;


        while(fr.read()!='\n');
        int ch=fr.read();
        int transno=0,itemno=0;
        List<Integer> itemList;
        itemList=new ArrayList();
        while(ch!=-1){
            if(ch=='\n')
            {
                DataArray.add(transno, itemList);
                itemList=new ArrayList();
                transno++;
            }
            if((char)ch=='I'){
                itemno=0;
                while((char)ch!=' '){
                    int c=fr.read();
                    itemno=itemno*10+c-48;
                    ch=fr.read();
                }
                DatasetTable[transno][itemno]=true;
                itemList.add(itemno);
                supportCounts[itemno]++;
            }
            ch=fr.read();
        }

        System.out.println("Table created");
        System.out.println("List created\n");
        System.out.println(DataArray);
        for(int i=0;i<supportCounts.length;i++)
        System.out.print(supportCounts[i]+"  ");
    }

    public void makeHeader(){
        int max=0,minidx=0;
        for(int i=0;i<headerTable.length;i++){
            max=supportCounts[headerTable[i].itemNo];
            minidx=i;
            for(int j=i+1;j<headerTable.length;j++){
                if(supportCounts[headerTable[j].itemNo]>max){
                    max=supportCounts[headerTable[j].itemNo];
                    minidx=j;
                }
            }
            int t=headerTable[i].itemNo;
            headerTable[i].itemNo=headerTable[minidx].itemNo;
            headerTable[minidx].itemNo=t;
        }
        
        for(int i=0;i<headerTable.length;i++)
            System.out.print("HEADER TABLE : "+headerTable[i].itemNo+"  ");
     }
                    
                    
        private List<Integer> sortit(List<Integer> itemList){
        List<Integer> list=new ArrayList();
        for(int i=0;i<headerTable.length;i++)
            if(itemList.contains(headerTable[i].itemNo))
                list.add(headerTable[i].itemNo);
        return list;
    }
         
    
        
        
        
    /** Resizes the given array of FP-tree nodes so that its length is 
    increased by one element and new element inserted.
    @param oldArray the given array of FP-tree nodes.
    @param newNode the given node to be added to the FP-tree
    @return The revised array of FP-tree nodes. */
    
    private FPtreeNode[] reallocFPtreeChildRefs(FPtreeNode[] oldArray, 
                FPtreeNode newNode) {
    // No old array
    if (oldArray == null) {
        FPtreeNode[] newArray = {newNode};
        tempIndex = 0;
        return(newArray);
        }
    
    // Otherwise create new array with length one greater than old array
    int oldArrayLength = oldArray.length;
    FPtreeNode[] newArray = new FPtreeNode[oldArrayLength+1];
    
    // Insert new node in correct lexicographic order.
    int index1; 
    for (index1=0;index1 < oldArrayLength;index1++) {
            newArray[index1] = oldArray[index1];
        }
        newArray[index1]=newNode;
    tempIndex=index1;
    // Default
    return(newArray);
    }
        
        
        /* ADD REST OF ITEMSET */
    
    /** Continues adding attributes in current itemset to FP-tree. 
    @param ref the current FP-tree node reference.
    @param backRef the backwards link to the previous node.
    @param place the current index in the given itemset.
    @param itemSet the given itemset.
    @param support the associated support value for the given itemset.
    @param headerRef the link to the appropriate place in the header table. */
    
    private void addRestOfitemSet(FPtreeNode ref, ItemNode backRef, 
                    int place, List<Integer> itemSet, 
                        HeaderTable[] headerRef) {
        
    // Process if more items in item set.
    if (place<itemSet.size()) {
        // Create new Item Prefix Subtree Node
        ItemNode newItemNode = new
                ItemNode(itemSet.get(place),backRef);
        // Create new FP tree node incorporating new Item Prefix Subtree 
        // Node
        FPtreeNode newFPtreeNode = new FPtreeNode(newItemNode);
        // Add link from header table
        addRefToHT(itemSet.get(place),newItemNode,headerRef);
        ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
        // Add into FP tree
        addRestOfitemSet(ref.childRefs[tempIndex],newItemNode,place+1,
                            itemSet,headerRef);
        }
    }

    
        private void addToFPtree(FPtreeNode ref, int place, List<Integer> itemSet, 
                    HeaderTable[] headerRef) {  
    if (place < itemSet.size()) {
        if (!addToFPtree1(ref,place,itemSet,headerRef)) 
                addToFPtree2(ref,place,itemSet,headerRef);
        }
    }
   
    private boolean addToFPtree1(FPtreeNode ref, int place, List<Integer> itemSet,HeaderTable[] headerRef) {
    if (ref.childRefs != null) {
        for (int index=0;index<ref.childRefs.length;index++) {
            if (itemSet.get(place) == ref.childRefs[index].node.itemNo) {
                ref.childRefs[index].node.itemCount++;
            addToFPtree(ref.childRefs[index],place+1,itemSet,
                    headerRef);
            return(true);
                }
                }
        }   
    return(false);
    }
    
    
    private void addToFPtree2(FPtreeNode ref, int place, List<Integer> itemSet,HeaderTable[] headerRef) {   

    ItemNode newItemNode = new 
                ItemNode(itemSet.get(place),ref.node);
    FPtreeNode newFPtreeNode = new FPtreeNode(newItemNode);
    addRefToHT(itemSet.get(place),newItemNode,headerRef);
    // Add into FP tree
    ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
    // Proceed down branch with rest of itemSet
    addRestOfitemSet(ref.childRefs[tempIndex],newItemNode,place+1,itemSet,headerRef);
    }
      

     private void addRefToHT(int col,ItemNode newNode,HeaderTable[] headerRef) {
        ItemNode temp;
    for (int index=0;index<headerRef.length;index++) {
        if (col == headerRef[index].itemNo) {
            temp = headerRef[index].nodeLink;
        headerRef[index].nodeLink = newNode;
        newNode.nodeLink = temp;
        break;
        }
        }   
        }
     
     
            
    /*------------------------------------------------------------------ */
    /*                                                                   */
    /*                           OUTPUT METHODS                          */
    /*                                                                   */
    /*------------------------------------------------------------------ */

    
        
    /* OUTPUT FP TREE */
    
    /** Commences process of outputting FP-tree to screen. */
    
    public void outputFPtree() {
        System.out.println("\n\nFP TREE");
    outputFPtreeNode1();
        System.out.println();
    }
    
    /** Commences process of outputting a given branch of an FP-tree to the
    screen. 
    @param ref the reference to the given FP-tree branch. */
    
    private void outputFPtreeNode(FPtreeNode ref) {
        System.out.println("LOCAL FP TREE");
    outputFPtreeNode2(ref.childRefs,"");
        System.out.println();
    }

    /** Continues process of outputting FP-tree to screen. */
        
    private void outputFPtreeNode1() {
    outputFPtreeNode2(root.childRefs,"");
        }

    /** Outputs a given level in an FP-tree to the screen.
    @param ref the reference to the given FP-tree level.
    @param nodeID the root string for the node ID. */
        
    private void outputFPtreeNode2(FPtreeNode ref[],String nodeID) {
        if (ref == null) return;
    
    // Otherwise process
    
        for (int index=0;index<ref.length;index++) {
        System.out.print("(" + nodeID + (index+1) + ") ");
        outputItemPrefixSubtreeNode(ref[index].node);
        outputFPtreeNode2(ref[index].childRefs,nodeID+(index+1)+".");
        }
    }
        
    /* OUTPUT ITEM PREFIX SUB-TREE NODE */
    
    /** Outputs the given prefix sub tree node. 
    @param ref the reference to the given node. */
    
    public void outputItemPrefixSubtreeNode(ItemNode ref) {
        System.out.print(ref.itemNo + ":" + ref.itemCount);
    if (ref.nodeLink != null) {
        System.out.println(" (ref to " + 
                             ref.nodeLink.itemNo + ":" +
                                             ref.nodeLink.itemCount + ")");
        }   
    else System.out.println(" (ref to null)");
    }

     
     
     
     

    public static void main(String[] args) throws FileNotFoundException, IOException {
        int Tlim=9,Ilim=5,minsupport=2;
        File f=new File("dataset.txt");
        Scanner s=new Scanner(System.in);       
       // System.out.println("Enter no of transactions");
        //Tlim=s.nextInt();
       // System.out.println("Enter no of items");
       // Ilim=s.nextInt();
        //Initialisation Completed

       FPtree fp=new FPtree(Tlim,Ilim,minsupport,f);
         
       // a.CreateRandomDataset();//write a random dataset into file
        fp.ReadDatasetFile();//store transaction details i^nto 2-D array "DatasetTable "
        fp.makeHeader();
        for(int i=0;i<Tlim;i++){
            List<Integer> l=new ArrayList();
            l=fp.sortit(fp.DataArray.get(i));
            System.out.println("Adding " +l);
            fp.addToFPtree(fp.root, 0,l,fp.headerTable);
            fp.outputFPtree();
        }
    
        
    }   
}
