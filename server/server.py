from flask import Flask

app = Flask(__name__)


@app.route('/users')
def users():
    return ['albi', 'justin']


if __name__ == '__main__':
    app.run()
