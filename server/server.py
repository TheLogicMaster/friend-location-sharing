import time

from flask import Flask, request
import json
import copy
from os import path
from flask_httpauth import HTTPBasicAuth
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime
import uuid

app = Flask(__name__, static_url_path='/file', static_folder='files')
auth = HTTPBasicAuth()

data = {
    'users': {
        'user1': {
            'password': generate_password_hash('1234'),
            'friends': {
                'user2': {
                    'sharing': 'ALL'
                },
                'user3': {
                    'sharing': 'OFF'
                }
            },
            'sharing': 'CURRENT',
            'locations': []
        },
        'user2': {
            'password': generate_password_hash('1234'),
            'friends': {
                'user1': {
                    'sharing': 'OFF'
                },
                'user3': {
                    'sharing': 'CURRENT'
                }
            },
            'sharing': 'ALL',
            'locations': []
        },
        'user3': {
            'password': generate_password_hash('1234'),
            'friends': {
                'user1': {
                    'sharing': 'ALL'
                },
                'user2': {
                    'sharing': 'CURRENT'
                }
            },
            'sharing': 'OFF',
            'locations': []
        }
    },
    'groups': [
        {
            'name': 'Group 1',
            'id': '8dd60a04-7a0a-4b61-943a-577d5eb384d1',
            'users': {
                'user1': {
                    'sharing': 'CURRENT'
                },
                'user2': {
                    'sharing': 'ALL'
                },
                'user3': {
                    'sharing': 'OFF'
                }
            }
        }
    ],
    'chats': [
        {
            'name': 'Chat 1',
            'id': '8dd60a04-7a0a-4b61-943a-577d5eb384d4',
            'users': ['user1', 'user2', 'user3'],
            'messages': []
        }
    ]
}


def load_data():
    if not path.exists('data.json'):
        return
    with open('data.json') as f:
        global data
        data = json.load(f)


def save_data():
    with open('data.json', 'w') as f:
        json.dump(data, f)


@auth.verify_password
def verify_password(username, password):
    if username in data['users'] and \
            check_password_hash(data['users'][username]['password'], password):
        return username


@app.route('/signup')
def signup():
    password = request.args.get('password', default='')
    username = request.args.get('username', default='')
    if username in data['users']:
        return 'Sorry, that username is already taken'
    data['users'][username] = {
        'password': generate_password_hash(password),
        'friends': {},
        'sharing': 'ALL',
        'locations': []
    }
    save_data()
    print(f'{username} signed up using password: {password}')
    return 'Successfully created account!'


@app.route('/login')
@auth.login_required
def login():
    print(f'{auth.current_user()} signed in')
    return f'Welcome, {auth.current_user()}!'


@app.route('/groups')
@auth.login_required
def get_groups():
    groups = []
    for group in data['groups']:
        for user in group['users']:
            if auth.current_user() == user:
                groups.append(copy.copy(group))
                break
    return {'groups': groups}


# noinspection PyTypeChecker
@app.route('/group')
@auth.login_required
def get_group():
    group = None
    for g in data['groups']:
        if g['id'] == request.args.get('id'):
            group = copy.deepcopy(g)
    if group is None:
        return 'No such group', 404

    for name in group['users']:
        user = group['users'][name]
        locations = data['users'][name]['locations']
        if user['sharing'] == 'ALL' and data['users'][name]['sharing'] == 'ALL':
            user['locations'] = locations
        elif user['sharing'] != 'OFF' and data['users'][name]['sharing'] != 'OFF':
            user['locations'] = [locations[len(locations) - 1]] if len(locations) > 0 else []
        else:
            user['locations'] = []
    return group


@app.route('/createGroup', methods=['post'])
@auth.login_required
def create_group():
    users = {}
    for user in request.json['users']:
        users[user] = {
            'sharing': 'ALL'
        }
    if auth.current_user() not in users:
        users[auth.current_user()] = {
            'sharing': 'ALL'
        }
    data['groups'].append({
        'name': request.json['name'],
        'id': str(uuid.uuid4()),
        'users': users
    })
    save_data()
    return 'OK'


@app.route('/deleteGroup', methods=['post'])
@auth.login_required
def delete_group():
    group = None
    for g in data['groups']:
        if g['id'] == request.args.get('id'):
            group = g
    if group is None:
        return 'No such group', 404
    data['groups'].remove(group)
    save_data()
    return 'OK'


@app.route('/chats')
@auth.login_required
def get_chats():
    chats = []
    for chat in data['chats']:
        for user in chat['users']:
            if auth.current_user() == user:
                chats.append(copy.copy(chat))
                del chats[len(chats) - 1]['messages']
                break
    return {'chats': chats}


@app.route('/chat')
@auth.login_required
def get_chat():
    for chat in data['chats']:
        if chat['id'] == request.args.get('id'):
            return chat
    return 'No such chat', 404


@app.route('/createChat', methods=['post'])
@auth.login_required
def create_chat():
    users = request.json['users']
    if auth.current_user() not in users:
        users.append(auth.current_user())
    data['chats'].append({
        'name': request.json['name'],
        'id': str(uuid.uuid4()),
        'users': users,
        'messages': []
    })
    save_data()
    return 'OK'


@app.route('/deleteChat', methods=['post'])
@auth.login_required
def delete_chat():
    for c in data['chats']:
        if c['id'] == request.args.get('id'):
            chat = c
            break
    else:
        return 'No such chat', 404
    data['chats'].remove(chat)
    save_data()
    return 'OK'


