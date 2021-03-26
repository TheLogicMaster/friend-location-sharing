from flask import Flask, request
import json
from os import path
from flask_httpauth import HTTPBasicAuth
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
auth = HTTPBasicAuth()

data = {
    'users': {
        'user': {
            'password': generate_password_hash('1234')
        }
    }
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


if __name__ == '__main__':
    load_data()
    app.run('0.0.0.0', '6969')
