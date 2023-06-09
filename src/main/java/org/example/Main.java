package org.example;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.opencsv.bean.CsvToBeanBuilder;

import modelo.Pronostico;
import modelo.Resultado;
import modelo.Configuracion;


public class Main {
	public static void main(String[] args) throws JsonProcessingException  {
		
		
		
		// -------- Leemos el Archivo de Configuracion para recuperar algunos datos.
		
		
			//Grabamos la configuracion una vez para tener el archivo congruente con lo que vamos a leer
		
//				Configuracion configuracion = new Configuracion();
//				ObjectMapper objectMapper = new ObjectMapper();
//				
//				configuracion.setDRVCadena("com.mysql.cj.jdbc.Driver");
//				configuracion.setPartidosCSV("D:\\\\develop\\\\Java\\\\TrabajoPractico\\\\src\\\\main\\\\resources\\\\partidos.csv");
//				configuracion.setPronosticoCSV("D:\\\\develop\\\\Java\\\\TrabajoPractico\\\\src\\\\main\\\\resources\\\\pronostico.csv");
//				configuracion.setPronosticoSQL("jdbc:mysql://localhost:33306/tp\",\"root\",\"javadb");
//				configuracion.setPuntosxAcierto(2);
//				
//				String json = objectMapper.writeValueAsString(configuracion);
//				
//				try {
		
	//				FileWriter fileWriter = new FileWriter("configuracion.json");
	//				fileWriter.write(json);
	//				fileWriter.close();
	//				
//				} catch (IOException ex) {
//					ex.printStackTrace(); }
//				
//				System.out.println(json);
//			
		
		// ----- Ahora vamos a leerlo.
		

		
//		try {
//			
//			
////			ObjectMapper objectMapper = new ObjectMapper();
////			Configuracion configuracion = objectMapper.readValue(configFile, Configuracion.class);
////			
////				String cfgPronosticoCSV = configuracion.getPronosticoCSV();
////				String cfgPronosticoSQL =configuracion.getPronosticoSQL();
////				String cfgPartidosCSV =configuracion.getPartidosCSV();
////				int cfgPuntosxAcierto =configuracion.getPuntosxAcierto();
////				String cfgDRVCadena =configuracion.getDRVCadena();
////			
//			
//		} catch (JsonProcessingException ex) { 
//			System.err.println("No se Pudieron recuperar los datos.");
//			ex.printStackTrace(); 
//		} catch (IOException ex) { 
//			System.err.println("Error en el Archivo.");
//			ex.printStackTrace(); 
//		}
//		
		
		
		// --------------------------------------------------------------------------

		// ----- Creamos las listas que vamos a utilizar con los archivos CSV
		
		List<Resultado> listaDeResultados = new ArrayList<>();
		List<Pronostico> listaDePronostico = new ArrayList<>();
		
		//----- Leemos el Archivo de los resultados de los partidos  
		try {
			//listaDeResultados = new CsvToBeanBuilder(new FileReader(args[0]))
			listaDeResultados = new CsvToBeanBuilder<Resultado>(new FileReader("D:\\develop\\Java\\TrabajoPractico\\src\\main\\resources\\partidos.csv"))
			//listaDeResultados = new CsvToBeanBuilder(new FileReader(cfgPartidosCSV );
					.withSkipLines(1)
					.withSeparator(';')
					.withType(Resultado.class)
					.build()
					.parse();
		} catch (IOException ex) { 
			System.err.println("Erorr al leer el archivo");
			ex.printStackTrace(); }
		
		System.out.println("Leimos el Archivo .CSV de los Partidos...");
		


		//--------------------------------------------------------------------------------------------------------------------------        
		//----- Leemos el Archivo de los pronosticos.
		try {
			//listaDePronostico = new CsvToBeanBuilder(new FileReader(args[1]))
			listaDePronostico = new CsvToBeanBuilder(new FileReader("D:\\develop\\Java\\TrabajoPractico\\src\\main\\resources\\pronostico.csv"))
					.withSkipLines(1)
					.withSeparator(';')
					.withType(Pronostico.class)
					.build()
					.parse();


		} catch (IOException ex) {
			System.err.println("Error al leer el archivo");
			ex.printStackTrace();  
		} 
		 
		System.out.println("Leimos el Archivo .CSV de los Pronosticos...");
		System.out.println("");
		//---------------------------------------------------------------------------------------------------
		// Procesamos los Datos de los archivos Leidos. Ambos en .csv
		//---------------------------------------------------------------------------------------------------
	
		//Creamos un diccionario con hashmap
		HashMap<String, Integer> hashMapPronostico = new HashMap<>();
		
		int i = 0;
		int nVal = 0;
		
		//----- recorremos pronostico y sumamos en el diccionario (hashmap) la  puntuacion de los participantes
		for (Pronostico pronostico: listaDePronostico) {
			if (hashMapPronostico.containsKey(pronostico.getplayer())==false) {
	        	hashMapPronostico.put(pronostico.getplayer(), 0);
	        	i = 0;
	        }
			
			if (listaDeResultados.get(i).getGanador().equalsIgnoreCase(pronostico.getPronostico())) {
	            nVal = hashMapPronostico.get(pronostico.getplayer());
	            nVal = nVal + 1;
	            hashMapPronostico.put(pronostico.getplayer(), nVal);
			}
			
			i = i + 1;
		}
		//----- Imprimimos los resultados
		for (HashMap.Entry<String, Integer> entry : hashMapPronostico.entrySet()) {
            System.out.printf("Jugador: %s.  \t  Puntuacion: %d\n", entry.getKey(), entry.getValue());
        }
		
		System.out.println();
		
		//---------------------------------------------------------------------------------------------------
		//	Tratamos de leer los pronosticos de una DB que se llama tp. 
		//  tiene la misma forma que el pronostico.cvs 
		//---------------------------------------------------------------------------------------------------
		
		System.out.println("Leemos nuestra DB coon los pronosticos");
		
		
		
		try {
			//Registro driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Creo la conexion
            Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:33306/tp","root","javadb");
            System.out.println("...");  
            //Creamos la sentencia SQL
            Statement stmt=conn.createStatement();
            
            ResultSet rs=stmt.executeQuery("SELECT * FROM Pronostico;");
            
            
            
            //Procesamos los datos con otro diccionario hecho con hashmap
            HashMap<String, Integer> hashMapPronosticoDB = new HashMap<>();
            //Hacemos otra Lista de Pronosticos que se llame PronosticosDB
            List<Pronostico> listaDePronosticoDB = new ArrayList<>();
            
				
            
            //lleno mi nuevo array con los registros de la DB
				while (rs.next()) {
					Pronostico pronosticoAUX = new Pronostico(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6));
					listaDePronosticoDB.add(pronosticoAUX);
				}
				
				
				//Hago la misma comparacion que cuando tengo el archivo .CSV
				i = 0;
				nVal = 0;
				
				//----- recorremos pronostico y sumamos en el diccionario (hashmap) la  puntuacion de los participantes
				for (Pronostico pronostico: listaDePronosticoDB) {
					if (hashMapPronosticoDB.containsKey(pronostico.getplayer())==false) {
			        	hashMapPronosticoDB.put(pronostico.getplayer(), 0);
			        	i = 0;
			        }
					
					if (listaDeResultados.get(i).getGanador().equalsIgnoreCase(pronostico.getPronostico())) {
			            nVal = hashMapPronosticoDB.get(pronostico.getplayer());
			            nVal = nVal + 1;
			            hashMapPronosticoDB.put(pronostico.getplayer(), nVal);
					}
					
					i = i + 1;
				}				

					
				//----- Imprimimos los resultados de lo conseguido en la DB
				for (HashMap.Entry<String, Integer> entry : hashMapPronosticoDB.entrySet()) {
			        System.out.printf("Jugador: %s.  \t  Puntuacion: %d\n", entry.getKey(), entry.getValue());
			    }
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (SQLException ex) {
			ex.printStackTrace();  }
		
		System.out.println();
	

		
		System.out.println("-------.Fin del Programa.-------");	
		
		
		
		
		

	
	}
	

}





