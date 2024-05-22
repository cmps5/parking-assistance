from flask import Flask, request, jsonify
import socket

app = Flask(__name__)

car_running = False  # Status do carro

# IP address and port of the Arduino within the local Wi-Fi network
ARDUINO_IP = 'arduino_local_ip_address'  # Replace with the correct IP address
ARDUINO_PORT = 1234  # Replace with the correct port


# Função para buscar a distância mediada pelo sensor
def receive_distance_from_arduino():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.connect((ARDUINO_IP, ARDUINO_PORT))
            s.sendall(b'DISTANCE')  # Send a request for distance
            data = s.recv(1024)  # Adjust buffer size as needed
            distance = float(data.decode('utf-8').strip())
            return distance
        except Exception as e:
            print(f"Error receiving distance from Arduino: {e}")
            return None
        
# Função para buscar a velocidade do carro
def receive_velocity_from_arduino():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.connect((ARDUINO_IP, ARDUINO_PORT))
            s.sendall(b'VELOCITY')  # Send a request for velocity
            data = s.recv(1024)  # Adjust buffer size as needed
            velocity = float(data.decode('utf-8').strip())
            return velocity
        except Exception as e:
            print(f"Error receiving velocity from Arduino: {e}")
            return None

# Mandar a distância para o API
@app.route('/distance', methods=['GET'])
def get_distance():
    distance = receive_distance_from_arduino()
    if distance is not None:
        return jsonify({'distance': distance})
    else:
        return jsonify({'error': 'Failed to receive distance from Arduino'})

# Mandar a velocidade para o API
@app.route('/velocity', methods=['GET'])
def get_velocity():
    velocity = receive_velocity_from_arduino()
    if velocity is not None:
        return jsonify({'velocity': velocity})
    else:
        return jsonify({'error': 'Failed to receive velocity from Arduino'})
    
# Controlar o carro pela a app
@app.route('/control', methods=['POST'])
def control_car():
    global car_running
    action = request.json.get('action')
    if action == 'start':
        car_running = True
        return jsonify({'message': 'Car started'})
    elif action == 'stop':
        car_running = False
        return jsonify({'message': 'Car stopped'})
    else:
        return jsonify({'error': 'Invalid action'})

# Buscar o status do carro atraves da app
@app.route('/status', methods=['GET'])
def get_car_status():
    return jsonify({'status': 'running' if car_running else 'stopped'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
