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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author gustavosantos
 */
public class MulticastAp {

    public static HashMap<String, byte[]> nome_chave = new HashMap<String, byte[]>();
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
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
                

		// args give message contents and destination multicast group (e.g. "228.5.6.7")
		MulticastSocket s =null;
		try {			
                        InetAddress group = InetAddress.getByName("228.5.6.7");
                        String estado = "REALEASED";
                        String nome;
                        Boolean achou = false;
                        Scanner reader = new Scanner(System.in); 
                        nome = reader.next();                        
                        
                        List<String> online = new ArrayList<String>();
                        //List<String, byte[]> nome_chave = new ArrayList<String, byte[]>();
                        

                        List<byte[]> chaves_publicas = new ArrayList<byte[]>();
			
                        s = new MulticastSocket(6789);
                        s.joinGroup(group);
                        //online.add(nome);                                           
                        
                        KeyPairGen key = new KeyPairGen();                           
                        byte[] pub_key  = key.getPub().getEncoded();
                        key.writeToFile("KeyPair/publicKey" + nome, pub_key);
                        byte[] priv_key =  key.getPriv().getEncoded();
                        key.writeToFile("KeyPair/privateKey" + nome, priv_key);
                                                                     
                        String mensagem = nome + "id;" + pub_key.toString();
                        
                                                
 			byte [] m = mensagem.getBytes();
			DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
			s.send(messageOut);	
			byte[] buffer = new byte[2048];                                                                                 
                        
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
                            
                            Object obj = new Object();                                                        
                            
                            try{
                                obj = deserialize(conteudo.getBytes());                                
                                System.out.println("piroca");
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
                            
                            if(online.size() == 3){
                                                                    
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
                            }*/
                            cleanBuffer(buffer);
                            
                            
                            
                        } while(opcao != 4);
                                                		
		}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		}catch (IOException e){System.out.println("IO: " + e.getMessage());
		}finally {if(s != null) s.close();}
	}		      	

    
}
