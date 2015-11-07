package com.ksamardzhiev.jst;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public class Main {

	public static void main(String[] args) {

		Path logFile = Paths.get("logs", "current.log");
		Path archiveFile = Paths.get("logs", "archive.zip");
		
		try(Logger LOGGER = new Logger(logFile, archiveFile);){
			
//			for(int i=0; i<100000; i++){
//				LOGGER.log("test" + i);
//			}	
			
			//2015.11.07 10:58:03.587
			
//			Calendar moment = Calendar.getInstance();
//			moment.set(Calendar.YEAR, 2015);
//			moment.set(Calendar.MONTH, 11);
//			moment.set(Calendar.DAY_OF_MONTH, 7);
//			moment.set(Calendar.HOUR, 10);
//			moment.set(Calendar.MINUTE, 58);
//			moment.set(Calendar.SECOND, 3);
//			moment.set(Calendar.MILLISECOND, 587);
//			
//			
//			String msg = LOGGER.getFirstMessageAfter(moment);
//			System.out.println(msg);
//			
			
			Calendar moment = Calendar.getInstance();
			moment.set(Calendar.YEAR, 2015);
			moment.set(Calendar.MONTH, 11);
			moment.set(Calendar.DAY_OF_MONTH, 7);
			moment.set(Calendar.HOUR, 11);
			moment.set(Calendar.MINUTE, 53);
			moment.set(Calendar.SECOND, 49);
			moment.set(Calendar.MILLISECOND, 281);
			
			//2015.11.07 11:53:49.281 test94315
			
			String msg = LOGGER.getFirstMessageAfter_BinSearch(moment);
			System.out.println(msg);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
