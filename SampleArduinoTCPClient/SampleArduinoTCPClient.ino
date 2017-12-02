#include <SPI.h>
#include <Ethernet.h>

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(192,168,11,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
//EthernetServer server(8080);

EthernetClient client;
byte serverip[] = { 192, 168, 11, 99 }; //<<< ENTER REMOTE IP ADDRESS HERE!!!

void setup() {
  Serial.begin(9600);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  
  //server.begin();

  Ethernet.begin(mac, ip);
   Serial.begin(9600);

   delay(1000);

   Serial.println("connecting...");

   if (client.connect(serverip, 9090)) {
     Serial.println("connected");
     client.println("Test arduino");
     client.println();
   } else {
     Serial.println("connection failed");
   }
}

int increment=0;
char endChar = '\n';
char receivedChar;
String receivedText = "";

void loop() {
  if (client.available()) {
     char receivedChar = client.read();
     Serial.print(receivedChar);

     if (receivedChar==endChar){
      //TODO: Check the line received
      Serial.println(receivedText);

      // Reset the line received
      receivedText = "";

      Serial.println("END");
      client.stop();
    }
    else
    {
      receivedText.concat(receivedChar);
    } 
   }

   if (!client.connected()) {
     Serial.println();
     Serial.println("disconnecting.");
     client.stop();

     delay(1000);
     Serial.println("reconnecting...");

     if (client.connect(serverip, 9090)) {
       if(client.connected())
       {
          increment++;
         Serial.println("connected");
         client.println(increment);
       }
     } else {
       Serial.println("connection failed");
     }
   }
}
