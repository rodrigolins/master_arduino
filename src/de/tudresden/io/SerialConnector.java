package de.tudresden.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import de.tudresden.parsers.PropertyJsonParser;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialConnector implements SerialPortEventListener {

	SerialPort serialPort;
	
	private static final String SERIAL_PORT = "/dev/ttyACM0";

	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	
	/** The output stream to the port */
	private OutputStream output;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public void initialize() {
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		// System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
		System.setProperty("gnu.io.rxtx.SerialPorts", SERIAL_PORT);
		
		try {
			CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(SERIAL_PORT);
			
			// open serial port.
			serialPort = (SerialPort) port.open(this.getClass().getName(), TIME_OUT);
			
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,	SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
		} catch (NoSuchPortException e1) {
			System.err.println("Port not found");
			e1.printStackTrace();
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine = input.readLine();
				System.out.println(inputLine);
				System.out.println(PropertyJsonParser.parseLine(inputLine));
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
		// TODO: Check all other eventTypes
	}

	public static void main(String[] args) throws Exception {
		ArduinoComm main = new ArduinoComm();
		main.initialize();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing incoming messages to console).
				
				// Remove the try/catch block to run indefinitely.
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		};
		t.start();
		System.out.println("Started");
	}
}
