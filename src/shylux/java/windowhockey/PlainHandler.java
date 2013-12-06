package shylux.java.windowhockey;

import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class PlainHandler extends ConsoleHandler {
	public void publish(LogRecord record) {
		// check log level
		if (record.getLevel().intValue() < this.getLevel().intValue()) return;
		
		PrintStream outputStream = System.out;

		// if record is WARNING or higher log on stderr
		if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
			outputStream = System.err;
		}
		
		outputStream.println(record.getMessage());
	}
}
