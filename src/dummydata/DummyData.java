/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dummydata;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author akrv
 */
public class DummyData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random rand = new Random();
        String propHostname = GetHostname();
//        System.out.println(propHostname);
        String[] dataSet;
        String timeStamp, nodeMacAddress, snifferMacAddress, str;
        //hazelcast parameters
        Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, String> mapSensors = instance.getMap("mapSensors");
        IMap<String, String> snifferMap = instance.getMap("snifferMap");
        
        // to make mac address common for all nodes.
        Map<String, String> listNodeMacAddress = instance.getMap("listNodeMacAddress");
        Map<String, String> listSnifferMacAddress = instance.getMap("listSnifferMacAddress");
        if (listNodeMacAddress.isEmpty()){
        listNodeMacAddress.putAll(GenerateNodeMacAddress());
        listSnifferMacAddress.putAll(GenerateNodeMacAddress());        
        }
        Integer keySniffer = Integer.parseInt(Collections.max(listSnifferMacAddress.keySet()));
        Integer keySensor = Integer.parseInt(Collections.max(listNodeMacAddress.keySet()));
//        Map<String, String> macAddr = instance.getMap("macAddr");
//        Map<String, String> gatewayNames = instance.getMap("gatewayNames");
        while (true){

        dataSet = GenerateData();
        nodeMacAddress = listNodeMacAddress.get(""+rand.nextInt(keySensor));
        dataSet[5] = propHostname;
        dataSet[6] = nodeMacAddress;
        str = Arrays.toString(dataSet);
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        mapSensors.put(timeStamp, str);
        System.out.println("sensor: "+timeStamp+str);
        dataSet = GenerateData();
        snifferMacAddress = listSnifferMacAddress.get(""+rand.nextInt(keySniffer));
        dataSet[5] = propHostname;
        dataSet[6] = nodeMacAddress;
        dataSet[7] = snifferMacAddress;
        str = Arrays.toString(dataSet);
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        System.out.println("sniffer: "+timeStamp+str);
        
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException ie) {
            System.out.println(ie);
        }
        } 
    }

    private static String[] GenerateData() {
        Random rand = new Random();
        String[] value = new String[8];
        for (int i =0;i<5;i++){
        int a = rand.nextInt(50) + 1;
        value[i] = a+"";
        }
        return value;
    }
    private static String GetHostname() {
        // try environment properties.    
        String host = System.getenv("COMPUTERNAME");
        if (host != null)
            return host;
        host = System.getenv("HOSTNAME");
        if (host != null)
            return host;
        // undetermined.
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(DummyData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return host;
    }    

//    not used here, MacAddr4Mongo generates this all
//    read from mongoDB
    private static Map GenerateNodeMacAddress(){
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        MongoDatabase db = mongoClient.getDatabase("translator-dev");
        MongoCollection<Document> collection = db.getCollection("macAddrs");
        FindIterable<Document> iterable = collection.find();
        Document document = iterable.first();
        document.remove("_id");
        Map<String, String> documentMap = (Map) document;
        return documentMap;
    }
    
    // not used here
    private static String randomMACAddress(){
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);
        macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated
        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){
            if(sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
