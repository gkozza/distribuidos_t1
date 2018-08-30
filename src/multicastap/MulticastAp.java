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
    public static void main(String[] args) throws UnknownHostException, IOException {
                

		// args give message contents and destination multicast group (e.g. "228.5.6.7")
		MulticastSocket s =null;
		try {			
                        InetAddress group = InetAddress.getByName("228.5.6.7");
                        String estado = "REALEASED";
                        String nome;
                        Scanner reader = new Scanner(System.in); 
                        nome = reader.next();                        
                        
                        List<String> online = new ArrayList<String>();
              //        List<String, byte[]> nome_chave = new ArrayList<String, byte[]>();
                        

                        List<byte[]> chaves_publicas = new ArrayList<byte[]>();
			
                        s = new MulticastSocket(6789);
                        s.joinGroup(group);
                        online.add(nome);                                           
                        
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
                        while(k < 50){    
                            
                            
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
                                System.out.println(conteudo + " entrou");
                                
                                for(Map.Entry<String, byte[]> entry: nome_chave.entrySet()) {
                                    System.out.println(entry.getKey() + " : " + entry.getValue());                                    
                                }
                                
                                String mensagem_de_volta = nome + "volta;" + pub_key.toString();                                                                        
                                byte [] m2 = mensagem_de_volta.getBytes();
                                messageOut = new DatagramPacket(m2, m2.length, group, 6789);
                                s.send(messageOut);	                                
                            }
                            
                            if(conteudo.contains("volta")){
                                split2 = conteudo.split(";");
                                String chavep = split2[1];
                                byte[] key_ = chavep.getBytes();
                                String nome_ = split2[0].replace("volta","");
                                online.add(nome_);                                 
                                nome_chave.put(nome_, key_);                                                                
                            for(Map.Entry<String, byte[]> entry: nome_chave.entrySet()) {
                                    System.out.println("print volta \n");
                                    System.out.println(entry.getKey() + " : " + entry.getValue());                                    
                                }
                            
                            }                            
                            //Teste criptografar
                            if(k == 2 ){
                                Scanner reader2 = new Scanner(System.in); 
                                String chat = reader.next();                                
                                byte [] msg = chat.getBytes();
                                byte[] chat_msg = key.encriptarComChavePrivada(msg, key.getPriv());
                                byte[] descipt = key.decriptarComChavePublica(chat_msg, key.getPub());
                                String tomate = new String(descipt);
                                System.out.println(tomate);
                                messageOut = new DatagramPacket(chat_msg, chat_msg.length, group, 6789);
                                s.send(messageOut);	
                            }
                            
                            
                        
                            
                            
                            cleanBuffer(buffer);
                            k++;
                            System.out.println("Iteracao numero " + k);
                            
                        }
                                                		
		}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		}catch (IOException e){System.out.println("IO: " + e.getMessage());
		}finally {if(s != null) s.close();}
	}		      	

    
}
