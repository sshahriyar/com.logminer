/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logminer.reader.adlogs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Syed Shariyar Murtaza
 */
public class NVDNodes {
    public String cveId;
    public String summary;
    public String publishedTime;
    public String cwe;
    public String cvssScore;
    public String cvssAccessVector;
    public String cvssAccessComplexity;
    public String cvssAuthentication;
    public String cvssConfidentialityImpact;
    public String cvssIntegrityImpact;
    public String cvssAvailabilityImpact;
    public String cvssGeneratedOnDatetime;
    public List<String> softwareProduct;

    public NVDNodes() {
        softwareProduct=new ArrayList<>();
    }
    
    
    public static String getCreateVulnerabilityTableQuery(){
        String qry="CREATE TABLE IF NOT EXISTS vulnerabilitylist (id INTEGER AUTO_INCREMENT NOT NULL ,"
                + "cveid VARCHAR(50) , "
                + "published_time VARCHAR (100),"
                + "cwe VARCHAR (50),"
                + "summary VARCHAR(5000),"
                + "cvss_score VARCHAR(50),"
                + "cvss_access_vector VARCHAR(50),"
                + "cvss_access_complexity VARCHAR(50),"
                + "cvss_confidentiality_impact VARCHAR(50),"
                + "cvss_availability_impact VARCHAR(50),"
                + "cvss_generated_time VARCHAR(100),"
                + "PRIMARY KEY (id));";
        return qry;
    }
    
    /**
     * 
     * @return 
     */
    public static String getCreateProductTableQuery(){
        String qry="CREATE TABLE IF NOT EXISTS vulnerableproducts("
                + "id INTEGER AUTO_INCREMENT NOT NULL,"
                + "cveid VARCHAR(50),"
                +"product VARCHAR(1000),"
                + "PRIMARY KEY (id));";
        return qry;
                
    }
    /**
     * 
     * @return 
     */
   public String getVulnerabilityInsertQuery(){
       if (summary.contains("\\")){
           summary=summary.replace("\\", "\\\\");
       }
               
        String qry="INSERT INTO vulnerabilitylist"
                + "(cveid, published_time,cwe,summary,cvss_score,cvss_access_vector,cvss_access_complexity,"
                + "cvss_confidentiality_impact, cvss_availability_impact,cvss_generated_time)"
                + "VALUES ('"
                + cveId+"', '"+publishedTime+ "', '"+cwe+"', \""+summary.replace("\"", "\\\"")+"\", '"+cvssScore+ "', '"+ cvssAccessVector+ "', '"+cvssAccessComplexity+"', '"
                + cvssConfidentialityImpact+ "', '"+cvssAvailabilityImpact+ "', '"+cvssGeneratedOnDatetime+ "')";
        
        return qry;
    }
   /**
    * 
    * @return 
    */
   public String getProductInsertQuery(){
       String qry="INSERT INTO vulnerableproducts (cveid,product) VALUES";
       String prodList="";
       int cnt=1; int prodLength=softwareProduct.size();
       for (String prod: softwareProduct){
           if (cnt <prodLength)
             prodList += " ('"+cveId+"', '"+prod+"'),";
           else
             prodList += " ('"+cveId+"', '"+prod+"');";
           cnt++;
       }
       
       if (prodList.isEmpty())
           return null;
       else
        return (qry+prodList);
   }
    
}
