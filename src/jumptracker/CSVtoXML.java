/*
 * Copyright (C) 2021 Michael Collins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jumptracker;

/**
 *
 * @author DissentingPotato
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CSVtoXML {
    // Protected Properties
    protected DocumentBuilderFactory domFactory = null;
protected DocumentBuilder domBuilder = null;

public CSVtoXML() {
    try {
        domFactory = DocumentBuilderFactory.newInstance();
        domBuilder = domFactory.newDocumentBuilder();
    } catch (FactoryConfigurationError exp) {
        System.err.println(exp.toString());
    } catch (ParserConfigurationException exp) {
        System.err.println(exp.toString());
    } catch (Exception exp) {
        System.err.println(exp.toString());
    }

}

public static void clean(String filepath) throws IOException{

        String content = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String curLine;
            while((curLine = br.readLine()) != null){
                content += curLine;
            }

            //Path fileName = Path.of(filepath);
            //String content  = Files.readString(fileName);
            content = content.substring(content.indexOf(">")+1);
            content = 
                    content.substring(0, content.indexOf(">")+1) 
                    + "<version>0.7.9.2</version>"
                    + "<jumper>"
                    + "<jumpername>importedCSV</jumpername>"
                    + "<types>[All, Origin, Perk, Item, Companion, Drawback, Scenario, Other]</types>" 
                    + content.substring(content.indexOf(">")+1, content.indexOf("</jumptracker>"))
                    + "</jumper>"
                    + "</jumptracker>";

            content = content.replaceAll("><", ">\n<");

            PrintWriter writer = new PrintWriter(filepath, "UTF-8");
            writer.print(content);
            writer.close();
        }catch(Exception e){}
}

public static void cleanJump(String filepath) throws IOException{

        String content = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String curLine;
            while((curLine = br.readLine()) != null){
                content += curLine;
            }

            //Path fileName = Path.of(filepath);
            //String content  = Files.readString(fileName);
            content = content.substring(content.indexOf("<jump>"));
            content = content.substring(0, content.indexOf("/jump>")+6);

            content = content.replaceAll("><", ">\n<");

            PrintWriter writer = new PrintWriter(filepath, "UTF-8");
            writer.print(content);
            writer.close();
        }catch(Exception e){}
    
}

public static String[] addElement(String[] line, String addMe){
    int i;
  
    // create a new ArrayList
    List<String> tempList = new ArrayList<String>(Arrays.asList(line));
  
    // Add the new element
    tempList.add(addMe);
  
    // Convert the Arraylist to array
    line = tempList.toArray(line);
  
    // return the array
    return line;
}

    //arguments are as follows
    //csv file to convert, xml file to be saved, type of delimiter to use, in this case it should be ","
    //delimiter was only included for flexibility
    public int convertFile(
            String csvFileName,
            String xmlFileName,
            String delimiter) {

        int rowsCount = -1;
        try {
            Document newDoc = domBuilder.newDocument();
            // <jumptracker></jumptracker> Root element
            // all children will appear within the > < between the tags
            Element rootElement = newDoc.createElement("jumptracker");
            newDoc.appendChild(rootElement);
            // Read csv file
            BufferedReader csvReader;
            csvReader = new BufferedReader(new FileReader(csvFileName));
            //init variables
            int fieldCount = 0;
            String[] csvFields = null;
            ArrayList<String> csvValues = new ArrayList<String>();
            //yes this starts at 0, this is intended
            String[] line = new String[fieldCount]; 

            
            //This is the first runthrough, grabs all the column names
            String curLine = csvReader.readLine();
            if (curLine != null) {
                line = curLine.split(delimiter);
                for(String value : line) {
                    csvValues.add(value);
                }
                fieldCount = line.length;
                if (fieldCount > 0) {
                    csvFields = new String[fieldCount];
                    int i = 0;
                    while (i < line.length) {
                        csvFields[i] = String.valueOf(line[i]);
                        i++;
                    }
                }
            }

            // start creating the elements

            Element jumpElement = newDoc.createElement("jump");
            Element rowElement = null;
            
            while ((curLine = csvReader.readLine()) != null) {
                line = curLine.split(delimiter);
                if (fieldCount > 0) {
                    rowElement = newDoc.createElement("option");
                    int i = 0;
                    //if the line doesn't have a world name
                    //then this assumes it's another option for the previous world, fuck arrays in particular
                    if(String.valueOf(line[0]).length() < 1) {
                        rowElement = newDoc.createElement("option");
                        i = 4;
                        while (i < fieldCount) {
                            try {
                                String curValue = String.valueOf(line[i]);
                                if(i == 4) {
                                    rowElement = newDoc.createElement("option");
                                }
                                if(curValue.equals("") || curValue.equals(null)) {
                                    curValue = " ";
                                    
                                }
                                Element curElement = newDoc.createElement(csvFields[i]);
                                curElement.appendChild(newDoc.createTextNode(curValue));
                                rowElement.appendChild(curElement);
                                i++;
                            } catch (Exception exp) {
                                if(line.length < fieldCount) {
                                    line = addElement(line, " ");
                                }
                            }
                        }
                    //if the line does have a world name, create another jump tag
                    }else {
                        if(jumpElement.hasChildNodes()) {
                            rootElement.appendChild(jumpElement);
                        }
                        jumpElement = newDoc.createElement("jump");
                        while (i <= fieldCount-1) {
                            try {
                                String curValue = String.valueOf(line[i]);
                                if(i == 4) {
                                    rowElement = newDoc.createElement("option");
                                }
                                if(i >= 4) {
                                    Element curElement = newDoc.createElement(csvFields[i]);
                                    if(curValue.equals("") || curValue.equals(null)) {
                                        curValue = " ";
                                        
                                    }
                                    curElement.appendChild(newDoc.createTextNode(curValue));
                                    rowElement.appendChild(curElement);
                                }else {
                                    Element curElement = newDoc.createElement(csvFields[i]);
                                    curElement.appendChild(newDoc.createTextNode(curValue));
                                    jumpElement.appendChild(curElement);
                                }
                                i++;
                            } catch (Exception exp) {
                                if(line.length < fieldCount) {
                                    line = addElement(line, " ");
                                }
                            }
                        }
                    }
                    
                    if(rowElement != null) {
                        jumpElement.appendChild(rowElement);
                    }
                    rootElement.appendChild(jumpElement);
                    rowsCount++;
                }
            }
            csvReader.close();

            // Save the document to the disk file
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();
            Source src = new DOMSource(newDoc);
            Result result = new StreamResult(new File(xmlFileName));
            aTransformer.transform(src, result);
            rowsCount++;

            

        } catch (IOException exp) {
            System.err.println(exp.toString());
        } catch (Exception exp) {
            System.err.println(exp.toString());
        }
        return rowsCount;
        // "XLM Document has been created" + rowsCount;
    }
}
