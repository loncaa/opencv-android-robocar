#include <CarController.h>
#include <SoftwareSerial.h>
#include <string.h>

SoftwareSerial myBluetooth(4, 2); // RX - tx , TX - rx;
CarController myCar(10,9,6, 8,7,5); //in1,in2,en1, in3,in4,en2

byte speedin = 100; //0 - 255
int ndelay = 500, partOfInput = 0, j = 0;
char c[5], direct[3], nspeed[4], dtime[5] ;

int pow10(int len)
{
  double result = 1;
  int n = 0;
  while(n < len)
  {
    result *=10;
    n++;
  }

  return (result);
}
int stringToInt(char *a, int length)
{
   int number = 0;
   for (int i = 0; a[i] != '\0'; i++)
   {
    //number += (a[i] - 48)*pow(10.0, (length - 1) - i);
    //3% storage space-a se moze smanjiti ako se koristi funkcija pow10()
    number += (a[i] - 48)*pow10((length - 1) - i);  
   }  
   
   return (number);
}

void setup() {
  Serial.begin(9600);
  myBluetooth.begin(9600);
}

void loop() {
  while(myBluetooth.available()){
    
    c[j++] = (char) myBluetooth.read();
    
    /* pocetak parsiranja ulaza*/
    if(c[j-1] == ':')
    {
      c[j-1] = '\0';
      
      if(partOfInput == 0) /* sprema se dio do prve dvotocke */
      {
        partOfInput++;
        strcpy(direct, c); 
        
        //ako je S - stop.. te ako je bilo koji drugi znak koji nema nikakve veze sa upravljanjem          
        if(!strcmp(direct, "S")) //|| (strcmp(direct, "F") || strcmp(direct, "B") || strcmp(direct, "TR") || strcmp(direct, "RR") || strcmp(direct, "TL") || strcmp(direct, "RL"))) 
        {      
          myCar.stop();        
          partOfInput = 0;
        }
        
         Serial.println(c);
      }
      else if(partOfInput == 1) /* sprema se dio do druge dvotocke */
      { 
        partOfInput++;
        strcpy(nspeed, c);
       
        Serial.println(c);
        
        speedin = stringToInt(nspeed, j-1);    
        myCar.moveOn(direct, speedin);
      }
      else if(partOfInput == 2) /* treca dvotocka, odnosno kraj poruke */
      {
        
        Serial.println(c);
        /* ovaj stop sluzi ako nije defnirano vrijeme vrtnje motora */
         if(!strcmp(c, "S") )
         {
          myCar.stop(); 
          myBluetooth.write("stop");
         }
         else
         {
           strcpy(dtime, c);           
           ndelay = stringToInt(dtime, j-1);
           
           //myCar.moveOnWithDelay(direct, speedin, ndelay);
           delay(ndelay);
           myCar.stop();
         }
         
         partOfInput = 0;
      }

      j = 0;      
      }
    }
  }
