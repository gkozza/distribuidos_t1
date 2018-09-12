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


import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import java.time.Instant;
import java.util.Date;



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

    public static void menu(){
        System.out.println("///////////");
        System.out.println("1 - Recurso 1");
        System.out.println("2 - Recurso 2");
        System.out.println("3 - Nao fazer nada");
        System.out.println("4 - sair");
        System.out.println("///////////");
       
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
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
                

		// args give message contents and destination multicast group (e.g. "228.5.6.7")
		MulticastSocket s =null;
		try {			
                        InetAddress group = InetAddress.getByName("228.5.6.7");

                        String estado = "REALEASED";                        
                        Boolean achou = false;

                        String estado_rec1 = "RELEASED";                        
                        String estado_rec2 = "RELEASED";
                        
                        Boolean sair = false;                        
                        Boolean inicio = false;
                        Boolean rec1_uso = false;
                        ArrayList<String> fila_rc1 = new ArrayList<>();
                        ArrayList<String> fila_rc2 = new ArrayList<>();
                        System.out.println("Bem-vindo, por favor informe seu nome para login");

                        Scanner reader = new Scanner(System.in); 
                        nome = reader.next();
                        List<String> online = new ArrayList<String>();
                        

                        

                //create HashMap para troca de mensagem
                // 1 - Entrar Chat
                // 2 - Retornar informações para novos usuários
                // 3 - Enviar Mensagem
                // 4 - Sair do Chat
                        HashMap<Integer, HashMap<String, PublicKey>> mapPlayer = new HashMap<Integer,HashMap<String,PublicKey>>();
                        

                //hashMap para controle da RC de cada Recurso, KEY, ESTADO, TEMPO
                //1 - RELEASED
                //2 - WANTED
                //3 - HELD
                        
                        HashMap<String, List<Integer>> rc_rec1 = new HashMap<>();
                        
                        HashMap<String, List<Integer>> rc_rec2 = new HashMap<>();
                        
                        

                        
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
//                                if(obj instanceof HashMap){                           
//                                    HashMap map = (HashMap) obj;
//                                        if(map.containsKey(1)){
//                                           // Receber informações de alguém que entrou no servidor  
//                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(1);
//                                            String client_name = (String)parseHashMsg(1,map2);
//                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
//                                            nome_chave.put(client_name, keyPublic);
//                                            System.out.println(client_name + " entrou");
//                                            //prepara informações para enviar as minhas informações para o grupo para todos terem
//                                            mapPlayer.put(2, getMyInfo());
//                                            online.add(client_name);
//                                            byte[] msgVolta = serialize(mapPlayer);
//                                            s.send(getDatagram(msgVolta, group));
//                                        }
//                                        
//                                        if(map.containsKey(2)){
//                                            System.out.println("VOlta: ");
//
//                                //System.out.println("MAP: " + obj.toString());
                                
                                if(obj instanceof HashMap){                           
                                    HashMap map = (HashMap) obj;
                                        if(map.containsKey(6)){
                                            System.out.println("Recurso 2");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(6);
                                            String client_name = (String)parseHashMsg(1,map2);       
                                            
                                            Date date= new Date();
                                            int indicacao_tempo_rc2 = (int) date.getTime();
                                            rc_rec2.put(client_name, Arrays.asList(2,indicacao_tempo_rc2));
                                            int h = 0;
                                            for(Map.Entry<String, List<Integer>> entry: rc_rec2.entrySet()) {
                                                if(!client_name.equals(entry.getKey())){
                                                    if(entry.getValue().get(h).equals(3)){
                                                        System.out.println("O recurso 2 ja esta sendo usado por " + entry.getKey());
                                                        rec1_uso = true;
                                                    } 
                                                }
                                                h++;
                                            }
                                            if(!rec1_uso){
                                                rc_rec2.put(client_name, Arrays.asList(3, indicacao_tempo_rc2));                                                
                                            }else{
                                                fila_rc2.add(client_name);
                                            }                                                                                        
                                            mapPlayer.remove(6);
                                        }
                                        
                                        if(map.containsKey(5)){
                                            System.out.println("Recurso 1");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(5);
                                            String client_name = (String)parseHashMsg(1,map2);       
                                            
                                            Date date= new Date();
                                            int indicacao_tempo_rc1 = (int) date.getTime();
                                            rc_rec1.put(client_name, Arrays.asList(2,indicacao_tempo_rc1));
                                            int h = 0;
                                            for(Map.Entry<String, List<Integer>> entry: rc_rec1.entrySet()) {
                                                if(!client_name.equals(entry.getKey())){
                                                    if(entry.getValue().get(h).equals(3)){
                                                        System.out.println("O recurso 1 ja esta sendo usado por " + entry.getKey());
                                                        rec1_uso = true;
                                                    } 
                                                }
                                                h++;
                                            }
                                            if(!rec1_uso){
                                                rc_rec1.put(client_name, Arrays.asList(3, indicacao_tempo_rc1));
                                                System.out.println(client_name + " esta usando o recurso 1");
                                            }else{
                                                fila_rc1.add(client_name);
                                            }                                                                                        
                                            mapPlayer.remove(5);
                                        }
                                        
                                        if(map.containsKey(4)){
                                            System.out.println("Sair");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(4);
                                            String client_name = (String)parseHashMsg(1,map2);                                            
                                            for(int o = 0; o < online.size(); o++){
                                                if(online.get(o).equals(client_name)){                                        
                                                    System.out.println("deletou o "+ online.get(o));
                                                    online.remove(o);
                                                    rc_rec1.remove(client_name);
                                                    rc_rec2.remove(client_name);
                                                }
                                            }
                                            if(client_name.equals(nome)){
                                                s.leaveGroup(group);
                                                sair = true;
                                            }
                                            System.out.println(client_name + " saiu");    
                                                                                                    
                                        }
                                        
                                        
                                        else if(map.containsKey(3)){
                                            System.out.println("Descriptografar");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(3);
                                            String msgCrypto = (String)parseHashMsg(1,map2);
                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
                                            byte[] msgCripto = DatatypeConverter.parseBase64Binary(msgCrypto);
                                            
                                            
                                            byte[] message= key.decriptarComChavePublica(msgCripto, keyPublic);
                                                                                
                                            
                                            
                                            System.out.println("\n");
                                            System.out.println("Mensagem:: " + new String(message));
                                            
                                           
                                        
                                        }else if(map.containsKey(2)){
                                            System.out.println("Volta: ");
                                            HashMap<String,PublicKey> map2 = (HashMap<String,PublicKey>) map.get(2);
                                            String client_name = (String)parseHashMsg(1,map2);
                                            PublicKey keyPublic = (PublicKey)parseHashMsg(2,map2);
                                            
                                          // verifica a lista das pessoas online, caso a pessoa ainda não esteja na lista, adiciona ela.


                                                for(int h = 0; h < online.size(); h++){

                                                    
                                                    if(online.get(h).equals(client_name)){
                                                        achou = true;
                                                    }
                                                }
                                                if(!achou){    
                                                    online.add(client_name);                                                                 
                                                    nome_chave.put(client_name,keyPublic); 
                                                }
                                         
                                
                                            mapPlayer.remove(2);
                                        }
                                        
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
                                            rc_rec1.put(client_name, Arrays.asList(1,0));
                                            rc_rec2.put(client_name, Arrays.asList(1,0));
                                            byte[] msgVolta = serialize(mapPlayer);
                                            s.send(getDatagram(msgVolta, group));
                                            
                                        }                                                                                                                                                                                                                                                                                                                                                                        
                                    }
                                
                                
                                if(online.size() == 3){
                                    inicio = true;
                                }
                                
                                if(online.isEmpty()){
                                    sair = true;
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
                        
                            if(inicio && !sair){
                                    cleanBuffer(buffer);                                    
                                    menu();                                    
                                    System.out.println("Escolha a opção mensagem: ");             
                                    Scanner reader2 = new Scanner(System.in); 
                                    opcao = reader2.nextInt();
                                    switch(opcao){
                                        case 1:
                                            System.out.println("case 1 ");
                                            mapPlayer.put(5, getMyInfo());
                                            byte[] msgRec1 = serialize(mapPlayer);
                                            s.send(getDatagram(msgRec1, group));                                                                                                                                
                                        case 2:
                                            mapPlayer.put(6, getMyInfo());
                                            byte[] msgRec2 = serialize(mapPlayer);
                                            s.send(getDatagram(msgRec2, group));                                                                                                                                
                                        case 3:
                                            break;
                                        case 4:
                                            mapPlayer.put(4, getMyInfo());
                                            byte[] msgSaida = serialize(mapPlayer);
                                            s.send(getDatagram(msgSaida, group));                                                                                                                                
                                    }                                                                       
                                
                            }
                            System.out.println("Estao online: \n");
                            for(int h = 0; h<online.size(); h++){
                                System.out.println(online.get(h));
                            }
                            System.out.println("----------");   
                                
                            
                        }while(!sair);

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


