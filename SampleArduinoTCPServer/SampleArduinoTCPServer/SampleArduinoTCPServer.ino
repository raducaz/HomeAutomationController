#include <ArduinoJson.h>
#include <SPI.h>
#include <Ethernet.h>
#include "Thread.h"
#include "ThreadController.h"
#include <TimerOne.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(192,168,11,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
EthernetServer server(8080);
EthernetClient arduinoClient;
IPAddress serverIp(192,168,11,99); //<<< ENTER DEFAULT REMOTE SERVER IP ADDRESS HERE!!!

ThreadController threadsController = ThreadController();
void startThreadsController()
{
  // Is best practice to start the threadController from a Timer interrupt so we avoid blocking the main thread
  Timer1.stop();

  Timer1.initialize(500); // in micro second (us)
  Timer1.attachInterrupt(starterTimerCallback);
  Timer1.start();
}
class MyMonitorTcpClientThread: public Thread
{
  int no=0;
  //public:
    //unsigned int serverPort = 9090; //<<< ENTER DEFAULT REMOTE SERVER PORT HERE!!!

public:String sendMessageToServer(String msg)
{
  if(arduinoClient)
  {
  // Connect to server and send status
    if (arduinoClient.connect(serverIp, 9090)) {
      arduinoClient.println(msg + ":" + no);
      no++;
      return "OK";
     } else {
       return "connection failed";
     }
     arduinoClient.stop();
  }
}
    
  // Function executed on thread execution
  void run(){
    Serial.println("exec monitor");
    boolean pinState = digitalRead(7);
    String msg = "PIN:";// + pinState;
    String result = sendMessageToServer(msg);
    Serial.println("MON:" + result + ":" + pinState);
    }
    
    runned();  
};

class MyTcpServerThread: public Thread
{
  boolean printedNoClient = 0;
  boolean printedClient = 0;
  //public:
    //IPAddress serverIP(192,168,11,99); //<<< ENTER DEFAULT REMOTE SERVER IP ADDRESS HERE!!!
    //unsigned int serverPort = 9090; //<<< ENTER DEFAULT REMOTE SERVER PORT HERE!!!

  // Function executed on thred execution
  void run(){
    
    //Serial.println("START LOOP");
    EthernetClient client = server.available();
    //Serial.println("SERVER AVAIL");
    char endChar = '\n';
    String receivedText = "";
    
    if (client) {

      if(!printedClient)
      {
        Serial.println("Client present.");
        printedNoClient = 0;
        printedClient = 1;
      }
      
      int size = 0;
      while (client.connected()) {
        if (client.available()) {
          
          char receivedChar = client.read();
          
          Serial.print(receivedChar);
  
          size ++;
          
          if (receivedChar==endChar)
          {
            Serial.println("CMD:"+receivedText);
            
            if(receivedText == "StateFct")
            {
              client.println("Executing " + receivedText);
              Serial.println("Executing " + receivedText);
              delay(1000);
              int i = 1000;
              while(i>0)
              {i--;}
              digitalWrite(7, HIGH);
              client.println(receivedText + "step 1");
              Serial.println(receivedText + "step 1");
              i = 1000;
              while(i>0)
              {i--;}
              //digitalWrite(7, LOW);
              client.println(receivedText + "step 2");
              Serial.println(receivedText + "step 2");
              i = 1000;
              while(i>0)
              {i--;}
              client.print("END");
              Serial.println("END");
            }
  
            if(receivedText == "MonitorFct")
            {
              MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
              
              client.println("Try connect to server");
              String testResult = monitorTcpClientThread.sendMessageToServer("Test monitor 2");
              client.println(testResult);

              if(testResult == "OK")
              {
                client.println("Starting monitor thread.");
                
                // Set the interval the thread should run in loop
                monitorTcpClientThread.setInterval(1000); // in ms
              
                threadsController.add(&monitorTcpClientThread); 
  
                startThreadsController();
  
                client.println("Monitor started");
                Serial.println("Monitor started.");
              }
              client.print("END");
            }
            
            receivedText = "";
          }
          else
          {
            receivedText.concat(receivedChar);
          } 
        } 
      }
  
      Serial.println();
      Serial.println("CLOSE CONNECTION"); 
      client.stop();
      Serial.println("END LOOP");
  
    }
    else
    {
      if(!printedNoClient)
      {
        Serial.println("No Client.");
        printedNoClient = 1;
        printedClient = 0;
      }
    }  

    runned();

  }
};

void starterTimerCallback(){
  threadsController.run();
}

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(1); // in ms
  threadsController.add(&tcpServerThread);

  MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
  monitorTcpClientThread.setInterval(500); // in ms
  threadsController.add(&monitorTcpClientThread);
}

void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);
  server.begin();

  delay(1000);
  //setupTcpServerThread();
  //startThreadsController();

}
int increment = 0;
void loop() {
  increment ++;
  if (arduinoClient.connect(serverIp, 9090)) {
    arduinoClient.println("Test"+increment);
  }
}
