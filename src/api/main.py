import serial
import time
import paho.mqtt.client as mqtt
import re

ser = serial.Serial('/dev/ttyACM0', 9600, timeout=1)
time.sleep(2)

broker_address = "192.168.5.2"
client = mqtt.Client()
client.connect(broker_address)

pattern = re.compile(r"Distance\s*(-?\d+(?:[.,]\d+)?),\s*Velocity\s*(-?\d+(?:[.,]\d+)?)")
dist = "0"
vel = "0"
max_dist = 100

def connect_mqtt():
	def on_connect(client, userdata, flags, rc):
		if rc == 0:
			print("Connected to MQTT Broker")
		else:
			print("Failed to connect, return code %d\n", rc)

	client.on_connect = on_connect
	client.connect(broker_address, 1883, 60)

def publish_message(client, topic, message):
	result = client.publish(topic, message)
	status = result[0]
	if status == 0:
		print(f"Sent `{message}` to topic `{topic}`")
	else:
		print(f"Failed to send message to topic {topic}")

def time_to_hit():
	global dist, vel
	distFloat = float(dist)
	velFloat = float(vel)
	if velFloat > 0:
		to_hit = distFloat / velFloat
		return to_hit
	else:
		return 0

def main():
	connect_mqtt()
	client.loop_start()
	global dist, vel

	try:
		while True:
			line = ser.readline().decode('utf-8').rstrip()
			match = pattern.search(line)
			if match:
				dist = match.group(1)
				vel = match.group(2)
				if float(dist) < max_dist:
					to_hit = time_to_hit()
					client.publish("sensor/dist", dist)
					client.publish("sensor/vel", vel)
					client.publish("topic/hit", to_hit)
					#print(f"Published: Distance={dist} Velocity={vel} TimeToHit = {to_hit}")
			else:
				print(f"unexpected format: {line}")
	except KeyboardInterrupt:
		print("Script interrupted by user")

if __name__ == "__main__":
	main()
