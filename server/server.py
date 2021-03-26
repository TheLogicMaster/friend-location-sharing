from flask import Flask, request
import json
import copy
from os import path
from flask_httpauth import HTTPBasicAuth
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

app = Flask(__name__)
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
            'id': '1234567890',
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
        'password': generate_password_hash(password)
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


@app.route('/friends')
@auth.login_required
def get_friends():
    return data['users'][auth.current_user()]['friends']
    

@app.route('/friend')
@auth.login_required
def get_friend():
    name = request.args.get('name')
    friend = {
        'name': name,
        'sharing': data['users'][auth.current_user()]['friends'][name]['sharing']
    }
    locations = data['users'][name]['locations']
    if friend['sharing'] == 'ALL' and data['users'][name]['sharing'] == 'ALL':
        friend['locations'] = locations
    elif friend['sharing'] != 'OFF' and data['users'][name]['sharing'] != 'OFF':
        friend['locations'] = [locations[len(locations) - 1]] if len(locations) > 0 else []
    else:
        friend['locations'] = []
    return friend


@app.route('/location')
@auth.login_required
def get_location():
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


@app.route('/updateLocation', methods=['post'])
@auth.login_required
def update_location():
    if 'long' in request.json and 'lat' in request.json:
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

    if 'sharing' in request.json:
        data['users'][auth.current_user()]['sharing'] = request.json['sharing']

    return 'OK'


if __name__ == '__main__':
    load_data()
    app.run('0.0.0.0', '6969')
