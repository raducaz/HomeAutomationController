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
byte serverIp[] = { 192, 168, 11, 99 };

volatile ThreadController threadsController = ThreadController();
volatile boolean monitorThreadRunning = 0;

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
EthernetClient arduinoClient;
//IPAddress serverIp = IPAddress(192,168,11,99); //<<< ENTER DEFAULT REMOTE SERVER IP ADDRESS HERE!!!

String ReceiveMsgFromServer()
{
  char endChar = '\n';
  String receivedText = "";

  if(arduinoClient)
  {
    while (arduinoClient.available()) {
       char receivedChar = arduinoClient.read();
       Serial.print(receivedChar);
  
       if (receivedChar==endChar){
        //TODO: Check the line received
        Serial.println(receivedText);
  
        // Reset the line received
        receivedText = "";
        Serial.println("END");
        break;
      }
      else
      {
        receivedText.concat(receivedChar);
      } 
     }
  }

   return receivedText;
}
public:boolean ConnectToServer()
{
  if(arduinoClient)
  {
    Serial.println("MON: Stop client.");
    arduinoClient.stop();
    delay(1000);
  }

  Serial.println("MON: Connect client.");
  if (arduinoClient.connect(serverIp, 9090)) {
    Serial.println("MON: Client connected.");
    return arduinoClient.connected();
  }
  Serial.println("MON: Client dinn't connect.");
  return false;
}
public:boolean SendMsgToServer(String msg)
{
  Serial.println("MON: Send " + msg);
  if(arduinoClient.connected())
   {
      Serial.println("MON: Client is connected, send " + msg);
       arduinoClient.println(msg);
       // Success
       return true;
   }
   else
   {
      Serial.println("MON: Client not connected, try connect");
      // Retry once
      if(ConnectToServer())
      {
        Serial.println("MON: Client connected on retry");
        arduinoClient.println(msg);
        // Success
        return true;
      }
      else
      {
        Serial.println("MON: Retry connect failed");
        // Failed
        return false;
      }
   }
}
    
  // Function executed on thread execution
  void run(){

    Serial.println("MON: exec monitor");

    // If needed the message from server
    //Serial.println(ReceiveMsgFromServer());
    
    boolean pinState = digitalRead(7);
    String msg = "PIN:" + String(pinState);
    Serial.println("MON:" + msg);
    
    if(SendMsgToServer(msg))
    {
      Serial.println("MON:Sent msg to server.");
    }
    else
    {
      Serial.println("MON:FAILED Sent msg to server.");
    }

    // Finish Thread execution
    runned();  
  }
  
};

volatile MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();

class MyTcpServerThread: public Thread
{
  EthernetServer server = EthernetServer(8080);
 
  // Function executed on thread execution
  void run(){

    server.begin();
  
    EthernetClient client = server.available();
    char endChar = '\n';
    String receivedText = "";
    
    if (client) {
     
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

              if(digitalRead(7)==HIGH)
                digitalWrite(7, LOW);
              else
                digitalWrite(7, HIGH);
                
              client.println(receivedText + "step 1");
              Serial.println(receivedText + "step 1");

              long i = 10000000;
              while(i>0)
              {i--;}
              
              client.println(receivedText + "step 2");
              Serial.println(receivedText + "step 2");
              
              client.println("END");
              Serial.println("END");
            }
  
            if(receivedText == "MonitorFct")
            {
              
              //noInterrupts();

              if(!monitorThreadRunning)
              {
                client.println("Try connect to server");
                if(monitorTcpClientThread.ConnectToServer())
                {
                  client.println("Connection to server success.");
                }
                else
                {
                  client.println("Connection to server failed.");
                }
  
                  client.println("Starting monitor thread.");
                  
                  // Set the interval the thread should run in loop
                  monitorTcpClientThread.setInterval(1000); // in ms

                  // Add thread to controller, this will fire the thread automatically
                  threadsController.add(&monitorTcpClientThread); 
                  // Mark monitor started
                  monitorThreadRunning = 1;
                  
                  //startThreadsController();
    
                  client.println("Monitor started");
                  Serial.println("Monitor started.");
                  
                //interrupts();
              }
              else
              {
                client.println("Monitor is running");
              }
              
              client.println("END");
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
      // No client, server not available()
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

  //MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
  //monitorTcpClientThread.setInterval(500); // in ms
  //threadsController.add(&monitorTcpClientThread);
}

void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);

  delay(1000);
  setupTcpServerThread();
  startThreadsController();

}

void loop() {
  
}
