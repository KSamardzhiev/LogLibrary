package com.ksamardzhiev.jst;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Logger implements AutoCloseable {

	private static final int INDEX_RECORD_SIZE = 8 + 8;
	//time stamp and offset size
	private static final long MAX_LOG_FILE_SIZE = 1024 * 1024;
	private static final SimpleDateFormat tsFORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
	private Path logFile;
	private Path archiveFile;
	private PrintWriter logFileWriter;
	private SeekableByteChannel indexFileChannel;

	public Logger(Path filePath, Path archiveFile) throws IOException {
		
		this.logFile = filePath;
		this.archiveFile = archiveFile;
		this.openLogFile();
		this.openIndexFile();

	}
	
	private void openIndexFile() throws IOException{
		Path indexFilePath = Paths.get(this.logFile+".idx");
		this.indexFileChannel = Files.newByteChannel(indexFilePath, 
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE);
		
	}

	public void log(String msg) throws IOException {
		Calendar now = Calendar.getInstance();
		
		this.updateInde(now);
		
		String nowAsString = tsFORMAT.format(now.getTime());
		this.logFileWriter.println(nowAsString + " " + msg);
		this.archiveIfNecessary();

	}

	private void updateInde(Calendar now) throws IOException {
		// TODO Auto-generated method stub
		this.indexFileChannel.position(this.indexFileChannel.size());
		
		long timeInMillis = now.getTimeInMillis();
		long offset =  Files.size(this.logFile);
		ByteBuffer buf = ByteBuffer.allocate(this.INDEX_RECORD_SIZE);
		
		buf.putLong(timeInMillis);
		buf.putLong(offset);
		buf.flip();
		this.indexFileChannel.write(buf);
		
	}

	private void archiveIfNecessary() throws IOException {

		long currentSize = Files.size(this.logFile);

		if (currentSize > MAX_LOG_FILE_SIZE) {

			this.logFileWriter.close();
			
			// String archiveFileURI =
			// this.archiveFile.toAbsolutePath().toUri().toString();

			// String absArchivePath =
			// this.archiveFile.toAbsolutePath().toString();
			// absArchivePath.replaceAll("\\\\","/")

			URI uri = URI.create("jar:file:" + this.archiveFile.toAbsolutePath());
			Map<String, String> env = new HashMap<String, String>();
			env.put("create", "true");

			try (FileSystem zipFS = FileSystems.newFileSystem(uri, env)) {
				Calendar now = Calendar.getInstance();
				String year = Integer.toString(now.get(Calendar.YEAR));
				String month = Integer.toString(now.get(Calendar.MONTH) + 1);
				String day = Integer.toString(now.get(Calendar.DAY_OF_MONTH));

				Path archiveDir = zipFS.getPath(year, month, day);
				Files.createDirectories(archiveDir);
				int maxLogFileNum = 0;
				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(archiveDir, "log.*")) {
					for (Path logFile : dirStream) {

						String fileName = logFile.getFileName().toString();
						String logFileNameAsString = fileName.substring("log.".length());
						int logFileNum = Integer.parseInt(logFileNameAsString);

						if (logFileNum > maxLogFileNum) {
							maxLogFileNum = logFileNum;
						}
					}

				}
				int newLogFileNum = maxLogFileNum + 1;
				Path newArchivedLogFile = archiveDir.resolve("log." + newLogFileNum);
				
				Files.move(this.logFile, newArchivedLogFile);
			}
			this.openLogFile();
		}

	}

	private void openLogFile() throws IOException {
		this.logFileWriter = new PrintWriter(new FileOutputStream(this.logFile.toString(), true));
	}

	@Override
	public void close() throws Exception {
		this.logFileWriter.close();
		this.indexFileChannel.close();
	}
	
	public String getFirstMessageAfter(Calendar moment) throws IOException, ParseException{
		
		this.logFileWriter.flush();//problem
		
		try(InputStream is = new FileInputStream(this.logFile.toString());
			Reader isr = new InputStreamReader(is);
			BufferedReader logFileReader = new BufferedReader(isr)){
			
			String line;
			while((line=logFileReader.readLine())!=null){
				tsFORMAT.parse(line).getTime();
				long milis = moment.getTimeInMillis();
				if(milis>=moment.getTimeInMillis()){
					return line;
					
				}
			}
		}
		return null;
	}
	
	public String getFirstMessageAfter_BinSearch(Calendar moment) throws IOException{
		
		long count = this.indexFileChannel.size() / this.INDEX_RECORD_SIZE;
		binSearch(moment,0, count);
		
		return null;
		
	}
	
	private String binSearch(Calendar moment, long left, long right) throws IOException{
		
		if(left >= right){
			this.indexFileChannel.position(left * this.INDEX_RECORD_SIZE);
			ByteBuffer buf = ByteBuffer.allocate(this.INDEX_RECORD_SIZE);
			this.indexFileChannel.read(buf);
			buf.flip();
			
			long millis = buf.getLong();
			long offset = buf.getLong();
	
			try(InputStream is = new FileInputStream(this.logFile.toString());
					Reader isr = new InputStreamReader(is);
					BufferedReader logFileReader = new BufferedReader(isr)){
				is.skip(offset);
				return logFileReader.readLine();
			}
		}
		
		long middle = (right-left)/2+left;
		
		this.indexFileChannel.position(middle * this.INDEX_RECORD_SIZE);
		ByteBuffer buf = ByteBuffer.allocate(this.INDEX_RECORD_SIZE);
		this.indexFileChannel.read(buf);
		buf.flip();
		
		long millis = buf.getLong();
		long offset = buf.getLong();
		
		if(moment.getTimeInMillis() <= millis){
			
			return binSearch(moment, left, middle);
		} else {
			return binSearch(moment, middle+1, right);
		}
			
	}
}
