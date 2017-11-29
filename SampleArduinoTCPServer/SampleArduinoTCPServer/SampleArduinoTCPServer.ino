#include <ArduinoJson.h>
#include <SPI.h>
#include <Ethernet.h>

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(192,168,11,100); //<<< ENTER YOUR IP ADDRESS HERE!!!

EthernetServer server(8080);

void setup() {
  Serial.begin(9600);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  server.begin();
}

void loop() {
 
  //Serial.println("START LOOP");
  EthernetClient client = server.available();
  //Serial.println("SERVER AVAIL");
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
            delay(1000);
            client.println(receivedText + "step 1");
            Serial.println(receivedText + "step 1");
            delay(1000);
            client.println(receivedText + "step 2");
            Serial.println(receivedText + "step 2");
            delay(1000);
            client.println("END");
            Serial.println("END");
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
    delay(100);
    Serial.println("END LOOP");

  }
  else
  {
    //Serial.println("NO CLIENT");
  }
}
