package com.ginko.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPathException;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
 
public class Dom4jParser  { 
	
      public  static void Parse(String targetXml) {
    	  
          SAXReader reader = new SAXReader(); 
          String schemType = "";          
          
          try { 
        	  
              Document doc = reader.read(new StringReader(targetXml)); 
              Element root = doc.getRootElement();               
              System.out.println("Root Node is "+root.getName()); 
              
              List sqlElement=root.elements(); 
              Iterator it = sqlElement.iterator(); 
              while(it.hasNext()) 
              { 
                Element e = (Element)it.next(); 
                if (e.getName().equals("category")) 
                { 
                    if (e.attribute("schme")!=null) 
                    { 
                        schemType = e.attribute("type").getValue(); 
                    } 
                    else 
                    { 
                        System.out.println(schemType+" is "+ 
                            e.attribute(schemType).getValue()); 
                    } 
                } 
                //System.out.println("Element Name "+e.getName()); 
              } 
          }  
          catch(DocumentException e) 
          { 
            System.out.println("not well-formed data");   
          }           
      } 
      
      public  static Element getRootElement(String targetXml)
      {
    	  SAXReader reader = new SAXReader();
          Element root = null;
          try {
              Document doc = reader.read(new StringReader(targetXml));
              root = doc.getRootElement();
          }catch(DocumentException e){

              e.printStackTrace();
	      }

	      return root;
      }
      
      public static  Element getElement(Element node, String namespace) 
      { 
    	  Element rtvEle = null; 
    	  
    	  try{
    		  List ElementList = null;    	 
        	  String nodeList[] = namespace.split("/",2); 
              ElementList = node.elements(); 
              Iterator it = ElementList.iterator(); 
             
              while(it.hasNext()) 
              { 
                  Element e = (Element)it.next(); 
                  if (e.getName().equals(nodeList[0])) 
                  { 
                      // 더이상 탐험할 노드가 없다면 리턴 
                      if (nodeList.length == 1) 
                      { 
                          return e; 
                      } 
                      rtvEle=getElement(e, nodeList[1]); 
                  } 
              } 
             
    	  }
    	  catch (XPathException e){
    		  e.printStackTrace();   
    	  }
    	  
    	  return rtvEle; 
    	 
      } 

      public static void parseXmlByXpath(String filePath){
    	  
    	  SAXReader reader = new SAXReader();
    	  
    	  try{
    		  
    		  Document doc = reader.read(Dom4jParser.class.getClassLoader().getResourceAsStream(filePath));
    		 
    		  List objList = doc.selectNodes("//object");
    		  
    		 // List objList = doc.selectNodes("objects/object");
    		  
    		  //List objList = doc.selectNodes("//author/@id");
    		  
    		  for (int i=0; i<objList.size(); i++){
    			  Element e = (Element)objList.get(i);
    			  System.out.println(e.elementText("name"));
    		  }
    		  
    	  } catch(DocumentException e){ 
          
          e.printStackTrace();   
    	  }       
      }
      
      public  static List getAllElementsByXpath(String targetXml, String sortXPath){    	  
    	 
    	  SAXReader reader = new SAXReader(); 
    	 List childElements = null;	
    	  try { 
              Document doc = reader.read(new StringReader(targetXml)); 
              childElements = doc.selectNodes(sortXPath);              
          }catch(DocumentException e){ 
           
              e.printStackTrace();   
	      }   	
          return childElements;
    	 
      }
      
      public  static List getChildlElementsByName(String targetXml, String name){    	  
     	 
    	 List childs = null; 
    	 Element root = getRootElement(targetXml);
    	 if (root != null){
    		 Element element = getElement(root, name);
    		 if (element != null) childs = element.elements();
    	 }
    	 
          return childs;
    	 
      }
      
      public  static List getAllElementsByName(String targetXml, String name){   
    	  
      	 List elementList = null;
     	 Element root = getRootElement(targetXml);
     	 if (root != null ) elementList = root.elements(name);     	 
         return elementList;
     	 
       }   

	public static ArrayList<Element> getAllElementsByAttribute(String targetXml, String attriValue) {
		// TODO Auto-generated method stub
		int index = -1;
		ArrayList<Element> resultList = null;
		resultList = new ArrayList<Element>();
		
		Element root = getRootElement(targetXml);
		
		// iterate through child elements of root		
		for (Iterator i = root.elementIterator(); i.hasNext();){
			
			Element element = (Element) i.next();					
			String attri = element.attributeValue("type");
			if (attri != null){
				if (attri.compareTo(attriValue) == 0){				
					index++;
					resultList.add(index, element);				
				}
			}			
		}		
		return resultList;
	}	
} 
