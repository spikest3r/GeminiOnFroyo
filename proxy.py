from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.request
import urllib.error

API_KEY = "my-secret-key" # lan local api key for phone so it can access server, leave the same
GEMINI_API_KEY = "YOUR_GEMINI_KEY_HERE" # get on aistudio.google.com
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY

class Handler(BaseHTTPRequestHandler):

    def do_POST(self):
        if self.path != "/request":
            self.send_response(404)
            self.end_headers()
            return

        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length)

        try:
            data = json.loads(body)
        except:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"bad json")
            return

        if data.get("api_key") != API_KEY:
            self.send_response(401)
            self.end_headers()
            self.wfile.write(b"unauthorized")
            return

        message = data.get("message", "")
        if not message:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"no message")
            return

        try:
            messageFinal = "Do not add emojis, do not use markdown or fancy formatting. You will be given converstaion, you have to continue it.\n" + message
            gemini_body = json.dumps({
                "contents": [{"parts": [{"text": messageFinal}]}]
            }).encode("utf-8")

            req = urllib.request.Request(
                GEMINI_URL,
                data=gemini_body,
                headers={"Content-Type": "application/json"},
                method="POST"
            )

            with urllib.request.urlopen(req) as resp:
                gemini_data = json.loads(resp.read())
                reply = gemini_data["candidates"][0]["content"]["parts"][0]["text"]

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(reply.encode("utf-8"))

        except urllib.error.HTTPError as e:
            err = e.read().decode()
            print("gemini error:", err)  # <-- add this
            self.send_response(502)
            self.end_headers()
            self.wfile.write(("gemini error: " + err).encode())
        except Exception as e:
            self.send_response(500)
            self.end_headers()
            self.wfile.write(str(e).encode())

    def log_message(self, format, *args):
        print(f"[{self.address_string()}] {format % args}")


if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 6969), Handler)
    print("running on port 6969")
    server.serve_forever()
