/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicastap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author gustavosantos
 */
public class MulticastAp {
    public static String nome = null;
    public static PublicKey myKeyPublic = null;
    public static PrivateKey myKeyPrivate = null;
    public static HashMap<String, PublicKey> nome_chave = new HashMap<String, PublicKey>();
    public static int opcao;
    
    
    public static void cleanBuffer(byte[] buffer){
        for(int i = 0; i < buffer.length; i++){
            buffer[i] = 0;
        }
    }
    
    public static byte[] serialize(Object obj) throws IOException{
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }
    
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);       
        return o.readObject();
    }
    
    public static Object parseHashMsg(int opc, HashMap<String, PublicKey> map){
        String client_name = null;
        PublicKey keyPublic = null;
        for (Entry entry : map.entrySet()){
            client_name = (String) entry.getKey();
            keyPublic = (PublicKey) entry.getValue();
        }
        if(opc == 1 ){
            return client_name;
        }else{
            return keyPublic;
        }  
    }
    
    public static HashMap<String,PublicKey> getMyInfo(){
        HashMap<String, PublicKey> myInfo = new HashMap<String, PublicKey>();
        myInfo.put(nome, myKeyPublic);
        return myInfo;
    }
    
    
    
    public static KeyPairGen generateKey(){
        KeyPairGen key = new KeyPairGen();                           
        try {
            byte[] pub_key  = key.getPub().getEncoded();
            key.writeToFile("KeyPair/publicKey" + nome, pub_key);
            byte[] priv_key =  key.getPriv().getEncoded();
            key.writeToFile("KeyPair/privateKey" + nome, priv_key);
            myKeyPrivate = key.getPriv();
            myKeyPublic = key.getPub();
        } catch (IOException ex) {
            Logger.getLogger(MulticastAp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;                
    }
    
    public static DatagramPacket getDatagram(byte[] msg, InetAddress group){
         DatagramPacket messageOut = new DatagramPacket(msg, msg.length, group, 6789);
        return messageOut;
    } 
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
                

		// args give message contents and destination multicast group (e.g. "228.5.6.7")
		MulticastSocket s =null;
		try {			
                        InetAddress group = InetAddress.getByName("228.5.6.7");
                        String estado = "REALEASED";                        
                        Boolean achou = false;
                        Scanner reader = new Scanner(System.in); 
                        nome = reader.next();
                        List<String> online = new ArrayList<String>();
                        

                //create HashMap para troca de mensagem
                // 1 - Entrar Chat
                // 2 - Retornar informações para novos usuários
                // 3 - Enviar Mensagem
                // 4 - Sair do Chat
                        HashMap<Integer, HashMap<String, PublicKey>> mapPlayer = new HashMap<Integer,HashMap<String,PublicKey>>();
                        
                        
                        // Entrar no grupo multicast
                        s = new MulticastSocket(6789);
                        s.joinGroup(group);
                        
                        KeyPairGen key = generateKey();
                        mapPlayer.put(1, getMyInfo());
                        
                        byte[]msgHash = serialize(mapPlayer);
                        s.send(getDatagram(msgHash, group));	
			byte[] buffer = new byte[2048];
                        mapPlayer.remove(1);
                        do{
                            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                            s.receive(messageIn);
                            
                            //System.out.println(new String(messageIn.getData()) + " entrou");                            
                            String conteudo = new String(messageIn.getData());                                                                                   
                            Object obj = null;                                                        
                            
                            try{
                                obj = deserialize(messageIn.getData());
                                System.out.println("MAP: " + obj.toString());
                                if(obj instanceof HashMap){                           
                                    HashMap map = (HashMap) obj;
                                        if(map.containsKey(1)){
                                           // Receber informações de alguém que entrou no servidor  
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(1);
                                            String client_name = (String)parseHashMsg(1,map2);
                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
                                            nome_chave.put(client_name, keyPublic);
                                            System.out.println(client_name + " entrou");
                                            //prepara informações para enviar as minhas informações para o grupo para todos terem
                                            mapPlayer.put(2, getMyInfo());
                                            online.add(client_name);
                                            byte[] msgVolta = serialize(mapPlayer);
                                            s.send(getDatagram(msgVolta, group));
                                        }
                                        
                                        if(map.containsKey(2)){
                                            System.out.println("VOlta: ");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(2);
                                            String client_name = (String)parseHashMsg(1,map2);
                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
                                            
                                          // verifica a lista das pessoas online, caso a pessoa ainda não esteja na lista, adiciona ela.
                                                for(int h = 0; h<online.size(); h++){
                                                    
                                                    if(online.get(h).equals(client_name)){
                                                        achou = true;
                                                    }
                                                }
                                                if(!achou){    
                                                    online.add(client_name);                                                                 
                                                    nome_chave.put(client_name,keyPublic); 
                                                }
                                        } 
                                        if(map.containsKey(3)){
                                            System.out.println("Descriptografar");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(3);
                                            String msgCrypto = (String)parseHashMsg(1,map2);
                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
                                            byte[] msgCripto = DatatypeConverter.parseBase64Binary(msgCrypto);
                                            
                                            
                                            byte[] message= key.decriptarComChavePublica(msgCripto, keyPublic);
                                                                                
                                            
                                            
                                            System.out.println("\n");
                                            System.out.println("Mensagem:: " + new String(message));
                                            
                                           
                                        }
                                }else{
                                            System.out.println("nãodeu");
                                        
                                }
                                System.out.println("\n\n\n\n");
                                //System.out.println("Online: ");
                                //System.out.println("MAP : " +nome_chave.toString());
                            }
                            catch(IOException | ClassNotFoundException ex) {
                                System.out.println("se fude" + ex);
                            }
                            
                            
                            if(nome.contains("juca")){
                                    System.out.println("Aguradndao mensagem: ");             
                                    Scanner reader2 = new Scanner(System.in); 
                                    String chat = reader.next();   
                                    chat = chat;
                                    
                                    byte[] chat_byte = key.encriptarComChavePrivada(chat.getBytes(), key.getPriv());
                                    byte[] mensagem = key.decriptarComChavePublica(chat_byte, key.getPub());
                                
                                    HashMap<String, PublicKey> hashMsgCrypto = new HashMap<String, PublicKey>();
                                    
                                    
                                    String msgCripto = DatatypeConverter.printBase64Binary(chat_byte);
                                
                                    byte[] msgByte = DatatypeConverter.parseBase64Binary(msgCripto);
                                    
                                
                                    hashMsgCrypto.put(msgCripto, myKeyPublic);
                                    
                                    
                                    mapPlayer.put(3, hashMsgCrypto);
                                    byte [] msg = serialize(mapPlayer);
                                    
                                    s.send(getDatagram(msg, group));
                                
                            }
                        }while(opcao != 4);
                        /*String mensagem = nome + "id;" + pub_key.toString();
                        
                                                
 			byte [] m = mensagem.getBytes();
			
                        
                        int k = 0;
                        String[] split;
                        String[] split2;
                        
                        do{                                                            
                            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                            s.receive(messageIn);
                            
                            //System.out.println(new String(messageIn.getData()) + " entrou");                            
                            String conteudo = new String(messageIn.getData());                                                                                   
                            
                            if(conteudo.contains("id")){
                                split = conteudo.split(";");
                                String chave = split[1];
                                byte[] chave_ = chave.getBytes();
                                String nome_ = split[0].replace("id","");                                
                                online.add(nome_);                                                                 
                                nome_chave.put(nome_, chave_);
                                System.out.println(nome_ + " entrou");                                                                                               
                                String mensagem_de_volta = nome + "volta;" + pub_key.toString();                                                                        
                                byte [] m2 = mensagem_de_volta.getBytes();
                                messageOut = new DatagramPacket(m2, m2.length, group, 6789);
                                s.send(messageOut);	                                
                            }
                            
                            if(conteudo.contains("volta")){
                                System.out.println("entrou no volta");
                                split2 = conteudo.split(";");
                                String chavep = split2[1];
                                byte[] key_ = chavep.getBytes();
                                String nome_ = split2[0].replace("volta","");
                                
                                for(int h = 0; h<online.size(); h++){
                                        if(online.get(h).equals(nome_)){
                                            achou = true;
                                        }
                                }
                                if(!achou){    
                                    System.out.println("entrou aqui no nao achou");
                                    online.add(nome_);                                                                 
                                    nome_chave.put(nome_, key_); 
                                }
                                                           }
                            
                            Object obj = null;                                                        
                            
                            try{
                                obj = deserialize(messageIn.getData());                                
                                System.out.println("piroca");
                                if(obj instanceof HashMap){
                                    System.out.println("DEUBOA");
                                    HashMap map = (HashMap) obj;
                                        if(map.containsKey("PublicKey")){
                                            System.out.println("é chavepublica");
                                        }else{
                                            System.out.println("nãodeu");
                                        }
                                    }
                                
                            }
                            catch(IOException | ClassNotFoundException ex) {
                                System.out.println("se fude" + ex);
                            }
                             
                            
                            
                                
                            
                            
                            if(conteudo.contains("sair")){
                                split2 = conteudo.split(";");
                                String id = split2[1];                                
                                online.remove(id);               
                                s.leaveGroup(group);
                                System.out.println(id + " saiu");
                                opcao = 4;
                            //for(Map.Entry<String, byte[]> entry: nome_chave.entrySet()) {
                              //      System.out.println("print volta \n");
                                //    System.out.println(entry.getKey() + " : " + entry.getValue());                                    
                               // }                            
                            }
                            
                            if(nome.contains("juca")){
                                
                                
                                                       System.out.println("Aguradndao mensagem: ");             
                                    Scanner reader2 = new Scanner(System.in); 
                                    String chat = reader.next();   
                                    chat = chat;
                                    HashMap<byte[], PublicKey> msg_chave = new HashMap<byte[], PublicKey>();                                    
                                    byte[] chat_byte = key.encriptarComChavePrivada(chat.getBytes(), key.getPriv());                                    
                                    msg_chave.put(chat_byte, key.getPub());                                    
                                    byte [] msg = serialize(msg_chave);                                                                        
                                    DatagramPacket messageOutSaida = new DatagramPacket(msg, msg.length, group, 6789);
                                    s.send(messageOutSaida);
                            }
                                                        
                                  System.out.println("Estao online: \n");
                                  for(int h = 0; h<online.size(); h++){
                                        System.out.println(online.get(h));
                                  }
                            
                                    				                                    
                                   
                             
                            
                            
                            /*if(k == 2 ){
                                Scanner reader2 = new Scanner(System.in); 
                                String chat = reader.next();
                                chat = chat + ";" + key.getPub().toString();
                                byte [] msg = chat.getBytes();
                                byte[] chat_msg = key.encriptarComChavePrivada(msg, key.getPriv());
                                byte[] descipt = key.decriptarComChavePublica(chat_msg, key.getPub());
                                String tomate = new String(descipt);
                                System.out.println(tomate);
                                messageOut = new DatagramPacket(chat_msg, chat_msg.length, group, 6789);
                                s.send(messageOut);	
                            }
                            cleanBuffer(buffer);
                            
                           
                            
                        } while(opcao != 4);
                             */                   		
		}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		}catch (IOException e){System.out.println("IO: " + e.getMessage());
		}finally {if(s != null) s.close();}
	}		      	

    
}
