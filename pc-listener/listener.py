import socket
import json
import logging
import time
from pynput.keyboard import Key, Controller as KeyboardController
from pynput.mouse import Button, Controller as MouseController

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')

keyboard = KeyboardController()
mouse = MouseController()

# Action to Key/Button Mapping
ACTION_MAP = {
    "FIRE": Button.left,
    "ADS": Button.right,
    "AIM": None,
    "RELOAD": 'r',
    "JUMP": Key.space,
    "CROUCH": 'c',
    "PRONE": 'z',
    "SPRINT": Key.shift,
    "AUTO_SPRINT": 'l',
    "PEEK_LEFT": 'q',
    "PEEK_RIGHT": 'e',
    "INTERACT": 'f',
    "BAG": Key.tab,
    "MAP": 'm',
    "SETTINGS": Key.esc,
    "FREE_VIEW": Key.alt,
    "WEAPON_1": '1',
    "WEAPON_2": '2',
    "THROW_GRENADE": '4',
    "CONSUMABLE_MEDKIT": '9',
    "ADS": Button.right,
}

current_keys = set()

def handle_move(x, y):
    global current_keys
    new_keys = set()

    # Deadzone and threshold logic
    if y < -0.3:
        new_keys.add('w')
    if y > 0.3:
        new_keys.add('s')
    if x < -0.3:
        new_keys.add('a')
    if x > 0.3:
        new_keys.add('d')

    # Release keys no longer active
    for key in current_keys - new_keys:
        keyboard.release(key)
        print(f"Released: {key}")

    # Press new keys
    for key in new_keys - current_keys:
        keyboard.press(key)
        print(f"Pressed: {key}")

    current_keys = new_keys

def handle_event(event):
    action = event.get("action")
    state = event.get("state")

    if action == "MOVE":
        handle_move(event.get("x", 0), event.get("y", 0))
        return

    if action == "MOVE_STOP":
        for k in list(current_keys):
            keyboard.release(k)
        current_keys.clear()
        print("Movement Stopped")
        return

    if action == "AIM":
        dx = event.get("dx", 0)
        dy = event.get("dy", 0)
        mouse.move(int(dx), int(dy))
        return

    target = ACTION_MAP.get(action)
    if not target:
        return

    if isinstance(target, (str, Key)):
        if state == "DOWN" or state == "TAP":
            keyboard.press(target)
            if state == "TAP":
                time.sleep(0.05)
                keyboard.release(target)
        elif state == "UP":
            keyboard.release(target)
    elif isinstance(target, Button):
        if state == "DOWN" or state == "TAP":
            mouse.press(target)
            if state == "TAP":
                time.sleep(0.05)
                mouse.release(target)
        elif state == "UP":
            mouse.release(target)

def start_server(host='0.0.0.0', port=12345):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((host, port))
        s.listen()
        print(f"Server listening on {host}:{port}")

        while True:
            conn, addr = s.accept()
            print(f"Connected by {addr}")

            with conn:
                buffer = ""
                while True:
                    try:
                        data = conn.recv(1024)
                        if not data:
                            break

                        buffer += data.decode()
                        lines = buffer.split("\n")
                        buffer = lines[-1]

                        for line in lines[:-1]:
                            try:
                                if not line.strip():
                                    continue
                                event = json.loads(line)
                                handle_event(event)
                            except Exception as e:
                                print("JSON ERROR:", e)
                    except Exception as e:
                        print("CONN ERROR:", e)
                        break

if __name__ == "__main__":
    start_server()