@app.route('/sendMessage', methods=['post'])
@auth.login_required
def send_message():
    for c in data['chats']:
        if c['id'] == request.json['id']:
            chat = c
            break
    else:
        return 'No such chat', 404
    # noinspection PyUnresolvedReferences
    chat['messages'].append({
        'type': request.json['type'],
        'content': request.json['content'],
        'user': auth.current_user(),
        'id': str(uuid.uuid4()),
        'time': int(time.time())
    })
    save_data()
    return 'OK'


@app.route('/sendFile', methods=['post'])
@auth.login_required
def send_file():
    for c in data['chats']:
        if c['id'] == request.args.get('id'):
            chat = c
            break
    else:
        return 'No such chat', 404

    message_id = str(uuid.uuid4())
    request.files['file'].name = message_id
    request.files['file'].save('./files/' + message_id)

    # noinspection PyUnresolvedReferences
    chat['messages'].append({
        'type': request.args.get('type'),
        'content': 'https://example.thelogicmaster.com/file/' + message_id,
        'user': auth.current_user(),
        'id': message_id,
        'time': int(time.time())
    })
    save_data()
    return 'OK'


@app.route('/friends')
@auth.login_required
def get_friends():
    return data['users'][auth.current_user()]['friends']


@app.route('/friend')
@auth.login_required
def get_friend():
    name = request.args.get('name')
    if name not in data['users']:
        return 'No such user', 404
    if auth.current_user() not in data['users'][name]['friends']:
        return 'Not friends', 401
    friend = {
        'name': name,
        'sharing': data['users'][name]['friends'][auth.current_user()]['sharing']
    }
    locations = data['users'][name]['locations']
    if friend['sharing'] == 'ALL' and data['users'][name]['sharing'] == 'ALL':
        friend['locations'] = locations
    elif friend['sharing'] != 'OFF' and data['users'][name]['sharing'] != 'OFF':
        friend['locations'] = [locations[len(locations) - 1]] if len(locations) > 0 else []
    else:
        friend['locations'] = []
    return friend


@app.route('/addFriend', methods=['post'])
@auth.login_required
def add_friend():
    username = request.args.get('username')
    if username not in data['users']:
        return 'No such user exists', 404
    if username in data['users'][auth.current_user()]['friends']:
        return 'Friend already exists', 200
    data['users'][auth.current_user()]['friends'][username] = {
        'sharing': 'ALL'
    }
    save_data()
    return 'OK'


@app.route('/groupSharing', methods=['post'])
@auth.login_required
def update_group_sharing():
    group = None
    for g in data['groups']:
        if g['id'] == request.json['id']:
            group = g
    if group is None:
        return 'No such group', 404
    group['users'][auth.current_user()]['sharing'] = request.json['sharing']
    save_data()
    return 'OK'


@app.route('/friendSharing', methods=['post'])
@auth.login_required
def update_friend_sharing():
    if request.json['friend'] not in data['users'][auth.current_user()]['friends']:
        return 'No such friend', 404
    data['users'][auth.current_user()]['friends'][request.json['friend']]['sharing'] = request.json['sharing']
    save_data()
    return 'OK'


@app.route('/friendSharing')
@auth.login_required
def get_friend_sharing():
    if request.args.get('friend') not in data['users'][auth.current_user()]['friends']:
        return 'No such friend', 404
    return data['users'][auth.current_user()]['friends'][request.args.get('friend')]['sharing']


@app.route('/deleteFriend', methods=['post'])
@auth.login_required
def delete_friend():
    data['users'][auth.current_user()]['friends'].pop(request.args.get('friend'), None)
    save_data()
    return 'OK'


@app.route('/location')
@auth.login_required
def get_location():
    if auth.current_user() not in data['users'][request.json['user']]['friends']:
        return 'Not friends', 401
    if data['users'][request.json['user']]['sharing'] == 'OFF' or \
            data['users'][request.json['user']]['friends'][auth.current_user()]['sharing'] == 'OFF':
        return 'User not sharing location', 401
    locations = data['users'][request.json['user']]['locations']
    return locations[len(locations) - 1]


@app.route('/locationHistory')
@auth.login_required
def get_location_history():
    if data['users'][request.json['user']]['sharing'] != 'ALL' or \
            data['users'][request.json['user']]['friends'][auth.current_user()]['sharing'] != 'ALL':
        return get_location()
    return data['users'][request.json['user']]['locations']


@app.route('/updateSharing', methods=['post'])
@auth.login_required
def update_sharing():
    data['users'][auth.current_user()]['sharing'] = request.args.get('sharing')
    save_data()
    return 'OK'


@app.route('/sharing')
@auth.login_required
def get_sharing():
    return data['users'][auth.current_user()]['sharing']


@app.route('/updateLocation', methods=['post'])
@auth.login_required
def update_location():
    location = {
        'time': int(datetime.now().timestamp()),
        'long': request.json['long'],
        'lat': request.json['lat']
    }
    locations = data['users'][auth.current_user()]['locations']

    # Save the last location per minute
    # noinspection PyTypeChecker
    if len(locations) > 0 and int(locations[len(locations) - 1]['time'] / 60) == int(location['time'] / 60):
        locations.pop()
    locations.append(location)
    save_data()
    return 'OK'


if __name__ == '__main__':
    load_data()
    app.run('0.0.0.0', '6969')
