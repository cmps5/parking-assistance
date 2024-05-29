// defines pins numbers
const int trigPin = 9;
const int echoPin = 10;
const int buzzer = 11;
const int ledPin = 13;

// defines variables
long duration;
int distance;
int safetyDistance;
int prevDistance = 0;
unsigned long prevTime;
unsigned long currentTime;
float velocity;

void setup() {
  pinMode(trigPin, OUTPUT);  // Sets the trigPin as an Output
  pinMode(echoPin, INPUT);   // Sets the echoPin as an Input
  pinMode(buzzer, OUTPUT);
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600);  // Starts the serial communication
  prevTime = millis(); // Initialize the previous time
}

void loop() {
  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);

  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);

  // Calculating the distance
  distance = duration * 0.034 / 2;

  // Calculate the time difference
  currentTime = millis();
  unsigned long timeDiff = currentTime - prevTime;

  // Calculate the velocity (change in distance over time in cm/ms converted to cm/s)
  velocity = (prevDistance - distance) / (timeDiff / 1000.0);

  // Update previous distance and time for the next calculation
  prevDistance = distance;
  prevTime = currentTime;

  // Set the safety distance threshold and control the buzzer and LED
  safetyDistance = distance;
  if (safetyDistance <= 5) {
    digitalWrite(buzzer, HIGH);
    digitalWrite(ledPin, HIGH);
  } else {
    digitalWrite(buzzer, LOW);
    digitalWrite(ledPin, LOW);
  }

  // Prints the distance and velocity on the Serial Monitor
  Serial.print("Distance ");
  Serial.print(distance);
  Serial.print(", Velocity ");
  Serial.println(velocity);
  
  // Add a short delay to avoid flooding the serial monitor and to give more accurate readings
  delay(100);
}
