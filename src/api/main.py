from flask import Flask, jsonify, request
import paho.mqtt.client as mqtt
import serial
import threading

app = Flask(__name__)

car_running = False  # Car status
distance_value = None  # Shared variable for distance
velocity_value = None  # Shared variable for velocity
dummy_distance = 20
dummy_velocity = 5

# MQTT Settings
MQTT_BROKER = "170.10.20.2"
MQTT_PORT = 1883
MQTT_TOPIC_PREFIX = "arduino/"

# Serial Settings
SERIAL_PORT = '/dev/ttyUSB0'  # Replace with your serial port
BAUD_RATE = 9600

# Initialize serial connection
ser = serial.Serial(SERIAL_PORT, BAUD_RATE)

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

def publish_mqtt(topic, payload):
    client = mqtt.Client()
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.publish(topic, payload)
    client.disconnect()

@app.route('/distance', methods=['GET'])
def get_distance():
    global distance_value
    if distance_value is not None:
        return jsonify({'distance': distance_value})
    else:
        return jsonify({'error': 'Distance value not available'})

@app.route('/velocity', methods=['GET'])
def get_velocity():
    global velocity_value
    if velocity_value is not None:
        return jsonify({'velocity': velocity_value})
    else:
        return jsonify({'error': 'Velocity value not available'})

@app.route('/control', methods=['POST'])
def control_car():
    global car_running
    action = request.json.get('action')
    if action == 'start':
        car_running = True
        publish_mqtt(MQTT_TOPIC_PREFIX + "control", "start")
        return jsonify({'message': 'Car started'})
    elif action == 'stop':
        car_running = False
        publish_mqtt(MQTT_TOPIC_PREFIX + "control", "stop")
        return jsonify({'message': 'Car stopped'})
    else:
        return jsonify({'error': 'Invalid action'})

@app.route('/status', methods=['GET'])
def get_car_status():
    global car_running
    status = 'running' if car_running else 'stopped'
    publish_mqtt(MQTT_TOPIC_PREFIX + "status", status)
    return jsonify({'status': status})

# Update distance value
def update_distance(new_distance):
    global distance_value
    distance_value = new_distance
    publish_mqtt(MQTT_TOPIC_PREFIX + "distance", str(new_distance))

# Update velocity value
def update_velocity(new_velocity):
    global velocity_value
    velocity_value = new_velocity
    publish_mqtt(MQTT_TOPIC_PREFIX + "velocity", str(new_velocity))

# Function to read and parse serial data
def read_serial():
    global ser
    while True:
        if ser.in_waiting > 0:
            try:
                line = ser.readline().decode('utf-8').strip()
                if line.startswith("Distance: ") and ", Velocity: " in line:
                    parts = line.split(", ")
                    distance_part = parts[0].split(": ")[1]
                    velocity_part = parts[1].split(": ")[1]
                    distance = float(distance_part)
                    velocity = float(velocity_part)
                    update_distance(distance)
                    update_velocity(velocity)
            except Exception as e:
                print(f"Error reading serial data: {e}")

# Calculate time to hit an obstacle
@app.route('/time_to_obstacle', methods=['GET'])
def calculate_time_to_obstacle():
    global distance_value, velocity_value
    if distance_value is not None and velocity_value is not None:
        time_to_obstacle = distance_value / velocity_value
        return jsonify({'time_to_obstacle': time_to_obstacle})
    else:
        return jsonify({'error': 'Distance or velocity value not available'})

if __name__ == '__main__':
    serial_thread = threading.Thread(target=read_serial)
    serial_thread.daemon = True
    serial_thread.start()
    update_distance(dummy_distance)
    update_velocity(dummy_velocity)
    app.run(host='0.0.0.0', port=5000)
