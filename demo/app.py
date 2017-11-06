from flask import Flask,  request


app=Flask(__name__)


@app.route('/health')
def get_health():
	return "OK"

@app.route('/crash')
def crash():
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()



if __name__ == "__main__":
	app.debug = True
	app.run(threaded=True)