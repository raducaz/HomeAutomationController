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
          client.println("GOT:"+receivedText);
          Serial.print("GOT:"+receivedText);
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
    delay(1);
    Serial.println("END LOOP");

  }
  else
  {
    //Serial.println("NO CLIENT");
  }
}
